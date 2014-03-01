package org.opennms.vaadin.provision.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.core.utils.InetAddressUtils;
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

import static org.opennms.vaadin.provision.dashboard.DashBoardService.TN;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.DESCRIPTION;

import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasInvalidDnsBind9Label;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasInvalidDnsBind9LabelSize;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasInvalidDnsBind9Size;
import static org.opennms.vaadin.provision.dashboard.DashBoardService.hasUnSupportedDnsDomain;


public class TrentinoNetworkRequisitionNode {

	private static final Logger logger = Logger.getLogger(TrentinoNetworkRequisitionNode.class.getName());
	
	private RequisitionNode m_requisitionNode;

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
	
	protected boolean valid = true;
	private boolean update = true;
	private boolean updatenodeLabel = false;
	private boolean updatesnmpprofile = false;

	private DashBoardService m_service;
	
	public TrentinoNetworkRequisitionNode(String label,DashBoardService service) {
		m_service = service;
		m_requisitionNode = new RequisitionNode();
		hostname="";
		primary="";
		networkCategory = m_network_categories[0]; 
		vrf = m_network_categories[0][2];
		notifCategory = m_network_categories[0][3];
		threshCategory = m_network_categories[0][4];
		backupProfile = m_network_categories[0][5];
		snmpProfile = m_network_categories[0][6];
				
		m_requisitionNode.setNodeLabel(label);
		descr="Imported from Provision Dashboard";
		secondary.addContainerProperty("indirizzo ip", String.class, null);
		update=false;
		updatesnmpprofile = true;
		updatesnmpprofile = true;
	}

