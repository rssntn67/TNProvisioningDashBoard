package org.opennms.vaadin.provision.dashboard;

import java.util.Arrays;
import java.util.logging.Logger;

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

	private static final Logger logger = Logger.getLogger(TrentinoNetworkRequisitionNode.class.getName());
	
	protected static final String DESCR = "descr";
	protected static final String DESCRIPTION = "description";
	protected static final String HOST = "hostname";
	protected static final String PRIMARY = "primary";
	protected static final String VRF = "vrf";
	protected static final String PARENT = "parent";
	protected static final String VALID = "valid";
	
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
	
	protected boolean valid = true;
	
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

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
		if (networkCategory != null && Arrays.equals(this.networkCategory,networkCategory))
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
		if (notifCategory != null && notifCategory.equals(this.notifCategory))
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
		if (threshCategory != null && threshCategory.equals(this.threshCategory))
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
		if (this.vrf != null && this.vrf.equals(vrf))
			return;
		if (vrf != null && hostname != null ) {
			String nodelabel=hostname + "." + vrf;
			updateNodeLabel(nodelabel, hostname);
		}
		this.vrf = vrf;
	}

	private void updateNodeLabel(String nodelabel, String hostname) throws ProvisionDashboardValidationException {
		if	(hasInvalidDnsBind9Size(nodelabel))
			throw new ProvisionDashboardValidationException("Bind9 error: The full domain name exceed a total length of 253: " + nodelabel);
		if (hasInvalidDnsBind9LabelSize(nodelabel))
			throw new ProvisionDashboardValidationException("Bind9 error: dns label contains more then 63 characters: " + nodelabel);
		if (hasInvalidDnsBind9Label(nodelabel))
			throw new ProvisionDashboardValidationException("Bind9 error: dns label does not contain only a-zA-Z0-9 characters or start or end with hypen: " + nodelabel);
		if (hasUnSupportedDnsDomain(hostname, nodelabel))
			throw new ProvisionDashboardValidationException("There is no dns domain defined for: " + nodelabel);
		if (hasDuplicatedNodelabel(nodelabel))
			throw new ProvisionDashboardValidationException("The node label exist: cannot duplicate node label: " + nodelabel);
		m_requisitionNode.setNodeLabel(nodelabel);
		if (update) {
			MultivaluedMap<String, String> map = new MultivaluedMapImpl();
			map.add("node-label", nodelabel);
			m_service.update(TN, m_requisitionNode.getForeignId(), map);
		}
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
		if (snmpProfile != null)
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
		if (snmpProfile == null)
			valid = false;
		logger.info("checked snmpProfile: " + snmpProfile +" valid: " + valid);

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
		if (hostname != null && vrf != null) {
			String nodelabel=hostname + "." + vrf;
			updateNodeLabel(nodelabel, hostname);
		}
		this.hostname = hostname;
	}

	private boolean hasDuplicatedForeignId(String hostname) {
		for (String label: m_service.getForeignIds()) {
			if (label.equals(hostname)) 
				return true;
		}
		return false;
	}

	private boolean hasDuplicatedNodelabel(String nodelabel) {
		for (String label: m_service.getNodeLabels()) {
			if (label.equals(nodelabel)) 
				return true;
		}
		return false;
	}
	
	private boolean hasUnSupportedDnsDomain(String hostname, String nodelabel) {
		if (hostname.contains(".")) {
			String hostlabel = hostname.substring(0,hostname.indexOf("."));
			for (String subdomain: m_sub_domains ) {
				if (nodelabel.equals(hostlabel+"."+subdomain)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	private boolean hasInvalidDnsBind9Size(String nodelabel ) {		
		if (nodelabel.length() > 253)
			return true;
		return false;
	}

	private boolean hasInvalidDnsBind9LabelSize(String nodelabel ) {		
		for (String label: nodelabel.split("\\.")) {
			if (label.length() > 63)
				return true;
		}
		return false;
	}

	private boolean hasInvalidDnsBind9Label(String nodelabel ) {		
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split("\\.")) {
			if (!label.matches(re))	
				return true;
		}
		return false;
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
	
	public void commit() throws ProvisionDashboardValidationException {
		if (!update) {
			if (!hasDuplicatedForeignId(hostname))
							throw new ProvisionDashboardValidationException("The foreign id exist: cannot duplicate foreignId: " + hostname);
			m_requisitionNode.setForeignId(hostname);
			updateSnmpProfileOnServer(primary, snmpProfile);
			m_service.add(TN, m_requisitionNode);
			update=true;
		}
	}
}
