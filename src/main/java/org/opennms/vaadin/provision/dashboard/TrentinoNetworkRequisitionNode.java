package org.opennms.vaadin.provision.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

import static org.opennms.vaadin.provision.dashboard.DashBoardService.m_vrfs;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.m_network_categories;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.m_notif_categories;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.m_thresh_categories;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.CITY;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.ADDRESS;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.DESCRIPTION;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasInvalidDnsBind9Label;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasInvalidDnsBind9LabelSize;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasInvalidDnsBind9Size;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasUnSupportedDnsDomain;


public class TrentinoNetworkRequisitionNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	MultivaluedMap<String, String> m_updatemap = new MultivaluedMapImpl();
    List<RequisitionAsset> m_assetsToPut = new ArrayList<RequisitionAsset>();

    List<RequisitionCategory> m_categoriesToAdd = new ArrayList<RequisitionCategory>();
    List<RequisitionCategory> m_categoriesToDel = new ArrayList<RequisitionCategory>();
    
    List<RequisitionInterface> m_interfToAdd = new ArrayList<RequisitionInterface>();
    List<RequisitionInterface> m_interfToDel = new ArrayList<RequisitionInterface>();

    protected String descr;
	protected String hostname;
	protected String vrf;
	protected String primary;
	protected IndexedContainer secondary = new IndexedContainer();

	protected String parent;

	protected String[] networkCategory;
	protected String notifCategory;
	protected String threshCategory;

	protected String snmpProfile;
	protected String backupProfile;

	protected String city;
	protected String address1;
	
	protected String nodelabel;
	protected String foreignId;
	
	protected boolean valid = true;
	private boolean update = true;
	private boolean updatenodeLabel = false;
	private boolean updatesnmpprofile = false;

	private DashBoardService m_service;
	
	public TrentinoNetworkRequisitionNode(String label,DashBoardService service) {
		m_service = service;
		hostname="";
		
		primary="0.0.0.0";
		
		city="";
		address1="";

		networkCategory = m_network_categories[0]; 
		vrf = m_network_categories[0][2];
		notifCategory = m_network_categories[0][3];
		threshCategory = m_network_categories[0][4];
		backupProfile = m_network_categories[0][5];
		snmpProfile = m_network_categories[0][6];
				
		nodelabel = label;
		
		descr="Imported from Provision Dashboard";
		secondary.addContainerProperty("indirizzo ip", String.class, null);
		
		update=false;
		valid=false;
		
		updatesnmpprofile = true;
		updatenodeLabel = true;
	}

	@SuppressWarnings("unchecked")
	public TrentinoNetworkRequisitionNode(RequisitionNode requisitionNode, DashBoardService service) {
		m_service = service;

		foreignId = requisitionNode.getForeignId();
		if (foreignId == null)
			valid = false;
		nodelabel = requisitionNode.getNodeLabel();
		if (nodelabel == null)
			valid=false;

		if (requisitionNode.getParentForeignId() != null)
			parent = service.getNodeLabel(requisitionNode.getParentForeignId());
		
		for (String vrf: m_vrfs) {
			if (requisitionNode.getNodeLabel().endsWith("."+vrf)) {
				this.vrf = vrf;
				hostname = requisitionNode.getNodeLabel().substring(0,requisitionNode.getNodeLabel().indexOf(vrf)-1);
				break;
			}
		}

		if (vrf == null || hostname == null)
			valid = false;
		
		secondary.addContainerProperty("indirizzo ip", String.class, null);
		for (RequisitionInterface ip: requisitionNode.getInterfaces()) {
			if (ip.getSnmpPrimary() == null)
				valid=false;
			if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
				primary = ip.getIpAddr();
				descr = ip.getDescr();
			} else {
				Item ipItem = secondary.getItem(secondary.addItem());
				ipItem.getItemProperty("indirizzo ip").setValue(ip.getIpAddr()); 
			}
		}
		
		if (primary == null)
			valid = false;
		
		for (String[] networkCategory: m_network_categories) {
			if (requisitionNode.getCategory(networkCategory[0]) != null && requisitionNode.getCategory(networkCategory[1]) != null) {
				this.networkCategory = networkCategory;
				break;
			}
		}
		if (networkCategory == null)
			valid = false;

		for (String notifCategory: m_notif_categories) {
			if (requisitionNode.getCategory(notifCategory) != null ) {
				this.notifCategory = notifCategory;
				break;
			}
		}
		if (notifCategory == null)
			valid = false;
		
		for (String threshCategory: m_thresh_categories) {
			if (requisitionNode.getCategory(threshCategory) != null ) {
				this.threshCategory = threshCategory;
				break;
			}
		}
		if (threshCategory == null)
			valid = false;
		
		for (String profileId: m_service.getBackupProfiles()) {
			RequisitionNode profile = m_service.getBackupProfile(profileId);
		
			if (requisitionNode.getAsset("username") != null && profile.getAsset("username") != null 
			 && requisitionNode.getAsset("username").getValue().equals(profile.getAsset("username").getValue()) 
		     && requisitionNode.getAsset("password") != null && profile.getAsset("password") != null 
		     && requisitionNode.getAsset("password").getValue().equals(profile.getAsset("password").getValue())
			 && requisitionNode.getAsset("enable") != null   && profile.getAsset("enable") != null 
			 && requisitionNode.getAsset("enable").getValue().equals(profile.getAsset("enable").getValue())
			 && requisitionNode.getAsset("connection") != null && profile.getAsset("connection") != null 
			 && requisitionNode.getAsset("connection").getValue().equals(profile.getAsset("connection").getValue())
			 && (
					 (requisitionNode.getAsset("autoenable") != null && profile.getAsset("autoenable") != null 
					 && requisitionNode.getAsset("autoenable").getValue().equals(profile.getAsset("autoenable").getValue()))
			 	|| (requisitionNode.getAsset("autoenable") == null 
			 	   && (profile.getAsset("autoenable") == null || profile.getAsset("autoenable").getValue().equals("")	
			)))
			) {
				backupProfile=profileId;
				break;
			}
		}
		if (backupProfile == null)
			valid = false;
		
		if (requisitionNode.getCity() != null)
			city = requisitionNode.getCity();
		else
			valid = false;
		
		if (requisitionNode.getAsset(ADDRESS) != null)
			address1 = requisitionNode.getAsset(ADDRESS).getValue();
		else
			valid = false;
		
		if (hasInvalidDnsBind9Size(getNodeLabel()))
			valid = false;
		if (hasInvalidDnsBind9LabelSize(nodelabel))
			valid = false;
		if (hasInvalidDnsBind9Label(nodelabel))
			valid = false;
		if (hostname != null && hasUnSupportedDnsDomain(hostname,nodelabel))
			valid = false;		
	}
	
	public String getParent() {
		return parent;
	}

	public void setParent(String parentNodeLabel) {
		if (this.parent != null && this.parent.equals(parentNodeLabel))
			return;
		if (this.parent == null && parentNodeLabel == null) 
			return;
		
		String parentForeignId = "";
		if (parentNodeLabel != null) {
			parentForeignId = m_service.getForeignId(parentNodeLabel);
		}
		
		m_updatemap.add("parent-foreign-id", parentForeignId);
		
		this.parent = parentNodeLabel;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		if (this.city != null && this.city.equals(city))
			return;
		RequisitionAsset assetdescription = new RequisitionAsset(DESCRIPTION, city + " - " + address1);
		if (city != null) {
			m_updatemap.add(CITY, city);
			m_assetsToPut.add(assetdescription);
		}
		this.city = city;
		
	}

	public String[] getNetworkCategory() {
		return networkCategory;
	}

	public void setNetworkCategory(String[] networkCategory) {
		if (networkCategory != null && Arrays.equals(this.networkCategory,networkCategory))
			return;
		if (this.networkCategory != null) {
			m_categoriesToDel.add(new RequisitionCategory(this.networkCategory[0]));
			m_categoriesToDel.add(new RequisitionCategory(this.networkCategory[1]));
		}
		
		if (networkCategory != null) {
			m_categoriesToAdd.add(new RequisitionCategory(networkCategory[0]));
			m_categoriesToAdd.add(new RequisitionCategory(networkCategory[1]));
		}
		this.networkCategory = networkCategory;
	}
	
	public String getNotifCategory() {
		return notifCategory;
	}
	public void setNotifCategory(String notifCategory) {
		if (notifCategory != null && notifCategory.equals(this.notifCategory))
			return;
		if (this.notifCategory != null) {
			m_categoriesToDel.add(new RequisitionCategory(this.notifCategory));
		}
		if (notifCategory != null) { 
			m_categoriesToAdd.add(new RequisitionCategory(notifCategory));
		}
		this.notifCategory = notifCategory;
	}
	public String getThreshCategory() {
		return threshCategory;
	}
	public void setThreshCategory(String threshCategory) {
		if (threshCategory != null && threshCategory.equals(this.threshCategory))
			return;
		if (this.threshCategory != null) {
			m_categoriesToDel.add(new RequisitionCategory(this.threshCategory));
		}
		if (threshCategory != null) {
			m_categoriesToAdd.add(new RequisitionCategory(threshCategory));
		}		
		this.threshCategory = threshCategory;
	}
	public String getVrf() {
		return vrf;
	}
	public void setVrf(String vrf) throws ProvisionDashboardValidationException {
		if (this.vrf != null && this.vrf.equals(vrf))
			return;
		if (vrf != null && hostname != null ) {
			updatenodeLabel = true;
		}
		this.vrf = vrf;
	}

    public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}
	public IndexedContainer getSecondary() {
		return secondary;
	}
	
	public String getSnmpProfile() {
		return snmpProfile;
	}

	public void setSnmpProfile(String snmpProfile) {
		if (this.snmpProfile != null && this.snmpProfile.equals(snmpProfile))
			return;
		if (snmpProfile != null)
			updatesnmpprofile = true;
		this.snmpProfile = snmpProfile;
	}
	
	public void updateSnmpProfile(String snmpProfile) {
		this.snmpProfile=snmpProfile;
	}
	public String getBackupProfile() {
		return backupProfile;
	}
	public void setBackupProfile(String backupProfile) {
		if (this.backupProfile != null && this.backupProfile.equals(backupProfile))
			return;
		if (backupProfile != null) {
			for ( RequisitionAsset backupProfileItem : m_service.getBackupProfile(backupProfile).getAssets()) {
				m_assetsToPut.add(backupProfileItem);
			}
		}
		this.backupProfile = backupProfile;
	}

	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address) {
		if (this.address1 !=  null && this.address1.equals(address))
			return;
		if (address != null ) {
			m_assetsToPut.add(new RequisitionAsset(ADDRESS, address));
			m_assetsToPut.add(new RequisitionAsset(DESCRIPTION, city + " - " + address));
		}
		this.address1 = address;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) throws ProvisionDashboardValidationException {
		if (this.hostname != null && this.hostname.equals(hostname))
			return;
		if ( hostname != null ) {
			updatenodeLabel = true;
		}
		this.hostname = hostname;
	}
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		if (this.primary != null && this.primary.equals(primary))
			return;
		if (this.primary != null) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setIpAddr(this.primary);
			m_interfToDel.add(iface);
		}

		if (primary != null) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setSnmpPrimary(org.opennms.netmgt.model.PrimaryType.PRIMARY);
			iface.setIpAddr(primary);
			iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
			iface.setDescr("Provided by Provision Dashboard");
			setDescr(iface.getDescr());
			
			m_interfToAdd.add(iface);
			updatesnmpprofile=true;
		}
		this.primary = primary;
	}
	
	public String getNodeLabel() {
		return nodelabel;
    }
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public boolean getUpdate() {
		return update;
	}
	
	public void addSecondaryInterface(String ipaddress) {
		RequisitionInterface ipsecondary = new RequisitionInterface();
		ipsecondary.setIpAddr(ipaddress);
		ipsecondary.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
		ipsecondary.setDescr("Provided by Provision Dashboard");
		ipsecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		m_service.add(foreignId, ipsecondary);
	}
	
	public void removeSecondaryInteface(String ipaddress) {
		RequisitionInterface ipsecondary = new RequisitionInterface();
		ipsecondary.setIpAddr(ipaddress);
		m_service.delete(foreignId, ipsecondary);
	}
	
	public RequisitionNode getRequisitionNode() {
		RequisitionNode requisitionNode = new RequisitionNode();
		if (foreignId != null)
			requisitionNode.setForeignId(foreignId);
		else 
			requisitionNode.setForeignId(hostname);
		requisitionNode.setNodeLabel(nodelabel);
		if (parent != null) {
			requisitionNode.setParentForeignId(m_service.getForeignId(parent));
		}
		requisitionNode.setCity(city);
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(primary);
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
		iface.setDescr("Provided by Provision Dashboard");
		setDescr(iface.getDescr());
		
		requisitionNode.putInterface(iface);

		if (networkCategory != null) {
			requisitionNode.putCategory(new RequisitionCategory(networkCategory[0]));
			requisitionNode.putCategory(new RequisitionCategory(networkCategory[1]));
		}
		
		if (notifCategory != null)
			requisitionNode.putCategory(new RequisitionCategory(notifCategory));
		if (threshCategory != null)
			requisitionNode.putCategory(new RequisitionCategory(threshCategory));
		
		if (city != null && address1 != null)
			requisitionNode.putAsset(new RequisitionAsset(DESCRIPTION, city + " - " + address1));
		if (address1 != null)
			requisitionNode.putAsset(new RequisitionAsset(ADDRESS, address1));
		
		for ( RequisitionAsset backupProfileItem : m_service.getBackupProfile(backupProfile).getAssets()) {
			requisitionNode.putAsset(backupProfileItem);
		}
		return requisitionNode;
	}
	
	public void commit() throws ProvisionDashboardValidationException {
		InetAddressUtils.getInetAddress(primary);
		
		if (updatenodeLabel && vrf != null && hostname != null)
			updateNodeLabel();
		
		if (updatesnmpprofile && snmpProfile != null && primary != null )
			m_service.setSnmpInfo(primary, snmpProfile);
		
		if (!update) {
			if (m_service.hasDuplicatedForeignId(hostname))
							throw new ProvisionDashboardValidationException("The foreign id exist: cannot duplicate foreignId: " + hostname);
			if (m_service.hasDuplicatedPrimary(primary))
				throw new ProvisionDashboardValidationException("The primary ip exist: cannot duplicate primary: " + primary);
			m_service.add(getRequisitionNode(),primary);
			valid = true;
			update=true;
		} else {
			if (!m_updatemap.isEmpty())
				m_service.update(foreignId, m_updatemap);
			for (RequisitionInterface riface: m_interfToDel) {
				m_service.delete(foreignId, riface);
			}
			for (RequisitionInterface riface: m_interfToAdd) {
				m_service.add(foreignId, riface);
			}
			for (RequisitionAsset asset: m_assetsToPut) {
				m_service.add(foreignId, asset);
			}
			for (RequisitionCategory category: m_categoriesToDel) {
				m_service.delete(foreignId, category);
			}
			for (RequisitionCategory category: m_categoriesToAdd) {
				m_service.add(foreignId, category);
			}
		}

		updatenodeLabel = false;
		updatesnmpprofile = false;
		m_updatemap.clear();
		m_interfToDel.clear();
		m_interfToAdd.clear();
		m_assetsToPut.clear();
		m_categoriesToDel.clear();
		m_categoriesToAdd.clear();
	}
	
	private void updateNodeLabel() throws ProvisionDashboardValidationException {
		String nodelabel=hostname.toLowerCase() + "." + vrf;
		if	(hasInvalidDnsBind9Size(nodelabel))
			throw new ProvisionDashboardValidationException("Bind9 error: The full domain name exceed a total length of 253: " + nodelabel);
		if (hasInvalidDnsBind9LabelSize(nodelabel))
			throw new ProvisionDashboardValidationException("Bind9 error: dns label contains more then 63 characters: " + nodelabel);
		if (hasInvalidDnsBind9Label(nodelabel))
			throw new ProvisionDashboardValidationException("Bind9 error: dns label does not contain only a-zA-Z0-9 characters or start or end with hypen: " + nodelabel);
		if (hasUnSupportedDnsDomain(hostname, nodelabel))
			throw new ProvisionDashboardValidationException("There is no dns domain defined for: " + nodelabel);
		if (m_service.hasDuplicatedNodelabel(nodelabel))
			throw new ProvisionDashboardValidationException("The node label exist: cannot duplicate node label: " + nodelabel);
		this.nodelabel = nodelabel;
		m_updatemap.add("node-label", nodelabel);
		
	}

}
