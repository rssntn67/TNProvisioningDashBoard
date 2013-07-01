package org.opennms.vaadin.provision.dashboard;

import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;

public class DashBoardService {
    
	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
    
	private JerseyNodesService m_nodeService;
    
    public DashBoardService() {
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

	public Container getRequisitionNodes(String foreignSource) {
		
		BeanItemContainer<RequisitionNode> nodes = new BeanItemContainer<RequisitionNode>(RequisitionNode.class);
		
		for (RequisitionNode node : getProvisionService().get(foreignSource).getNodes()) {
			nodes.addBean(node);
		}
		
		return nodes;
	}

	public JerseyProvisionRequisitionService getProvisionService() {
		return m_provisionService;
	}


	public JerseyNodesService getNodeService() {
		return m_nodeService;
	}


	public void check() {
		getNodeService().getWithDefaultsQueryParams();
	}
	
    
}