	@SuppressWarnings("unchecked")
	public TrentinoNetworkRequisitionNode(RequisitionNode requisitionNode, DashBoardService service) {
		m_service = service;
		m_requisitionNode = requisitionNode;

		if (m_requisitionNode.getParentForeignId() != null)
			parent = service.getNodeLabel(m_requisitionNode.getParentForeignId());
		
		for (String vrf: m_vrfs) {
			if (m_requisitionNode.getNodeLabel().endsWith("."+vrf)) {
				this.vrf = vrf;
				hostname = m_requisitionNode.getNodeLabel().substring(0,m_requisitionNode.getNodeLabel().indexOf(vrf)-1);
				break;
			}
		}

		if (vrf == null || hostname == null)
			valid = false;
		logger.info("checked hostname and vrf: " + hostname + "." + vrf +" valid: " + valid);
		secondary.addContainerProperty("indirizzo ip", String.class, null);
		for (RequisitionInterface ip: m_requisitionNode.getInterfaces()) {
			if (ip.getSnmpPrimary().equals("P")) {
				primary = ip.getIpAddr();
				descr = ip.getDescr();
			} else {
				Item ipItem = secondary.getItem(secondary.addItem());
				ipItem.getItemProperty("indirizzo ip").setValue(ip.getIpAddr()); 
			}
		}
		if (primary == null)
			valid = false;
		logger.info("checked primary: " + primary + " valid: " + valid);
		
		for (String[] networkCategory: m_network_categories) {
			if (m_requisitionNode.getCategory(networkCategory[0]) != null && m_requisitionNode.getCategory(networkCategory[1]) != null) {
				this.networkCategory = networkCategory;
				break;
			}
		}
		if (networkCategory == null)
			valid = false;
		logger.info("checked networkcategory: " + networkCategory + " valid: " + valid);

		for (String notifCategory: m_notif_categories) {
			if (m_requisitionNode.getCategory(notifCategory) != null ) {
				this.notifCategory = notifCategory;
				break;
			}
		}
		if (notifCategory == null)
			valid = false;
		logger.info("checked notifcategory: " + notifCategory + " valid: " + valid);
		
		for (String threshCategory: m_thresh_categories) {
			if (m_requisitionNode.getCategory(threshCategory) != null ) {
				this.threshCategory = threshCategory;
				break;
			}
		}
		if (threshCategory == null)
			valid = false;
		logger.info("checked threshcategory: " + threshCategory + " valid: " + valid);
		
		for (String profileId: m_service.getBackupProfiles()) {
			RequisitionNode profile = m_service.getBackupProfile(profileId);
		
			if (m_requisitionNode.getAsset("username") != null && profile.getAsset("username") != null 
			 && m_requisitionNode.getAsset("username").getValue().equals(profile.getAsset("username").getValue()) 
		     && m_requisitionNode.getAsset("password") != null && profile.getAsset("password") != null 
		     && m_requisitionNode.getAsset("password").getValue().equals(profile.getAsset("password").getValue())
			 && m_requisitionNode.getAsset("enable") != null   && profile.getAsset("enable") != null 
			 && m_requisitionNode.getAsset("enable").getValue().equals(profile.getAsset("enable").getValue())
			 && m_requisitionNode.getAsset("connection") != null && profile.getAsset("connection") != null 
			 && m_requisitionNode.getAsset("connection").getValue().equals(profile.getAsset("connection").getValue())
			 && (
					 (m_requisitionNode.getAsset("autoenable") != null && profile.getAsset("autoenable") != null 
					 && m_requisitionNode.getAsset("autoenable").getValue().equals(profile.getAsset("autoenable").getValue()))
			 	|| (m_requisitionNode.getAsset("autoenable") == null 
			 	   && (profile.getAsset("autoenable") == null || profile.getAsset("autoenable").getValue().equals("")	
			)))
			) {
				backupProfile=profileId;
				break;
			}
		}
		if (backupProfile == null)
			valid = false;
		logger.info("checked backupProfile: " + backupProfile + " valid: " + valid);
		
		if (requisitionNode.getCity() != null)
			city = requisitionNode.getCity();
		else
			valid = false;
		logger.info("checked city: " + city + " valid: " + valid);
		
		if (m_requisitionNode.getAsset(ADDRESS) != null)
			address1 = m_requisitionNode.getAsset(ADDRESS).getValue();
		else
			valid = false;
		logger.info("checked address1: " + address1 + " valid: " + valid);
		
		if (hasInvalidDnsBind9Size(getNodeLabel()))
			valid = false;
		logger.info("hasInvalidDnsBind9Size: " + hostname + " valid: " + valid);
		if (hasInvalidDnsBind9LabelSize(getNodeLabel()))
			valid = false;
		logger.info("hasInvalidDnsBind9LabelSize: " + hostname + " valid: " + valid);
		if (hasInvalidDnsBind9Label(getNodeLabel()))
			valid = false;
		logger.info("hasInvalidDnsBind9Label: " + hostname + " valid: " + valid);
		if (hostname != null && hasUnSupportedDnsDomain(hostname,getNodeLabel()))
			valid = false;
		logger.info("hasUnSupportedDnsDomain: " + hostname + " valid: " + valid);
		
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
		m_requisitionNode.setParentForeignId(parentForeignId);
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
			m_requisitionNode.setCity(city);
			m_updatemap.add(CITY, city);
			m_requisitionNode.putAsset(assetdescription);
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
			for (String cat: this.networkCategory) {
				RequisitionCategory oldcategory = new RequisitionCategory(cat);
				m_categoriesToDel.add(oldcategory);
				m_requisitionNode.deleteCategory(oldcategory);
			}
		}
		
