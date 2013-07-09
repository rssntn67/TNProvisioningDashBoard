package org.opennms.vaadin.provision.dashboard;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;

public class DashboardService {
    
	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
    
	private JerseyNodesService m_nodeService;

	public static final String FOREIGNID = "foreignId";
	
    public DashboardService() {
    	m_provisionService = new JerseyProvisionRequisitionService();
    	m_nodeService = new JerseyNodesService();
    }
    
	public JerseyClientImpl getJerseyClient() {
		return m_jerseyClient;
	}

	public void setJerseyClient(JerseyClientImpl jerseyClient) {
		m_jerseyClient = jerseyClient;
		m_provisionService.setJerseyClient(m_jerseyClient);
	    m_nodeService.setJerseyClient(m_jerseyClient);
	}

	public Requisition getRequisitionNodes(String foreignSource) {		
		return m_provisionService.get(foreignSource);
	}

	public void check() {
		m_nodeService.getWithDefaultsQueryParams();
	}
	
	public void add(String foreignSource, RequisitionNode node) {
		m_provisionService.addOrReplace(foreignSource, node);
	}

	protected JerseyProvisionRequisitionService getProvisionService() {
		return m_provisionService;
	}

	protected JerseyNodesService getNodeService() {
		return m_nodeService;
	}

    
}
