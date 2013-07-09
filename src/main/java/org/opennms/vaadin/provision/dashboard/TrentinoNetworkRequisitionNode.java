package org.opennms.vaadin.provision.dashboard;

import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class TrentinoNetworkRequisitionNode {

	protected static final String DESCR = "descr";
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
	
	protected final static String[] m_snmp_profiles = {
		"winet_v2c",
		"bb_v2c",
		"?"
	};
	
	protected final static String[] m_backup_profiles = {
		"switch_winet",
		"router_winet",
		"aaa"
	};

	private RequisitionNode m_requisitionNode;

	protected String descr="Imported from Provision Dashboard";
	protected String hostname;
	protected String nodeLabel;
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
	
	@SuppressWarnings("unchecked")
	public TrentinoNetworkRequisitionNode(RequisitionNode requisitionNode) {
		m_requisitionNode = requisitionNode;
		nodeLabel = m_requisitionNode.getNodeLabel();

		parent = m_requisitionNode.getParentForeignId();
		//FIXME add snmpProfile set
		snmpProfile=m_snmp_profiles[0];

		//FIXME add backupprofile set
		backupProfile=m_backup_profiles[0];
		
		for (String vrf: m_vrfs) {
			if (m_requisitionNode.getNodeLabel().endsWith("."+vrf)) {
				this.vrf = vrf;
				hostname = m_requisitionNode.getNodeLabel().substring(0,m_requisitionNode.getNodeLabel().indexOf(vrf)-1);
				break;
			}
		}

		secondary.addContainerProperty("ip", String.class, null);
		for (RequisitionInterface ip: m_requisitionNode.getInterfaces()) {
			if (ip.getSnmpPrimary().equals("P")) {
				primary = ip.getIpAddr();
				descr = ip.getDescr();
			} else {
				Item ipItem = secondary.getItem(secondary.addItem());
				ipItem.getItemProperty("ip").setValue(ip.getIpAddr()); 
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

		city = requisitionNode.getCity();
		if (m_requisitionNode.getAsset(ADDRESS) != null)
			address1 = m_requisitionNode.getAsset(ADDRESS).getValue();
		
	}
	
	public String getParent() {
		return parent;
	}

	public void setParent(String parentForeignId) {
		this.parent = parentForeignId;
	}

	public String getNodeLabel() {
		return nodeLabel;
	}

	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String[] getNetworkCategory() {
		return networkCategory;
	}
	public void setNetworkCategory(String[] networkCategory) {
		this.networkCategory = networkCategory;
	}
	public String getNotifCategory() {
		return notifCategory;
	}
	public void setNotifCategory(String notifCategory) {
		this.notifCategory = notifCategory;
	}
	public String getThreshCategory() {
		return threshCategory;
	}
	public void setThreshCategory(String threshCategory) {
		this.threshCategory = threshCategory;
	}
	public String getVrf() {
		return vrf;
	}
	public void setVrf(String vrf) {
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

	public void setSecondary(IndexedContainer secondary) {
		this.secondary = secondary;
	}

	public String getSnmpProfile() {
		return snmpProfile;
	}

	public void setSnmpProfile(String snmpProfile) {
		this.snmpProfile = snmpProfile;
	}

	public String getBackupProfile() {
		return backupProfile;
	}

	public void setBackupProfile(String backupProfile) {
		this.backupProfile = backupProfile;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address) {
		this.address1 = address;
	}

	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		this.primary = primary;
	}

	public RequisitionNode getRequisitionNode() {
		return m_requisitionNode;
	}
	
	public void setRequisitionNode(RequisitionNode requisitionNode) {
		m_requisitionNode = requisitionNode;
	}
	
	
	
	
	
}
