package org.opennms.vaadin.provision.core;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.model.BackupProfile;

public class DashBoardUtils {

	public static final String[] service_list = {
		"StrafePing",
		"HTTP",
		"HTTP-8080",
		"HTTP-8000",
		"HTTPS",
		"HypericAgent",
		"HypericHQ",
		"FTP",
		"Telnet",
		"DNS",
		"DHCP",
		"IMAP",
		"MSExchange",
		"SMTP",
		"POP3",
		"SSH",
		"MySQL",
		"SQLServer",
		"Oracle",
		"Postgres",
		"Router",
		"HP Insight Manager",
		"Dell-OpenManage",
		"NSClient",
		"NSClientpp",
		"NRPE",
		"NRPE-NoSSL",
		"Windows-Task-Scheduler",
		"OpenNMS-JVM",
		"HTTP-HostExample",
		"Memcached",
		"NTP",
		"NotesHTTP",
		"DominoIIOP",
		"Citrix",
		"LDAP",
		"SMB",
		"HTTP-MGMT",
		"Informix",
		"Sybase",
		"RadiusAuth",
		"JBoss4",
		"JBoss32",
		"JVM",
		"DiskUsage-root",
		"DiskUsage-home",
		"DiskUsage-CDrive",
		"DiskUsage-BootDisk",
		"NON-IP",
		"MAIL",
		"UnixTime",
		"MSExchangeSA",
		"MSExchangeADTopology",
		"MSExchangeAntispamUpdate",
		"MSExchangeEdgeSync",
		"MSExchangeFDS",
		"MSExchangeServiceHost",
		"MSExchangeTransport",
		"MSExchangeMailSubmission",
		"MSExchangeMailboxAssistants",
		"WMI",
		"XMP",
		"Postgres5433",
		"Postgres5434",
		"VPCC8002",
		"PattonSIPCalls",
		"testMount",
		"testLog",
		"testCaptive",
		"testTestONMS",
		"Rancid",
		"HTTPS8006",
		"HTTP-TrunkFastweb",
		"qvsQuota",
		"novasCr",
		"VLM4212",
		"VLM5555",
		"NVS8070",
		"NVA8071",
		"MDT8072",
		"qvsVlm",
		"PowerSupplyUnit1",
		"upIsdn",
		"PowerSupplyUnit2",
		"CiscoRedundantPowerSupply",
		"WebZimbraMail",
		"WebFast",
		"WebTnNet",
		"WebTIR",
		"WebTestBed",
		"WebTnnetHelp",
		"SVC",
		"XMPP",
		"MicronAccessControl",
		"Hyphen.Pincushion.Console.1",
		"Hyphen.Pincushion.Console.2",
		"Hyphen.Pincushion.Console.3",
		"btService",
		"btService01",
		"btService02",
		"WebPincushion",
		"CiscoPowerSupplyFailure",
		"CiscoPowerSupplyRedundant",
		"JDBCQueryMSSQL01",
		"JDBCQueryMSSQL02",
		"JDBCQueryPostgresSQL01",
		"JDBCQueryPostgresSQLDB01",
		"G3PIPPO4000",
		"SnmpWinet1000",
		"SnmpCabla1001",
		"GprsInCon4005",
		"X25CCEngine180",
		"OpenNmsBaculaChecker",
		"Bacula9101",
		"LDAPnovasCheck",
		"WebGRU",
		"WebAuge",
		"WebIntranet",
		"WebIpplan",
		"WebGIS",
		"WebDBFibre",
		"Process-squid",
		"Telnet3128",
		"SSH16622",
		"checkUnitn",
		"Telnet443",
		"NovasExprivia",
		"Process-rsyslogd",
		"RadiusAuthTestBed",
		"HTTP-9080",
		"Postgres5435"
	};

	public static final String[] m_network_levels = {
		"Backbone",
		"Distribuzione",
		"Accesso",
		"LAN"
	};

	public static final String[] m_notify_levels = {
		"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"
	};

