package org.opennms.vaadin.provision.dashboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionForeignSourceService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;
import org.opennms.rest.client.JerseySnmpInfoService;
import org.opennms.rest.client.model.OnmsIpInterface;
import org.opennms.rest.client.model.OnmsNodeList;
import org.opennms.rest.client.snmpinfo.SnmpInfo;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class DashBoardService {
    private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());
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
		{"AccessPoint","Backbone","wl.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesspoint_essentia_ssh","wifless_v2"},
		{"Core","Backbone","bb.tnnet.it","EMERGENCY_F0","ThresholdWARNING","backbone_7","snmp_default_v2"},
		{"Ponte5p4","Backbone","wl.tnnet.it","EMERGENCY_F1","ThresholdWARNING","backbone_accesspoint_alvarion_tftp","public_v1"},
		{"PontePDH","Backbone","wl.tnnet.it","EMERGENCY_F1","ThresholdWARNING","backbone_accesso_switch_pat","public_v2"},
		{"SwitchWiNet","Backbone","wl.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_switch_alcatel_winet","wifless_v2"},
		{"Fiemme2013","Backbone","bb.tnnet.it","EMERGENCY_F0","ThresholdWARNING","backbone_2013","snmp_default_v2"},
		{"Universiade2013","Backbone","bb.tnnet.it","EMERGENCY_F0","ThresholdWARNING","backbone_2013","snmp_default_v2"},
		{"AgLav","Accesso","aglav.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Apss","Accesso","apss.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Biblio","Accesso","biblio.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"CPE","Accesso","wl.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesspoint_essentia_ssh","wifless_v2"},
		{"CUE","Accesso","cue.tnnet.it","EMERGENCY_F0","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ComuneTN","Accesso","comunetn.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v1"},
		{"Comuni","Accesso","comuni.tnnet.it","EMERGENCY_F4","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ConsPro","Accesso","conspro.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"GeoSis","Accesso","geosis.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Info","Accesso","info.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Internet","Accesso","internet.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Internet-Esterni","Accesso","internet-esterni.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v1"},
		{"LAN","Accesso","hq.tnnet.it","EMERGENCY_F0","ThresholdWARNING","lan","snmp_default_v2"},
		{"Medici","Accesso","medici.tnnet.it","INFORMATION","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Mitt","Accesso","mitt.tnnet.it","EMERGENCY_F1","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"OperaUnitn","Accesso","operaunitn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Pat","Accesso","pat.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"PatAcquePub","Accesso","patacquepub.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"PatDighe","Accesso","patdighe.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"PatVoce","Accesso","patvoce.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"RSACivicaTN","Accesso","rsacivicatn.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"RSASpes","Accesso","rsaspes.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"ReperibiliTnet","Accesso","reperibilitnet.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Scuole","Accesso","scuole.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ScuoleMaterne","Accesso","scuolematerne.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Telpat-Autonome","Accesso","telpat-autonome.tnnet.it","EMERGENCY_F4","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Unitn","Accesso","unitn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"VdsRovereto","Accesso","vdsrovereto.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Winwinet","Accesso","winwinet.tnnet.it","INFORMATION","ThresholdWARNING","accesso_radius","snmp_default_v2"}
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

	protected static final String LABEL = "nodeLabel";

	protected static final String PROPERTIES_FILE_PATH = "/etc/tomcat7/provision-dashboard.properties";
	protected static final String PROPERTIES_URLS_KEY = "urls";
	protected static final String PROPERTIES_URL_ROOT_KEY = "url.";
	protected static final String PROPERTIES_DB_URL_KEY = "db_url";
	protected static final String PROPERTIES_DB_USER_KEY = "db_username";
	protected static final String PROPERTIES_DB_PASS_KEY = "db_password";

	protected String[] URL_LIST = new String[] {
		"http://demo.arsinfo.it/opennms/rest",
		"http://localhost:8980/opennms/rest"
	};

	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
	private JerseyProvisionForeignSourceService m_foreignSourceService;
	private JerseyNodesService m_nodeService;
	private JerseySnmpInfoService m_snmpInfoService;

	private boolean snmpProfileLoaded = false;
	private boolean backupProfileLoaded = false;
	
	private Map<String,SnmpProfile> m_snmpProfiles;
	private SQLContainer m_backupProfiles;
	private Properties m_configuration = new Properties();
	
	private String m_username;
	private String m_url;
	private JDBCConnectionPool m_pool; 
	private Map<String,String> m_foreignIdNodeLabelMap;
	private Map<String,String> m_nodeLabelForeignIdMap;
	
	protected class SnmpProfile {
		final String community;
		final String version;
		final Integer timeout;
		SnmpInfo info;
		
		public SnmpProfile(Property<String> community, Property<String> version, Property<String> timeout) {
			super();
			this.community = community.getValue();
			this.version = version.getValue();
			this.timeout = Integer.parseInt(timeout.getValue());
			info = new SnmpInfo();
			info.setCommunity(this.community);
			info.setVersion(this.version);
			info.setTimeout(this.timeout);
		}

		public String getCommunity() {
			return community;
		}

		public String getVersion() {
			return version;
		}

		public Integer getTimeout() {
			return timeout;
		}		
		
		public SnmpInfo getSnmpInfo() {
			return info;
		}
	}
	
	public BeanContainer<String, TrentinoNetworkRequisitionNode> getRequisitionContainer(String foreignSource) {
		BeanContainer<String, TrentinoNetworkRequisitionNode> requisitionContainer = new BeanContainer<String, TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
		m_foreignIdNodeLabelMap = new HashMap<String, String>();
		m_nodeLabelForeignIdMap = new HashMap<String, String>();
		requisitionContainer.setBeanIdProperty(LABEL);
		for (RequisitionNode node : getRequisitionNodes(foreignSource).getNodes()) {
			requisitionContainer.addBean(new TrentinoNetworkRequisitionNode(node,this));
			m_foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
		}
		return requisitionContainer;
	}

	public DashBoardService() {
    	m_provisionService = new JerseyProvisionRequisitionService();
    	m_nodeService = new JerseyNodesService();
    	m_snmpInfoService = new JerseySnmpInfoService();
    	m_foreignSourceService = new JerseyProvisionForeignSourceService();
    	File file = new File(PROPERTIES_FILE_PATH);
    	if (file.exists() && file.isFile()) {
    		try {
    			m_configuration.load(new FileInputStream(file));
    			logger.info("Loaded Configuration file: " + PROPERTIES_FILE_PATH );
    		} catch (IOException ex) {
    			logger.log(Level.INFO, "Cannot load configuration file: ", ex );
    		}
  
    	}
    }
    
	public JerseyClientImpl getJerseyClient() {
		return m_jerseyClient;
	}

	public void setJerseyClient(JerseyClientImpl jerseyClient) {
		m_jerseyClient = jerseyClient;
	    m_nodeService.setJerseyClient(m_jerseyClient);
		m_provisionService.setJerseyClient(m_jerseyClient);
	    m_snmpInfoService.setJerseyClient(m_jerseyClient);
	    m_foreignSourceService.setJerseyClient(m_jerseyClient);
	}

	public Requisition getRequisitionNodes(String foreignSource) {		
		return m_provisionService.get(foreignSource);
	}

	public String getSnmpProfile(String ip) {
		SnmpInfo info = m_snmpInfoService.get(ip);
		for (Entry<String, SnmpProfile> snmpprofileentry :m_snmpProfiles.entrySet()) {
			if (info.getCommunity().equals(snmpprofileentry.getValue().getCommunity()) &&
				info.getVersion().equals(snmpprofileentry.getValue().getVersion()) &&	
				info.getTimeout() == snmpprofileentry.getValue().getTimeout().intValue())
				return snmpprofileentry.getKey();
		}
		return null;
	}

	public void setSnmpInfo(String ip, String snmpProfile) {
		m_snmpInfoService.set(ip, m_snmpProfiles.get(snmpProfile).getSnmpInfo());
	}

	public void update(String foreignSource, String foreignId, MultivaluedMap<String, String> map) {
		m_provisionService.update(foreignSource, foreignId, map);
	}
		
	public void destroy() {
		setJerseyClient(null);
		if (m_pool != null)
			m_pool.destroy();
	}
	
	public void logout() {
		logger.info("logged out user: " + getUsername() + "@" + getUrl());
		destroy();
	}
	
	public void login(String url, String username, String password) throws SQLException {
		logger.info("loggin user: " + username + "@" + url);
		setJerseyClient(
				new JerseyClientImpl(url,username,password));
		m_snmpInfoService.get("127.0.0.1");
		logger.info("logged in user: " + username + "@" + url);
		m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", getDbUrl(), getDbUsername(), getDbPassword());
		loadBackupProfiles();
		logger.info("connected to database: " + getDbUrl());
		m_username = username;
		m_url = url;
	}

	public void add(String foreignSource, String foreignid, RequisitionAsset asset) {
		m_provisionService.add(foreignSource, foreignid, asset);
	}

	public void add(String foreignSource, String foreignid, RequisitionCategory category) {
		m_provisionService.add(foreignSource, foreignid, category);
	}

	public void add(String foreignSource, String foreignid, RequisitionInterface riface) {
		m_foreignSourceService.addOrReplace(foreignSource, getPolicyWrapper(riface));
		m_provisionService.add(foreignSource, foreignid, riface);
	}

	private PolicyWrapper getPolicyWrapper(RequisitionInterface riface) {
		PolicyWrapper manage = new PolicyWrapper();
    	manage.setName(getName(riface));
    	manage.setPluginClass("org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy");
    	manage.addParameter("action", "MANAGE");
    	manage.addParameter("matchBehavior", "ALL_PARAMETERS");
    	manage.addParameter("ipAddress", "~^"+riface.getIpAddr()+"$");		
    	return manage;
	}

	public void add(String foreignSource, RequisitionNode node) {
		m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
		m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		m_provisionService.add(foreignSource, node);
		for (RequisitionInterface riface: node.getInterfaces())
			m_foreignSourceService.addOrReplace(foreignSource, getPolicyWrapper(riface));
	}

	public void delete(String foreignSource, RequisitionNode node) {
		m_foreignIdNodeLabelMap.remove(node.getForeignId());
		m_nodeLabelForeignIdMap.remove(node.getNodeLabel());
		m_provisionService.delete(foreignSource, node);
		for (RequisitionInterface riface: node.getInterfaces())
			m_foreignSourceService.deletePolicy(foreignSource, getName(riface));
	}

	public void delete(String foreignSource, String foreignId, RequisitionCategory category) {
		m_provisionService.delete(foreignSource, foreignId, category);
	}

	public void delete(String foreignSource, String foreignId, RequisitionInterface riface) {
		m_foreignSourceService.deletePolicy(foreignSource, getName(riface));
		m_provisionService.delete(foreignSource, foreignId, riface);
	}

	private String getName(RequisitionInterface riface) {
		return "Manage"+riface.getIpAddr();
	}

	@SuppressWarnings("deprecation")
	public Container getSnmpProfileContainer() throws SQLException {
        	List<String> primarykeys = new ArrayList<String>();
        	primarykeys.add("name");
			return new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles order by name", primarykeys,m_pool));
	}
	
	public Set<String> getSnmpProfiles() {
		return m_snmpProfiles.keySet();
	}
	
	public Container getBackupProfiles() {
		return m_backupProfiles;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public void loadSnmpProfiles() throws SQLException {
		
		if (snmpProfileLoaded)
			return;

    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		SQLContainer snmpProfileTable = new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
		m_snmpProfiles = new HashMap<String, DashBoardService.SnmpProfile>();
		logger.info("found iterating on snmp profiles");
		for (Iterator<?> i = snmpProfileTable.getItemIds().iterator(); i.hasNext();) {
			Item snmpprofiletableRow = snmpProfileTable.getItem(i.next());
			logger.info("found snmp profile name: " + snmpprofiletableRow.getItemProperty("name").getValue().toString());
			logger.info("found snmp profile community: " + snmpprofiletableRow.getItemProperty("community").getValue().toString());
			logger.info("found snmp profile version: " + snmpprofiletableRow.getItemProperty("version").getValue().toString());
			logger.info("found snmp profile timeout: " + snmpprofiletableRow.getItemProperty("timeout").getValue().toString());
			m_snmpProfiles.put(snmpprofiletableRow.getItemProperty("name").getValue().toString(),
					new SnmpProfile(snmpprofiletableRow.getItemProperty("community"), 
							snmpprofiletableRow.getItemProperty("version"), 
							snmpprofiletableRow.getItemProperty("timeout")));
		}
		snmpProfileLoaded = true;
		logger.info("loaded snmp profiles");
	}

	@SuppressWarnings("deprecation")
	public void loadBackupProfiles() throws SQLException {
		if (backupProfileLoaded)
			return;
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		m_backupProfiles = new SQLContainer(new FreeformQuery("select * from isi.asset_profiles", primarykeys,m_pool));
		backupProfileLoaded=true;
		logger.info("loaded backup profiles");
	}
	
	public String getNodeLabel(String foreignId) {
	   	return m_foreignIdNodeLabelMap.get(foreignId);
	}

	public String getForeignId(String nodeLabel) {
	   	return m_nodeLabelForeignIdMap.get(nodeLabel);
	}

	public Collection<String> getNodeLabels() {
		return m_foreignIdNodeLabelMap.values();
	}

	public Collection<String> getForeignIds() {
		return m_foreignIdNodeLabelMap.keySet();
	}
		
	public String[] getUrls() {
		List<String> urls = new ArrayList<String>();
		if (m_configuration.getProperty(PROPERTIES_URLS_KEY) != null ) {
			for (String urlKey: m_configuration.getProperty(PROPERTIES_URLS_KEY).split(",")) {
				String key = PROPERTIES_URL_ROOT_KEY+urlKey;
				if (m_configuration.getProperty(key) != null)
					urls.add(m_configuration.getProperty(key));
			}
		}
		if (urls.size() > 0)
			return urls.toArray(new String[urls.size()]);
		else
			return URL_LIST;
	}

	public String getDbUrl() {
		if (m_configuration.getProperty(PROPERTIES_DB_URL_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_URL_KEY);
		else 
			return "jdbc:postgresql://172.25.200.36:5432/tnnet";
	}
	
	public String getDbUsername() {
		if (m_configuration.getProperty(PROPERTIES_DB_USER_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_USER_KEY);
		else 
			return "isi_writer";
	}
	
	public String getDbPassword() {
		if (m_configuration.getProperty(PROPERTIES_DB_PASS_KEY) != null ) 
			return m_configuration.getProperty(PROPERTIES_DB_PASS_KEY);
		else 
			return "Oof6Eezu";
		
	}
	public List<String> getIpAddresses(String foreignSource,String nodelabel) {
	   	List<String> ipaddresses = new ArrayList<String>();
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
    	queryParams.add("label", nodelabel);
    	queryParams.add("foreignSource", foreignSource);

		OnmsNodeList nodes = m_nodeService.find(queryParams);
		if (nodes.isEmpty())
			return ipaddresses; 
        for (OnmsIpInterface ipinterface: m_nodeService.getIpInterfaces(m_nodeService.find(queryParams).getFirst().getId())){
        	ipaddresses.add(ipinterface.getIpAddress());
        }
        return ipaddresses;
	}

	public void checkUniqueNodeLabel() {
		Set<String> labels = new HashSet<String>();
		Set<String> duplicated = new HashSet<String>();
		for (String label: getNodeLabels()) {
			if (labels.contains(label)) {
				duplicated.add(label);
			} else {
				labels.add(label);
			}
		}
		if (!duplicated.isEmpty()) {
			logger.warning(" Found Duplicated NodeLabel: " + Arrays.toString(duplicated.toArray()));
			Notification.show("Found Duplicated NodeLabel",  Arrays.toString(duplicated.toArray()), Type.WARNING_MESSAGE);
		}
	}
	
	public void checkUniqueForeignId() {
		Set<String> labels = new HashSet<String>();
		Set<String> duplicated = new HashSet<String>();
		for (String label: getForeignIds()) {
			if (labels.contains(label)) {
				duplicated.add(label);
			} else {
				labels.add(label);
			}
		}
		if (!duplicated.isEmpty()) {
			logger.warning(" Found Duplicated ForeignId: " + Arrays.toString(duplicated.toArray()));
			Notification.show("Found Duplicated ForeignId",  Arrays.toString(duplicated.toArray()), Type.WARNING_MESSAGE);
		}
	}
	
	public boolean hasDuplicatedForeignId(String hostname) {
		for (String label: getForeignIds()) {
			if (label.equals(hostname)) 
				return true;
		}
		return false;
	}

	public boolean hasDuplicatedNodelabel(String nodelabel) {
		for (String label: getNodeLabels()) {
			if (label.equals(nodelabel)) 
				return true;
		}
		return false;
	}
	
	public static boolean hasUnSupportedDnsDomain(String hostname, String nodelabel) {
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

	public static boolean hasInvalidDnsBind9Size(String nodelabel ) {		
		if (nodelabel.length() > 253)
			return true;
		return false;
	}

	public static boolean hasInvalidDnsBind9LabelSize(String nodelabel ) {		
		for (String label: nodelabel.split("\\.")) {
			if (label.length() > 63)
				return true;
		}
		return false;
	}

	public static boolean hasInvalidDnsBind9Label(String nodelabel ) {		
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split("\\.")) {
			if (!label.matches(re))	
				return true;
		}
		return false;
	}


	public String getUsername() {
		return m_username;
	}


	public void setUsername(String username) {
		m_username = username;
	}


	public String getUrl() {
		return m_url;
	}


	public void setUrl(String url) {
		m_url = url;
	}

    public String[] getDefaultValuesFromNetworkCategory(Object networkcategory) {
    	if (networkcategory == null)
        	return m_network_categories[0];
    	String[] netcat = (String[]) networkcategory;
    	for (int i = 0; i< m_network_categories.length;i++) {
    	   	if (netcat[0].equals(m_network_categories[i][0]) &&
        			netcat[1].equals(m_network_categories[i][1]))
    	    	return m_network_categories[i];
    	}
    	return m_network_categories[0];
    }


}