		if (networkCategory != null) {
			for (String cat: networkCategory) {
				RequisitionCategory newcategory = new RequisitionCategory(cat);
				m_categoriesToAdd.add(newcategory);
				m_requisitionNode.putCategory(newcategory);
			}
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
			RequisitionCategory oldcategory = new RequisitionCategory(this.notifCategory);
			m_categoriesToDel.add(oldcategory);
			m_requisitionNode.deleteCategory(oldcategory);
		}
		if (notifCategory != null) { 
			RequisitionCategory newcategory = new RequisitionCategory(notifCategory);
			m_requisitionNode.putCategory(newcategory);
			m_categoriesToAdd.add(newcategory);
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
			RequisitionCategory category = new RequisitionCategory(this.threshCategory);
			m_categoriesToDel.add(category);
			m_requisitionNode.deleteCategory(category);
		}
		if (threshCategory != null) {
			RequisitionCategory newcategory = new RequisitionCategory(threshCategory);
			m_requisitionNode.putCategory(newcategory);
			m_categoriesToAdd.add(newcategory);
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
				m_requisitionNode.putAsset(backupProfileItem);
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
			RequisitionAsset asset = new RequisitionAsset(ADDRESS, address);
			RequisitionAsset assetdescription = new RequisitionAsset(DESCRIPTION, city + " - " + address);
			m_requisitionNode.putAsset(asset);
			m_requisitionNode.putAsset(assetdescription);
			m_assetsToPut.add(asset);
			m_assetsToPut.add(assetdescription);
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
		if (primary != null) {
			InetAddressUtils.getInetAddress(primary);
			RequisitionInterface iface = new RequisitionInterface();
			iface.setSnmpPrimary("P");
			iface.setIpAddr(primary);
			iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
			iface.setDescr("Provided by Provision Dashboard");
			setDescr(iface.getDescr());
			
			if (this.primary != null) {
				m_interfToDel.add(m_requisitionNode.getInterface(this.primary));
				iface.setMonitoredServices(m_requisitionNode.getInterface(this.primary).getMonitoredServices());
				m_requisitionNode.deleteInterface(this.primary);
			}
			m_interfToAdd.add(iface);
			m_requisitionNode.putInterface(iface);
			updatesnmpprofile=true;
		}
		this.primary = primary;
	}
	public RequisitionNode getRequisitionNode() {
		return m_requisitionNode;
	}
	public void setRequisitionNode(RequisitionNode requisitionNode) {
		m_requisitionNode = requisitionNode;
	}
	public String getNodeLabel() {
		return m_requisitionNode.getNodeLabel();
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
		ipsecondary.setSnmpPrimary("N");
		ipsecondary.setDescr("Provided by Provision Dashboard");
		ipsecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		m_service.add(TN, m_requisitionNode.getForeignId(), ipsecondary);
		m_requisitionNode.putInterface(ipsecondary);
	}
	
	public void removeSecondaryInteface(String ipaddress) {
		RequisitionInterface ipsecondary = new RequisitionInterface();
		ipsecondary.setIpAddr(ipaddress);
		m_service.delete(TN, m_requisitionNode.getForeignId(), ipsecondary);
		m_requisitionNode.deleteInterface(ipaddress);
	}
	
	public void commit() throws ProvisionDashboardValidationException {
		if (updatenodeLabel && vrf != null && hostname != null)
			updateNodeLabel();
		if (updatesnmpprofile && snmpProfile != null && primary != null )
			m_service.setSnmpInfo(primary, snmpProfile);
		if (!update) {
			if (m_service.hasDuplicatedForeignId(hostname))
							throw new ProvisionDashboardValidationException("The foreign id exist: cannot duplicate foreignId: " + hostname);
			m_requisitionNode.setForeignId(hostname);
			m_service.add(TN, m_requisitionNode);
			update=true;
		} else {
			m_service.update(TN, m_requisitionNode.getForeignId(), m_updatemap);
			for (RequisitionInterface riface: m_interfToDel) {
				m_service.delete(TN, m_requisitionNode.getForeignId(), riface);
			}
			for (RequisitionInterface riface: m_interfToAdd) {
				m_service.add(TN, m_requisitionNode.getForeignId(), riface);
			}
			for (RequisitionAsset asset: m_assetsToPut) {
				m_service.add(TN, m_requisitionNode.getForeignId(), asset);
			}
			for (RequisitionCategory category: m_categoriesToDel) {
				m_service.delete(TN, m_requisitionNode.getForeignId(), category);
			}
			for (RequisitionCategory category: m_categoriesToAdd) {
				m_service.add(TN, m_requisitionNode.getForeignId(), category);
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
		String nodelabel=hostname + "." + vrf;
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
		m_requisitionNode.setNodeLabel(nodelabel);
		if (update) {
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			map.add("node-label", nodelabel);
			m_service.update(TN, m_requisitionNode.getForeignId(), map);
		}
	}

}
