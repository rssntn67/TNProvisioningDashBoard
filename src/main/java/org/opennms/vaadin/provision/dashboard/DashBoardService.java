package org.opennms.vaadin.provision.dashboard;

import java.util.HashSet;
import java.util.Set;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;

public class DashBoardService {
    
	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
    
	private JerseyNodesService m_nodeService;

	private Set<String> m_categorieslist = new HashSet<String>();

	public Set<String> getCategorieslist() {
		return m_categorieslist;
	}


	public static final String LABEL = "nodeLabel";
	public static final String FOREIGNID = "foreignId";
	public static final String CATEGORIES = "categories";
	
	public static final String[] fieldNames = new String[] { LABEL, FOREIGNID, CATEGORIES};

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

	public BeanItemContainer<RequisitionNode> getRequisitionNodes(String foreignSource) {
		
		BeanItemContainer<RequisitionNode> nodes = new BeanItemContainer<RequisitionNode>(RequisitionNode.class);
		
		for (RequisitionNode node : m_provisionService.get(foreignSource).getNodes()) {
			nodes.addBean(node);
		}
		
		return nodes;
	}

	public BeanContainer<String,RequisitionNode> getContainerRequisitionNodes(String foreignSource) {
		
		BeanContainer<String,RequisitionNode> nodes = new BeanContainer<String,RequisitionNode>(RequisitionNode.class);
		nodes.setBeanIdProperty(FOREIGNID);
		
		for (RequisitionNode node : m_provisionService.get(foreignSource).getNodes()) {
			nodes.addBean(node);
		}
		
		return nodes;
	}

	/*
	 * Generate some in-memory example data to play with. In a real application
	 * we could be using SQLContainer, JPAContainer or some other to persist the
	 * data.
	 */
	@SuppressWarnings("unchecked")
	public IndexedContainer getNodeList() {
		m_categorieslist.clear();
	 	IndexedContainer ic = new IndexedContainer();

		for (String p : fieldNames) {
			ic.addContainerProperty(p, String.class, "");
		}

		for (OnmsNode node : m_nodeService.getAll() ) {
			Object id = ic.addItem();
			ic.getContainerProperty(id, LABEL).setValue(node.getLabel());
			ic.getContainerProperty(id, FOREIGNID).setValue(node.getForeignId());
			String categories = "";
			for (OnmsCategory category: node.getCategories()) {
				if (category != null && !category.getName().equals("")) {
					m_categorieslist.add(category.getName());
					categories += category.getName()+"-";
				}
			}
			ic.getContainerProperty(id, CATEGORIES).setValue(categories);
			if (categories.equals(""))
				continue;
			m_categorieslist.add(categories);
		}

		return ic;
	}
	
	
	@SuppressWarnings("unchecked")
	public IndexedContainer getProvisionNodeList(String foreignSource) {
		m_categorieslist.clear();
	 	IndexedContainer ic = new IndexedContainer();

		for (String p : fieldNames) {
			ic.addContainerProperty(p, String.class, "");
		}

	 	for (RequisitionNode node: m_provisionService.getNodes(foreignSource)) {
			Object id = ic.addItem();
			ic.getContainerProperty(id, LABEL).setValue(node.getNodeLabel());
			ic.getContainerProperty(id, FOREIGNID).setValue(node.getForeignId());
			String categories = "";
			for (RequisitionCategory category: node.getCategories()) {
				if (category != null && !category.getName().equals("")) {
					m_categorieslist.add(category.getName());
					categories += category.getName()+"-";
				}
			}
			ic.getContainerProperty(id, CATEGORIES).setValue(categories);
			if (categories.equals(""))
				continue;
			m_categorieslist.add(categories);
		}
	 		
	 	return ic;
	}

	public void check() {
		m_nodeService.getWithDefaultsQueryParams();
	}
	
	protected JerseyProvisionRequisitionService getProvisionService() {
		return m_provisionService;
	}

	protected JerseyNodesService getNodeService() {
		return m_nodeService;
	}

    
}
