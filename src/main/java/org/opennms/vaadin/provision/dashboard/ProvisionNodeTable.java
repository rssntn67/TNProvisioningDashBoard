package org.opennms.vaadin.provision.dashboard;


import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
public class ProvisionNodeTable extends CustomComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2888254036745211183L;

	/**
	 * 
	 */
	/* User interface components are stored in session. */
	private Table m_requisitionNodeList = new Table();

	private String m_group;
	private DashBoardService m_service;
	private boolean loaded=false;

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	ProvisionNodeTable(String group,DashBoardService service) {
		m_group = group;
		m_service = service;
		setCompositionRoot(m_requisitionNodeList);
	}

	public void load() {
		if (loaded)
			return;
		m_requisitionNodeList.setContainerDataSource(m_service.getRequisitionNodes(getGroup()));
		loaded=true;
	}

	public String getGroup() {
		return m_group;
	}
	

}
