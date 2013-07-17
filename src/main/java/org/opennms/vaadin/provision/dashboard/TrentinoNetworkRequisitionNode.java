package org.opennms.vaadin.provision.dashboard;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.SnmpInfo;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class TrentinoNetworkRequisitionNode {

	protected static final String DESCR = "descr";
	protected static final String DESCRIPTION = "description";
	protected static final String HOST = "hostname";
	protected static final String LABEL = "nodeLabel";
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
	
	protected static final String[] m_domains = {
		"aglav.tnnet.it",
		"alv01.wl.tnnet.it",
		"alv02.wl.tnnet.it",
		"alv03.wl.tnnet.it",
		"alv04.wl.tnnet.it",
		"alv05.wl.tnnet.it",
		"alv06.wl.tnnet.it",
		"apss.tnnet.it",
		"asw01.wl.tnnet.it",
		"bb.tnnet.it",
		"biblio.tnnet.it",
		"cavalese-l3.pat.tnnet.it",
		"comunetn.tnnet.it",
		"comuni.tnnet.it",
		"conspro.tnnet.it",
		"cpe01.biblio.tnnet.it",
		"cpe01.pat.tnnet.it",
		"cpe01.patacquepub.tnnet.it",
		"cpe01.scuole.tnnet.it",
		"cpe01.wl.tnnet.it",
		"cue.tnnet.it",
		"ess01.wl.tnnet.it",
		"ess02.wl.tnnet.it",
		"ess03.wl.tnnet.it",
		"ess04.wl.tnnet.it",
		"ess05.wl.tnnet.it",
		"ess06.wl.tnnet.it",
		"ess07.wl.tnnet.it",
		"ess08.wl.tnnet.it",
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
		"mtk01.reperibilitnet.tnnet.it",
		"mtr01.wl.tnnet.it",
		"operaunitn.tnnet.it",
		"pat.tnnet.it",
		"patacquepub.tnnet.it",
		"patdighe.tnnet.it",
		"patvoce.tnnet.it",
		"reperibilitnet.tnnet.it",
		"rsacivicatn.tnnet.it",
		"rsaspes.tnnet.it",
		"scuole.tnnet.it",
		"sw01.bb.tnnet.it",
		"sw02.bb.tnnet.it",
		"telpat-autonome.tnnet.it",
		"uby.wl.tnnet.it",
		"unitn.tnnet.it",
		"vdsrovereto.tnnet.it",
		"winwinet.tnnet.it",
		"wl.tnnet.it"
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
	
	protected final static String[][] m_snmp_profiles = {
		{"FP1"          ,"*frizen*"        ,"v1" ,"5000"},
		{"FP2"          ,"frizen3"         ,"v1" ,"5000"},
		{"IP1"          ,"Ieb5Saitoe3oofiG","v2c","5000"},
		{"WP_v1_3000"   ,"WL01tnes02ro"    ,"v1" ,"3000"},
		{"WP_v1_5000"   ,"WL01tnes02ro"    ,"v1" ,"5000"},
		{"WP_v2c_3000"  ,"WL01tnes02ro"    ,"v2c","3000"},
		{"WP_v2c_5000"  ,"WL01tnes02ro"    ,"v2c","5000"},
		{"AP1"          ,"admin"           ,"v2c","3000"},
		{"CP_v1"        ,"cisco"           ,"v1" ,"3000"},
		{"CP_v2c"       ,"cisco"           ,"v2c","3000"},
		{"IP"           ,"infotnro"        ,"v2c","5000"},
		{"MT1_v1_1800"  ,"mT9gq"           ,"v1" ,"1800"},
		{"MT2_v1_5000"  ,"mt9gq"           ,"v1" ,"5000"},
		{"MP_v1_1800"   ,"ma14na165ge"     ,"v1" ,"1800"},
		{"MP_v1_3000"   ,"ma14na165ge"     ,"v1" ,"3000"},
		{"MP_v1_5000"   ,"ma14na165ge"     ,"v1" ,"5000"},
		{"MP_v1_8000"   ,"ma14na165ge"     ,"v1" ,"8000"},
		{"MP_v2c_1800"  ,"ma14na165ge"     ,"v2c","1800"},
		{"MP_v2c_3000"  ,"ma14na165ge"     ,"v2c","3000"},
		{"MP_v2c_5000"  ,"ma14na165ge"     ,"v2c","5000"},
		{"KP_v1_5000"   ,"ma918na2116ge"   ,"v1" ,"5000"},
		{"KP_v2c_1800"  ,"ma918na2116ge"   ,"v2c","1800"},
		{"KP_v2c_3000"  ,"ma918na2116ge"   ,"v2c","3000"},
		{"KP_v2c_5000"  ,"ma918na2116ge"   ,"v2c","5000"},
		{"PP1_v1"       ,"private"         ,"v1" ,"5000"},
		{"PP2_v1"       ,"public"          ,"v1" ,"5000"},
		{"PP2_v2c"      ,"public"          ,"v2c","5000"},
		{"PP3_v2c"      ,"public"          ,"v2c","1800"},
		{"VP_10000"     ,"verVZserVZ12"    ,"v2c","10000"},
		{"VP_1800"      ,"verVZserVZ12"    ,"v2c","1800"},
		{"VP_3000"      ,"verVZserVZ12"    ,"v2c","3000"},
		{"VP_5000"      ,"verVZserVZ12"    ,"v2c","5000"},
		{"WIP_v1_3000"  ,"w732pub"         ,"v1" ,"3000"},
		{"WIP_v1_5000"  ,"w732pub"         ,"v1" ,"5000"},
		{"WIP_v2c_5000" ,"w732pub"         ,"v2c","5000"},
		{"WLP"          ,"wl01tnes02ro"    ,"v2c","5000"}
	};
	
	protected final static String[][] m_backup_profiles = {
		{"A1","admin","default","notused","A","ssh"},
		{"A2","admin","default","notused","A","telnet"},
		{"B1","backupradius","torrone_morbido","notused","A","telnet"},
		{"I1","itn","civ27itn","civ27ena","","telnet"},
		{"I2","itn","civ27itn","notused","A","telnet"},
		{"H1","notused","ena21apss","notused","A","http"},
		{"O1","operator","o","notused","A","telnet"},
		{"O2","operator","tn07patnet","","","telnet"},
		{"R1","root","default","notused","A","ssh"},
		{"R2","root","default","notused","A","telnet"},
		{"T1","tnnepat07","tn07patnet","","","telnet"},
		{"T2","tnnepat07","tn07patnet","notused","A","telnet"},
		{"T3","tnnet","tnet01lan08","notused","A","telnet"},
		{"T4","tnnet","tnet2013!","","","telnet"},
		{"T5","tnnetbk07","bknet09tn","notused","A","telnet"},
		{"T6","tnnetbk07","bknet09tn","tn07enbknet","","telnet"},
		{"T7","tnnetbk07","tn07bknet","notused","A","telnet"},
		{"T8","tnnetbk07","tn07bknet","tn07enbknet","","telnet"},
		{"T9","tnnetpat07","tn07patnet","ena25pat","","telnet"},
		{"T10","tnnetpat07","tn07patnet","notused","A","telnet"},
		{"U1","user","cisco","notused","A","telnet"},
		{"U2","user","pass","enapass","","telnet"},
		{"U3","user","pass","notused","A","telnet"},
		{"U4","user","public","mT9gq","","tftp"},
		{"U5","user","public","notused","A","telnet"},
		{"U6","user","public","private","","tftp"},
		{"U7","user","tn07patnet","enapass","","telnet"},
		{"U8","user","tn07patnet","notused","A","telnet"},
		{"U9","user","torrone_morbido","enapass","","telnet"},
		{"U10","user","torrone_morbido","notused","A","telnet"}	
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

	protected String snmpProfile;
	protected String backupProfile;

	protected String city;
	protected String address1;
	
	private DashboardService m_service;
	@SuppressWarnings("unchecked")
	public TrentinoNetworkRequisitionNode(RequisitionNode requisitionNode, DashboardService service) {
		m_service = service;
		//FIXME add validation
		//FIXME use external source for profiles snmp and backup
		m_requisitionNode = requisitionNode;

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

		for (String[] profile: m_backup_profiles) {
			if (m_requisitionNode.getAsset("username") != null && m_requisitionNode.getAsset("username").getValue().equals(profile[1]) 
		     && m_requisitionNode.getAsset("password") != null && m_requisitionNode.getAsset("password").getValue().equals(profile[2])
			 && m_requisitionNode.getAsset("enable") != null && m_requisitionNode.getAsset("enable").getValue().equals(profile[3])
			 && m_requisitionNode.getAsset("autoenable") != null && m_requisitionNode.getAsset("autoenable").getValue().equals(profile[4])
			 && m_requisitionNode.getAsset("connection") != null && m_requisitionNode.getAsset("connection").getValue().equals(profile[5])
					) {
				backupProfile=profile[0];
				break;
			}
		}
		
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
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		map.add("parent-foreign-id", parentForeignId);
		m_service.update(TN, m_requisitionNode.getForeignId(), map);
		m_requisitionNode.setParentForeignId(parentForeignId);
		this.parent = parentForeignId;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		if (this.city != null && this.city.equals(city))
			return;
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		map.add(CITY, city);
		m_service.update(TN, m_requisitionNode.getForeignId(), map);
		m_requisitionNode.setCity(city);
		this.city = city;
		
		RequisitionAsset assetdescription = new RequisitionAsset(DESCRIPTION, city + " - " + address1);
		m_service.add(TN, m_requisitionNode.getForeignId(), assetdescription);
		m_requisitionNode.putAsset(assetdescription);
	}

	public String[] getNetworkCategory() {
		return networkCategory;
	}
	public void setNetworkCategory(String[] networkCategory) {
		if (this.networkCategory == networkCategory)
			return;
		for (String cat: this.networkCategory) {
			RequisitionCategory category = new RequisitionCategory(cat);
			m_service.delete(TN, m_requisitionNode.getForeignId(), category);
			m_requisitionNode.deleteCategory(category);
		}
		for (String cat: networkCategory) {
			RequisitionCategory category = new RequisitionCategory(cat);
			m_service.add(TN, m_requisitionNode.getForeignId(), category);
			m_requisitionNode.putCategory(category);
		}
		this.networkCategory = networkCategory;
	}
	public String getNotifCategory() {
		return notifCategory;
	}
	public void setNotifCategory(String notifCategory) {
		if (this.notifCategory == notifCategory )
			return;
		RequisitionCategory category = new RequisitionCategory(this.notifCategory);
		m_service.delete(TN, m_requisitionNode.getForeignId(), category);
		m_requisitionNode.deleteCategory(category);
		
		RequisitionCategory newcategory = new RequisitionCategory(notifCategory);
		m_service.add(TN, m_requisitionNode.getForeignId(), newcategory);
		m_requisitionNode.putCategory(newcategory);
		
		this.notifCategory = notifCategory;
	}
	public String getThreshCategory() {
		return threshCategory;
	}
	public void setThreshCategory(String threshCategory) {
		if (this.threshCategory == threshCategory )
			return;
		RequisitionCategory category = new RequisitionCategory(this.threshCategory);
		m_service.delete(TN, m_requisitionNode.getForeignId(), category);
		m_requisitionNode.deleteCategory(category);
		
		RequisitionCategory newcategory = new RequisitionCategory(threshCategory);
		m_service.add(TN, m_requisitionNode.getForeignId(), newcategory);
		m_requisitionNode.putCategory(newcategory);
		this.threshCategory = threshCategory;
	}
	public String getVrf() {
		return vrf;
	}
	public void setVrf(String vrf) {
		if (this.vrf != null && this.vrf == vrf)
			return;
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		String nodelabel=hostname + "." + vrf;
		map.add("node-label", nodelabel);
		m_service.update(TN, m_requisitionNode.getForeignId(), map);
		m_requisitionNode.setNodeLabel(nodelabel);

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
		if (this.snmpProfile == snmpProfile)
			return;
		updateSnmpProfileOnServer(this.primary, snmpProfile);
		this.snmpProfile = snmpProfile;
	}
	
	private void updateSnmpProfileOnServer(String ip,String snmp) {
		for (String[] profile: m_snmp_profiles) {
			if (profile[0] == snmp) {
				SnmpInfo info = new SnmpInfo();
				info.setCommunity(profile[1]);
				info.setVersion(profile[2]);
				info.setTimeout(Integer.parseInt(profile[3]));
				m_service.setSnmpInfo(ip, info);
				break;
			}
		}
	}
	public void updateSnmpProfile(SnmpInfo info) {
		for (String[] snmpdata: m_snmp_profiles) {
			if (info.getRetries() == 1 && info.getPort() == 161 
			  && info.getCommunity().equals(snmpdata[1]) 
			  && info.getVersion().equals(snmpdata[2])
			  && info.getTimeout() == Integer.parseInt(snmpdata[3])) {
				snmpProfile=snmpdata[0];
				break;
			}
		}
	}
	public String getBackupProfile() {
		return backupProfile;
	}
	public void setBackupProfile(String backupProfile) {
		if (this.backupProfile == backupProfile)
			return;
		for (String[] backup: m_backup_profiles) {
			if (backup[0] == backupProfile) {
				RequisitionAsset username = new RequisitionAsset("username", backup[1]);
				m_service.add(TN, m_requisitionNode.getForeignId(), username);
				m_requisitionNode.putAsset(username);
				RequisitionAsset password = new RequisitionAsset("password", backup[2]);
				m_service.add(TN, m_requisitionNode.getForeignId(), password);
				m_requisitionNode.putAsset(password);
				RequisitionAsset enable = new RequisitionAsset("enable", backup[3]);
				m_service.add(TN, m_requisitionNode.getForeignId(), enable);
				m_requisitionNode.putAsset(enable);
				RequisitionAsset autoenable = new RequisitionAsset("autoenable", backup[4]);
				m_service.add(TN, m_requisitionNode.getForeignId(), autoenable);
				m_requisitionNode.putAsset(autoenable);
				RequisitionAsset connection = new RequisitionAsset("connection", backup[5]);
				m_service.add(TN, m_requisitionNode.getForeignId(), connection);
				m_requisitionNode.putAsset(connection);
				break;
			}
		}
		this.backupProfile = backupProfile;
	}
	public String getAddress1() {
		return address1;
	}
	public void setAddress1(String address) {
		if (this.address1.equals(address))
			return;
		RequisitionAsset asset = new RequisitionAsset(ADDRESS, address);
		m_service.add(TN, m_requisitionNode.getForeignId(), asset);
		this.address1 = address;
		m_requisitionNode.putAsset(asset);
		RequisitionAsset assetdescription = new RequisitionAsset(DESCRIPTION, city + " - " + address);
		m_service.add(TN, m_requisitionNode.getForeignId(), assetdescription);
		m_requisitionNode.putAsset(assetdescription);
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		if (this.hostname != null && this.hostname == hostname)
			return;
		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		String nodelabel=hostname + "." + vrf;
		map.add("node-label", nodelabel);
		m_service.update(TN, m_requisitionNode.getForeignId(), map);
		m_requisitionNode.setNodeLabel(nodelabel);
		this.hostname = hostname;
	}
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		if (this.primary != null && this.primary.equals(primary))
			return;
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary("P");
		iface.setIpAddr(primary);
		iface.setMonitoredServices(m_requisitionNode.getInterface(this.primary).getMonitoredServices());
		iface.setDescr("Provided by Provision Dashboard");
		
		setDescr("Provided by Provision Dashboard");
		m_service.delete(TN, m_requisitionNode.getForeignId(), m_requisitionNode.getInterface(this.primary));
		m_requisitionNode.deleteInterface(this.primary);
		m_service.add(TN, m_requisitionNode.getForeignId(),iface);
		m_requisitionNode.putInterface(iface);
		
		updateSnmpProfileOnServer(primary, this.snmpProfile);

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
}
