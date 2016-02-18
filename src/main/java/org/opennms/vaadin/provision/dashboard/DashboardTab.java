package org.opennms.vaadin.provision.dashboard;


import com.vaadin.ui.CustomComponent;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
public abstract class DashboardTab extends CustomComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4694567853140078034L;
	private DashBoardSessionService m_service;

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	DashboardTab(DashBoardSessionService service) {
		m_service = service;
	}

	public abstract void load();
	
	public DashBoardSessionService getService() {
		return m_service;
	}

}
