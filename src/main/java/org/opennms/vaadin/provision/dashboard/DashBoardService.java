package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;
import org.opennms.rest.client.JerseySnmpInfoService;
import org.opennms.rest.client.model.OnmsIpInterface;
import org.opennms.rest.client.model.OnmsNodeList;
import org.opennms.rest.client.snmpinfo.SnmpInfo;

import com.vaadin.data.Container;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;

public class DashboardService {
    
	protected String[] URL_LIST = new String[] {
		"http://demo.arsinfo.it/opennms/rest",
		"http://demo.arsinfo.it:8980/opennms/rest",
		"http://localhost:8980/opennms/rest"
	};

	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
	private JerseyNodesService m_nodeService;
	private JerseySnmpInfoService m_snmpInfoService;

	private boolean loggedin = false;
	private SQLContainer m_snmpProfiles;
	private SQLContainer m_backupProfiles;

	private JDBCConnectionPool m_pool; 
   
	public DashboardService() {
    	m_provisionService = new JerseyProvisionRequisitionService();
    	m_nodeService = new JerseyNodesService();
    	m_snmpInfoService = new JerseySnmpInfoService();
    }
    
	public JerseyClientImpl getJerseyClient() {
		return m_jerseyClient;
	}

	public void setJerseyClient(JerseyClientImpl jerseyClient) {
		m_jerseyClient = jerseyClient;
	    m_nodeService.setJerseyClient(m_jerseyClient);
		m_provisionService.setJerseyClient(m_jerseyClient);
	    m_snmpInfoService.setJerseyClient(m_jerseyClient);
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
	
	public List<OnmsIpInterface> getIpInterfaces(MultivaluedMap<String, String> queryParams) {
		OnmsNodeList nodes = m_nodeService.find(queryParams);
		if (nodes.isEmpty())
			return new ArrayList<OnmsIpInterface>();
        return m_nodeService.getIpInterfaces(m_nodeService.find(queryParams).getFirst().getId());
	}
	public void destroy() {
		setJerseyClient(null);
		m_pool.destroy();
	}
	
	public void logout() {
		destroy();
		loggedin = false;
	}
	
	public void login(String url, String username, String password) throws SQLException {
		setJerseyClient(
				new JerseyClientImpl(url,username,password));
		m_snmpInfoService.get("127.0.0.1");
		m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", "jdbc:postgresql://172.25.200.36:5432/tnnet", "isi_writer", "Oof6Eezu");
		loadSnmpProfiles();
		loggedin = true;
	}

	public void add(String foreignSource, String foreignid, RequisitionAsset asset) {
		m_provisionService.add(foreignSource, foreignid, asset);
	}

	public void add(String foreignSource, String foreignid, RequisitionCategory category) {
		m_provisionService.add(foreignSource, foreignid, category);
	}

	public void add(String foreignSource, String foreignid, RequisitionInterface riface) {
		m_provisionService.add(foreignSource, foreignid, riface);
	}

	public void add(String foreignSource, RequisitionNode node) {
		m_provisionService.add(foreignSource, node);
	}

	public void delete(String foreignSource, RequisitionNode node) {
		m_provisionService.delete(foreignSource, node);
	}

	public void delete(String foreignSource, String foreignId, RequisitionCategory category) {
		m_provisionService.delete(foreignSource, foreignId, category);
	}

	public void delete(String foreignSource, String foreignId, RequisitionInterface riface) {
		m_provisionService.delete(foreignSource, foreignId, riface);
	}

	protected JerseyProvisionRequisitionService getProvisionService() {
		return m_provisionService;
	}

	protected JerseyNodesService getNodeService() {
		return m_nodeService;
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
		if (loggedin)
			return;
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		m_snmpProfiles = new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
	}

	@SuppressWarnings("deprecation")
	public void loadBackupProfiles() throws SQLException {
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		m_backupProfiles = new SQLContainer(new FreeformQuery("select * from isi.asset_profiles", primarykeys,m_pool));
	}
	
	public String[] getUrls() {
		return URL_LIST;
	}
	
}
