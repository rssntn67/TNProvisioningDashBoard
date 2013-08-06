package org.opennms.vaadin.provision.dashboard;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;

public class DashboardService {
    
	protected static final String LABEL = "nodeLabel";

	protected static final String PROPERTIES_FILE_PATH = "/etc/tomcat7/provision-dashboard.properties";
	protected static final String PROPERTIES_URLS_KEY = "urls";
	protected static final String PROPERTIES_URL_ROOT_KEY = "url";
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
	private boolean requisitionNodeLoaded = false;
	private boolean backupProfileLoaded = false;
	
	private SQLContainer m_snmpProfiles;
	private SQLContainer m_backupProfiles;
	private Properties m_configuration = new Properties();

	private BeanContainer<String, TrentinoNetworkRequisitionNode> m_requisitionContainer = new BeanContainer<String, TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);

	public BeanContainer<String, TrentinoNetworkRequisitionNode> getRequisitionContainer() {
		return m_requisitionContainer;
	}

	public void setRequisitionContainer(
			BeanContainer<String, TrentinoNetworkRequisitionNode> requisitionContainer) {
		m_requisitionContainer = requisitionContainer;
	}

	private JDBCConnectionPool m_pool; 
   
	public DashboardService() {
    	m_provisionService = new JerseyProvisionRequisitionService();
    	m_nodeService = new JerseyNodesService();
    	m_snmpInfoService = new JerseySnmpInfoService();
    	m_foreignSourceService = new JerseyProvisionForeignSourceService();
    	File file = new File(PROPERTIES_FILE_PATH);
    	if (file.exists() && file.isFile()) {
    		try {
    			m_configuration.load(new FileInputStream(file));
    		} catch (IOException ex) {
    			System.out.println("No configuration file found using default");
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

	public SnmpInfo getSnmpInfo(String ip) {
		return m_snmpInfoService.get(ip);
	}

	public void setSnmpInfo(String ip, SnmpInfo info) {
		m_snmpInfoService.set(ip, info);
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
		destroy();
	}
	
	public void login(String url, String username, String password) throws SQLException {
		setJerseyClient(
				new JerseyClientImpl(url,username,password));
		m_snmpInfoService.get("127.0.0.1");
		m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", getDbUrl(), getDbUsername(), getDbPassword());
		loadSnmpProfiles();
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
		m_provisionService.add(foreignSource, node);
		for (RequisitionInterface riface: node.getInterfaces())
			m_foreignSourceService.addOrReplace(foreignSource, getPolicyWrapper(riface));
	}

	public void delete(String foreignSource, RequisitionNode node) {
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

	public void setSnmpProfiles(SQLContainer snmpProfiles) {
		m_snmpProfiles = snmpProfiles;
	}

	public void setBackupProfiles(SQLContainer backupProfiles) {
		m_backupProfiles = backupProfiles;
	}

	public Container getSnmpProfiles() {
		return m_snmpProfiles;
	}
	
	public Container getBackupProfiles() {
		return m_backupProfiles;
	}

	@SuppressWarnings("deprecation")
	public void loadSnmpProfiles() throws SQLException {
		
		if (snmpProfileLoaded)
			return;
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		m_snmpProfiles = new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
		snmpProfileLoaded = true;
	}

	@SuppressWarnings("deprecation")
	public void loadBackupProfiles() throws SQLException {
		if (backupProfileLoaded)
			return;
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		m_backupProfiles = new SQLContainer(new FreeformQuery("select * from isi.asset_profiles", primarykeys,m_pool));
		backupProfileLoaded=true;
	}
	
	public void loadProvisionNode(String foreignSource) {
		if (requisitionNodeLoaded)
			return;
		m_requisitionContainer.setBeanIdProperty(LABEL);
		for (RequisitionNode node : getRequisitionNodes(foreignSource).getNodes()) {
			m_requisitionContainer.addBean(new TrentinoNetworkRequisitionNode(node,this));
		}
		requisitionNodeLoaded = true;
	}

	public List<String> getForeignIds() {
	   	List<String> foreignids = new ArrayList<String>();
	   	for (Object itemId: m_requisitionContainer.getItemIds()) {
	   		if (m_requisitionContainer.getItem(itemId).getBean().getRequisitionNode().getForeignId() != null)
	   			foreignids.add(m_requisitionContainer.getItem(itemId).getBean().getRequisitionNode().getForeignId());
	   	}
	   	return foreignids;
	}

	public List<String> getNodeLabels() {
	   	List<String> nodelabels = new ArrayList<String>();
	   	for (Object itemId: m_requisitionContainer.getItemIds()) {
	   		nodelabels.add(m_requisitionContainer.getItem(itemId).getBean().getNodeLabel());
	   	}
	   	return nodelabels;
	}

	public String[] getUrls() {
		List<String> urls = new ArrayList<String>();
		if (m_configuration.getProperty(PROPERTIES_URLS_KEY) != null ) {
			for (String urlKey: m_configuration.getProperty(PROPERTIES_URLS_KEY).split(",")) {
				String key = PROPERTIES_URL_ROOT_KEY+"."+urlKey;
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


}
