package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;
import org.opennms.rest.client.JerseySnmpInfoService;
import org.opennms.rest.client.SnmpInfo;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;

public class DashboardService {
    
	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
	private JerseyNodesService m_nodeService;
	private JerseySnmpInfoService m_snmpInfoService;

	private JDBCConnectionPool m_pool; 
	public static final String FOREIGNID = "foreignId";
	
    public DashboardService() {
    	try {
			m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", "jdbc:postgresql://172.25.200.36:5432/tnnet", "isi_writer", "Oof6Eezu");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public void check() {
		m_snmpInfoService.get("127.0.0.1");
	}
	
	public void add(String foreignSource, RequisitionNode node) {
		m_provisionService.add(foreignSource, node);
	}

	public void delete(String foreignSource, RequisitionNode node) {
		m_provisionService.delete(foreignSource, node);
	}
	
	protected JerseyProvisionRequisitionService getProvisionService() {
		return m_provisionService;
	}

	protected JerseyNodesService getNodeService() {
		return m_nodeService;
	}

    
}
