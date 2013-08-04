package org.opennms.vaadin.provision.dashboard;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.snmpinfo.SnmpInfo;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.sqlcontainer.RowId;

public class TrentinoNetworkRequisitionNode {

	protected static final String DESCR = "descr";
	protected static final String DESCRIPTION = "description";
	protected static final String HOST = "hostname";
	protected static final String PRIMARY = "primary";
	protected static final String VRF = "vrf";
	protected static final String PARENT = "parent";
	
	protected static final String CITY    = "city";
	protected static final String ADDRESS = "address1";

	protected static final String NETWORK_CATEGORY = "networkCategory";
	protected static final String NOTIF_CATEGORY   = "notifCategory";
	protected static final String THRESH_CATEGORY  = "threshCategory";
	
	protected static final String SNMP_PROFILE    = "snmpProfile";
	protected static final String BACKUP_PROFILE  = "backupProfile";
	
	protected static final String TN = "TrentinoNetwork";
	
	protected static final String[] m_notif_categories = {
		"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"
	};
	
	protected static final String[] m_thresh_categories = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};

	protected static final String[][] m_network_categories = {
		{"AccessPoint","Backbone"},
		{"Core","Backbone"},
		{"Fiemme2013","Backbone"},
		{"Ponte5p4","Backbone"},
		{"PontePDH","Backbone"},
		{"SwitchWiNet","Backbone"},
		{"Fiemme2013","Backbone"},
		{"AgLav","Accesso"},
		{"Apss","Accesso"},
		{"Biblio","Accesso"},
		{"CPE","Accesso"},
		{"CUE","Accesso"},
		{"ComuneTN","Accesso"},
		{"Comuni","Accesso"},
		{"ConsPro","Accesso"},
		{"GeoSis","Accesso"},
		{"Info","Accesso"},
		{"Internet","Accesso"},
		{"Internet-Esterni","Accesso"},
		{"LAN","Accesso"},
		{"Medici","Accesso"},
		{"Mitt","Accesso"},
		{"OperaUnitn","Accesso"},
		{"Pat","Accesso"},
		{"PatAcquePub","Accesso"},
		{"PatDighe","Accesso"},
		{"PatVoce","Accesso"},
		{"RSACivicaTN","Accesso"},
		{"RSASpes","Accesso"},
		{"ReperibiliTnet","Accesso"},
		{"Scuole","Accesso"},
		{"ScuoleMaterne","Accesso"},
		{"Telpat-Autonome","Accesso"},
		{"Unitn","Accesso"},
		{"VdsRovereto","Accesso"},
		{"Winwinet","Accesso"}
	};
	
	public static final String[] m_sub_domains = {
		"sw01.bb.tnnet.it",
		"sw02.bb.tnnet.it",
		"cavalese-l3.pat.tnnet.it",
		"cpe01.biblio.tnnet.it",
		"cpe01.pat.tnnet.it",
		"cpe01.patacquepub.tnnet.it",
		"cpe01.scuole.tnnet.it",
		"mktic.comuni.tnnet.it",
		"mtk01.reperibilitnet.tnnet.it",
		"alv01.wl.tnnet.it",
		"alv02.wl.tnnet.it",
		"alv03.wl.tnnet.it",
		"alv04.wl.tnnet.it",
		"alv05.wl.tnnet.it",
		"alv06.wl.tnnet.it",
		"asw01.wl.tnnet.it",
		"cpe01.wl.tnnet.it",
		"ess01.wl.tnnet.it",
		"ess02.wl.tnnet.it",
		"ess03.wl.tnnet.it",
		"ess04.wl.tnnet.it",
		"ess05.wl.tnnet.it",
		"ess06.wl.tnnet.it",
		"ess07.wl.tnnet.it",
		"ess08.wl.tnnet.it",
		"mtr01.wl.tnnet.it",
		"uby.wl.tnnet.it"
	};
	
	protected static final String[] m_vrfs = {
		"aglav.tnnet.it",
		"apss.tnnet.it",
		"bb.tnnet.it",
		"biblio.tnnet.it",
		"comunetn.tnnet.it",
		"comuni.tnnet.it",
		"conspro.tnnet.it",
		"cue.tnnet.it",
		"esterni.tnnet.it",
		"geosis.tnnet.it",
		"hq.tnnet.it",
		"iasma.tnnet.it",
		"info.tnnet.it",
		"internet-esterni.tnnet.it",
		"internet.tnnet.it",
		"medici.tnnet.it",
		"mitt.tnnet.it",
		"mktic.comuni.tnnet.it",
		"operaunitn.tnnet.it",
		"pat.tnnet.it",
		"patacquepub.tnnet.it",
		"patdighe.tnnet.it",
		"patvoce.tnnet.it",
		"reperibilitnet.tnnet.it",
		"rsacivicatn.tnnet.it",
		"rsaspes.tnnet.it",
		"scuole.tnnet.it",
		"telpat-autonome.tnnet.it",
		"unitn.tnnet.it",
		"vdsrovereto.tnnet.it",
		"winwinet.tnnet.it",
		"wl.tnnet.it"
	};
	

	private RequisitionNode m_requisitionNode;

	protected String descr="Imported from Provision Dashboard";
	protected String hostname;
	protected String vrf;
	protected String primary;
	protected IndexedContainer secondary = new IndexedContainer();

	protected String parent;

	protected String[] networkCategory;
	protected String notifCategory;
	protected String threshCategory;

	protected RowId snmpProfile;
	protected RowId backupProfile;

	protected String city;
	protected String address1;
	
	private DashboardService m_service;
	private boolean update = true;
	
	public boolean getUpdate() {
		return update;
	}
	
	public TrentinoNetworkRequisitionNode(String label,DashboardService service) {
		m_service = service;
		m_requisitionNode = new RequisitionNode();
		m_requisitionNode.setNodeLabel(label);
		secondary.addContainerProperty("indirizzo ip", String.class, null);
		update=false;
	}

	@SuppressWarnings("unchecked")
	public TrentinoNetworkRequisitionNode(RequisitionNode requisitionNode, DashboardService service) {
		m_service = service;
		m_requisitionNode = requisitionNode;

		if (m_requisitionNode.getParentForeignId() != null)
			parent = m_requisitionNode.getParentForeignId();
		
		for (String vrf: m_vrfs) {
			if (m_requisitionNode.getNodeLabel().endsWith("."+vrf)) {
				this.vrf = vrf;
				hostname = m_requisitionNode.getNodeLabel().substring(0,m_requisitionNode.getNodeLabel().indexOf(vrf)-1);
				break;
			}
		}

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
		
		for (String[] networkCategory: m_network_categories) {
			if (m_requisitionNode.getCategory(networkCategory[0]) != null && m_requisitionNode.getCategory(networkCategory[1]) != null) {
				this.networkCategory = networkCategory;
				break;
			}
		}

		for (String notifCategory: m_notif_categories) {
			if (m_requisitionNode.getCategory(notifCategory) != null ) {
				this.notifCategory = notifCategory;
				break;
			}
		}
		
		for (String threshCategory: m_thresh_categories) {
			if (m_requisitionNode.getCategory(threshCategory) != null ) {
				this.threshCategory = threshCategory;
				break;
			}
		}
		
		for (Object profileId: m_service.getBackupProfiles().getItemIds()) {
			Item profile = m_service.getBackupProfiles().getItem(profileId);
		
			if (m_requisitionNode.getAsset("username") != null && m_requisitionNode.getAsset("username").getValue().equals(profile.getItemProperty("username").getValue()) 
		     && m_requisitionNode.getAsset("password") != null && m_requisitionNode.getAsset("password").getValue().equals(profile.getItemProperty("password").getValue())
			 && m_requisitionNode.getAsset("enable") != null && m_requisitionNode.getAsset("enable").getValue().equals(profile.getItemProperty("enable").getValue())
			 && m_requisitionNode.getAsset("autoenable") != null && m_requisitionNode.getAsset("autoenable").getValue().equals(profile.getItemProperty("auto_enable").getValue())
			 && m_requisitionNode.getAsset("connection") != null && m_requisitionNode.getAsset("connection").getValue().equals(profile.getItemProperty("connection").getValue())
					) {
				backupProfile=(RowId)profileId;
				break;
			}
		}
		
		if (requisitionNode.getCity() != null)
			city = requisitionNode.getCity();
		if (m_requisitionNode.getAsset(ADDRESS) != null)
			address1 = m_requisitionNode.getAsset(ADDRESS).getValue();
		
	}
	
	public String getParent() {
		return parent;
	}

	public void setParent(String parentForeignId) {
		if (this.parent != null && this.parent.equals(parentForeignId))
			return;
		if (parentForeignId != null) {
			m_requisitionNode.setParentForeignId(parentForeignId);
			if (update) {
				MultivaluedMap<String, String> map = new MultivaluedMapImpl();
				map.add("parent-foreign-id", parentForeignId);
				m_service.update(TN, m_requisitionNode.getForeignId(), map);
			}
		}
		this.parent = parentForeignId;
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
			m_requisitionNode.putAsset(assetdescription);
			if (update) {
				MultivaluedMap<String, String> map = new MultivaluedMapImpl();
				map.add(CITY, city);
				m_service.update(TN, m_requisitionNode.getForeignId(), map);
				m_service.add(TN, m_requisitionNode.getForeignId(), assetdescription);
			}
		}
		this.city = city;
		
	}

	public String[] getNetworkCategory() {
		return networkCategory;
	}
	public void setNetworkCategory(String[] networkCategory) {
		if (networkCategory != null && this.networkCategory == networkCategory)
			return;
		if (update && this.networkCategory != null) {
			for (String cat: this.networkCategory) {
				RequisitionCategory oldcategory = new RequisitionCategory(cat);
				m_service.delete(TN, m_requisitionNode.getForeignId(), oldcategory);
				m_requisitionNode.deleteCategory(oldcategory);
			}
		}
		
		if (networkCategory != null) {
			for (String cat: networkCategory) {
				RequisitionCategory newcategory = new RequisitionCategory(cat);
				m_requisitionNode.putCategory(newcategory);
				if (update)
					m_service.add(TN, m_requisitionNode.getForeignId(), newcategory);
			}
		}
		this.networkCategory = networkCategory;
	}
	
	public String getNotifCategory() {
		return notifCategory;
	}
	public void setNotifCategory(String notifCategory) {
		if (notifCategory != null && this.notifCategory == notifCategory )
			return;
		if (update && this.notifCategory != null) {
			RequisitionCategory oldcategory = new RequisitionCategory(this.notifCategory);
			m_service.delete(TN, m_requisitionNode.getForeignId(), oldcategory);
			m_requisitionNode.deleteCategory(oldcategory);
		}
		if (notifCategory != null) { 
			RequisitionCategory newcategory = new RequisitionCategory(notifCategory);
			m_requisitionNode.putCategory(newcategory);
			if (update)
				m_service.add(TN, m_requisitionNode.getForeignId(), newcategory);
		}
		this.notifCategory = notifCategory;
	}
	public String getThreshCategory() {
		return threshCategory;
	}
	public void setThreshCategory(String threshCategory) {
		if (threshCategory != null && this.threshCategory == threshCategory )
			return;
		if (update && this.threshCategory != null) {
			RequisitionCategory category = new RequisitionCategory(this.threshCategory);
			m_service.delete(TN, m_requisitionNode.getForeignId(), category);
			m_requisitionNode.deleteCategory(category);
		}
		if (threshCategory != null) {
			RequisitionCategory newcategory = new RequisitionCategory(threshCategory);
			m_requisitionNode.putCategory(newcategory);
			if (update)
				m_service.add(TN, m_requisitionNode.getForeignId(), newcategory);
		}		
		this.threshCategory = threshCategory;
	}
	public String getVrf() {
		return vrf;
	}
	public void setVrf(String vrf) throws ProvisionDashboardValidationException {
		if (this.vrf != null && this.vrf == vrf)
			return;
		if (vrf != null) {
			String nodelabel=hostname + "." + vrf;
			if ( hostname != null )
					checkNodeLabel(nodelabel);
			m_requisitionNode.setNodeLabel(nodelabel);
			if (update) {
				MultivaluedMap<String, String> map = new MultivaluedMapImpl();
				map.add("node-label", nodelabel);
				m_service.update(TN, m_requisitionNode.getForeignId(), map);
			}
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
	public RowId getSnmpProfile() {
		return snmpProfile;
	}
	public void setSnmpProfile(RowId snmpProfile) {
		if (this.snmpProfile != null && this.snmpProfile.equals(snmpProfile))
			return;
		updateSnmpProfileOnServer(this.primary, snmpProfile);
		this.snmpProfile = snmpProfile;
	}
	
	private void updateSnmpProfileOnServer(String ip,RowId snmp) {
			Item profile = m_service.getSnmpProfiles().getItem(snmp);
			if (profile != null) {
				SnmpInfo info = new SnmpInfo();
				info.setCommunity(profile.getItemProperty("community").getValue().toString());
				info.setVersion(profile.getItemProperty("version").getValue().toString());
				info.setTimeout(Integer.parseInt(profile.getItemProperty("timeout").getValue().toString()));
				m_service.setSnmpInfo(ip, info);
			}
	}
	public void updateSnmpProfile(SnmpInfo info) {
		for (Object profileId: m_service.getSnmpProfiles().getItemIds()) {
			Item snmpdata = m_service.getSnmpProfiles().getItem(profileId);
			if (info.getRetries() == 1 && info.getPort() == 161 
			  && info.getCommunity().equals(snmpdata.getItemProperty("community").getValue().toString()) 
			  && info.getVersion().equals(snmpdata.getItemProperty("version").getValue().toString())
			  && info.getTimeout() == Integer.parseInt(snmpdata.getItemProperty("timeout").getValue().toString())) {
				snmpProfile=(RowId)profileId;
				break;
			}
		}
	}
	
	public RowId getBackupProfile() {
		return backupProfile;
	}
	public void setBackupProfile(RowId backupProfile) {
		if (this.backupProfile != null && this.backupProfile.equals(backupProfile))
			return;
		if (backupProfile != null) {
			Item backup = m_service.getBackupProfiles().getItem(backupProfile);
			if (backup != null) {
				RequisitionAsset username = new RequisitionAsset("username", backup.getItemProperty("username").getValue().toString());
				RequisitionAsset password = new RequisitionAsset("password", backup.getItemProperty("password").getValue().toString());
				RequisitionAsset enable = new RequisitionAsset("enable",backup.getItemProperty("enable").getValue().toString());
				RequisitionAsset connection = new RequisitionAsset("connection", backup.getItemProperty("connection").getValue().toString());
				m_requisitionNode.putAsset(username);
				m_requisitionNode.putAsset(password);
				m_requisitionNode.putAsset(enable);
				m_requisitionNode.putAsset(connection);
				if (update) {
					m_service.add(TN, m_requisitionNode.getForeignId(), username);
					m_service.add(TN, m_requisitionNode.getForeignId(), password);
					m_service.add(TN, m_requisitionNode.getForeignId(), enable);
					m_service.add(TN, m_requisitionNode.getForeignId(), connection);
				}
				if (backup.getItemProperty("auto_enable") != null && backup.getItemProperty("auto_enable").getValue() != null) {
					RequisitionAsset autoenable = new RequisitionAsset("autoenable", backup.getItemProperty("auto_enable").getValue().toString());
					m_requisitionNode.putAsset(autoenable);
					if (update)
						m_service.add(TN, m_requisitionNode.getForeignId(), autoenable);
				}
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
			if (update) {
				m_service.add(TN, m_requisitionNode.getForeignId(), asset);
				m_service.add(TN, m_requisitionNode.getForeignId(), assetdescription);
			}
		}
		this.address1 = address;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) throws ProvisionDashboardValidationException {
		if (this.hostname != null && this.hostname.equals(hostname))
			return;
		if (hostname != null ) {
			//check if exist and then throws validation exception
			String nodelabel=hostname + "." + vrf;
			if (vrf != null) 
				checkNodeLabel(nodelabel);

			m_requisitionNode.setNodeLabel(nodelabel);
			if (update) {
				MultivaluedMap<String, String> map = new MultivaluedMapImpl();
				map.add("node-label", nodelabel);
				m_service.update(TN, m_requisitionNode.getForeignId(), map);
			} else {
				m_requisitionNode.setForeignId(hostname);
			}
		}
		this.hostname = hostname;
	}
	
	private void checkNodeLabel(String nodelabel ) throws ProvisionDashboardValidationException {
		//check duplicated hostname
		for (String label: m_service.getNodeLabels()) {
			if (label.equals(nodelabel)) 
				throw new ProvisionDashboardValidationException("The node label exist: cannot duplicate node label: " + label);
		}
		//check if domain name exists
		if (hostname.contains(".")) {
			boolean subdomainnotfound = true;
			String hostlabel = hostname.substring(0,hostname.indexOf("."));
			for (String subdomain: m_sub_domains ) {
				if (nodelabel.equals(hostlabel+"."+subdomain)) {
					subdomainnotfound = false;
					break;
				}
			}
			if (subdomainnotfound)
				throw new ProvisionDashboardValidationException("There is no dns domain for: " + nodelabel);
		}
		
		
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
				if (update) {
					m_service.delete(TN, m_requisitionNode.getForeignId(), m_requisitionNode.getInterface(this.primary));
					iface.setMonitoredServices(m_requisitionNode.getInterface(this.primary).getMonitoredServices());
				}
				m_requisitionNode.deleteInterface(this.primary);
			}
			if (update)
				m_service.add(TN, m_requisitionNode.getForeignId(),iface);
			m_requisitionNode.putInterface(iface);
			updateSnmpProfileOnServer(primary, this.snmpProfile);
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
	
	public void commit() {
		if (!update) {
			updateSnmpProfileOnServer(primary, snmpProfile);
			m_service.add(TN, m_requisitionNode);
			update=true;
		}
	}
}