	public static final String[] m_threshold_levels = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};
	
	public static final String[] m_sla_levels = {
		"noApssSlaReport",
		"noMittSlaReport",
		"noPatSlaReport"
	};
	
	public static final String[][] m_server_levels = {
		{"VirtualServer","VmVz"},
		{"VirtualServer","VmKVM"},
		{"VirtualServer","Docker"},
		{"VirtualServer","VmLXC"},
		{"VirtualServer","VmVMWare"},
		{"PhysicalDevice","Proxmox"},
		{"PhysicalDevice","Storage"},
		{"PhysicalDevice","ESX"},
		{"PhysicalDevice","OpenStack"},
		{"PhysicalDevice","PDU"},
		{"PhysicalDevice","IPMA"}
	};	
	
	public static final String[][] m_server_managedby = {
		{"ServerSI","Managed By ICT"},
		{"ManagedByIngRete", "Managed By Servizi"}
	};

	public static final String[] m_server_notif = {
		"RepSI",
		"RepINS"
	};
	
	public static final String[] m_server_optional = {
		"Videoconferenza",
		"VideoconferenzaTerminali",
		"Videosorveglianza",
		"Voip",
		"Sicurezza",
		"Testbed",
		"Eworks"
	};

	public static final String[] m_server_prod = {
		"produzione",
		"sviluppo"
	};
	
	public static final String LABEL = "nodeLabel";

	public static final String SNMP_PROFILE    = "snmpProfile";
	public static final String BACKUP_PROFILE  = "backupProfile";
	

	public static final String SERVER_LEVEL_CATEGORY = "serverLevelCategory";
	public static final String SERVER_MANAGED_BY_CATEGORY   = "managedByCategory";
	public static final String SERVER_OPTIONAL_CATEGORY  = "optionalCategory";
	public static final String SERVER_PROD_CATEGORY  = "prodCategory";
	public static final String SERVER_TN_CATEGORY  = "trentinoNetworkCategory";
	public static final String NETWORK_CATEGORY = "networkCategory";
	public static final String NOTIF_CATEGORY   = "notifCategory";
	public static final String THRESH_CATEGORY  = "threshCategory";
	public static final String SLA_CATEGORY  = "slaCategory";

	public static final String DESCR = "descr";
	public static final String HOST = "hostname";
	public static final String PRIMARY = "primary";
	public static final String VRF = "vrf";
	public static final String PARENT = "parent";
	public static final String VALID = "valid";
	public static final String CITY    = "city";
	public static final String BUILDING_SCALAR = "building_scalar";

	public static final String DESCRIPTION = "description";
	public static final String ADDRESS1 = "address1";
	public static final String BUILDING = "building";
	public static final String CIRCUITID = "circuitId";
	
	public static final String LEASEEXPIRES = "leaseExpires" ;
	public static final String LEASE = "lease";
	public static final String VENDORPHONE = "vendorPhone";
	public static final String VENDOR = "vendor";
	public static final String SLOT = "slot";
	public static final String RACK = "rack";
	public static final String ROOM = "room";
	public static final String OPERATINGSYSTEM = "operatingSystem";
	public static final String DATEINSTALLED = "dateInstalled";
	public static final String ASSETNUMBER = "assetNumber";
	public static final String SERIALNUMBER = "serialNumber";
	public static final String CATEGORY = "category";
	public static final String MODELNUMBER = "modelNumber";
	public static final String MANUFACTURER = "manufacturer";


	public static final String m_fast_default_notify = "Default";
	
	public static boolean hasInvalidIp(String ip) {
		if (ip == null)
			return true;
		String re ="^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"; 
		if (!ip.matches(re))	
			return true;
		if (ip.equals("127.0.0.1"))
			return true;
		if (ip.equals("0.0.0.0"))
			return true;
		return false;
	}

	public static boolean hasInvalidDnsBind9Label(String nodelabel ) {
		if (nodelabel == null)
			return true;
		if (nodelabel.length() > 253)
			return true;
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split("\\.")) {
			if (!label.matches(re))	
				return true;
			if (label.length() > 63)
				return true;
		}
		return false;
	}
	
	public static boolean hasUnSupportedDnsDomain(String hostname, String nodelabel, List<String> sub_domains) {
		if (hostname == null)
			return true;
		if (hostname.contains(".")) {
			String hostlabel = hostname.substring(0,hostname.indexOf("."));
			for (String subdomain: sub_domains ) {
				if (nodelabel.equals(hostlabel+"."+subdomain)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
	public static boolean isValidNotifyLevel(String notifyLevel) {
		for (String validnotifyLevel: m_notify_levels) {
			if (validnotifyLevel.equals(notifyLevel))
				return true;
		}
		if (m_fast_default_notify.equals(notifyLevel))
			return true;
		return false;
	}
	
	public static PolicyWrapper getPolicyWrapper(String ipaddr) {
		PolicyWrapper manage = new PolicyWrapper();
    	manage.setName(getPolicyName(ipaddr));
    	manage.setPluginClass("org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy");
    	manage.addParameter("action", "MANAGE");
    	manage.addParameter("matchBehavior", "ALL_PARAMETERS");
    	manage.addParameter("ipAddress", "~^"+ipaddr+"$");		
    	return manage;
	}
	
	public static String getPolicyName(String ipaddr) {
		return "Manage"+ipaddr;
	}

	public static String getBackupProfile(RequisitionNode requisitionNode,Map<String,BackupProfile> backupprofilemap) {
		for (String profileId: backupprofilemap.keySet()) {
			RequisitionNode profile = backupprofilemap.get(profileId).getRequisitionAssets();
		
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
				return profileId;
			}
		}
		return null;
	}

	public static final String TN_REQU_NAME = "TrentinoNetwork";
	public static final String SI_REQU_NAME = "SI";
	public static final String SIVN_REQU_NAME = "SIVirtualNodes";

	public static final String MEDIAGATEWAY_CATEGORY = "MediaGateway";
	public static final String MEDIAGATEWAY_NETWORK_CATEGORY = "Accesso";

	public static boolean validNotifyCategory(String notifyCategory) {
		for (String validcategory:m_notify_levels) {
			if (validcategory.equals(notifyCategory))
				return true;
		}
		return false;
	}


}