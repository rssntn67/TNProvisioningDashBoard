package org.opennms.vaadin.provision.dashboard;


import com.vaadin.ui.Table;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
public class ProvisionGroupTable extends ProvisionGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2888254036745211183L;

	/**
	 * 
	 */
	/* User interface components are stored in session. */
	private Table m_requisitionNodeList = new Table();

	private boolean loaded=false;

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	ProvisionGroupTable(String foreignsource,DashBoardService service) {
		super(foreignsource, service);
		setCompositionRoot(m_requisitionNodeList);
	}

	public void load() {
		if (loaded)
			return;
		m_requisitionNodeList.setContainerDataSource(getService().getRequisitionNodes(getForeignSource()));
		loaded=true;
	}

}
