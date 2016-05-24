package org.opennms.vaadin.provision.dashboard;


import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

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
	private VerticalLayout m_core;
	private Panel m_headPanel;


	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	DashboardTab(DashBoardSessionService service) {
		m_service = service;
		m_core = new VerticalLayout();
		m_headPanel = new Panel();
		setCompositionRoot(m_core);
		HorizontalLayout head = new HorizontalLayout();
		head.setSizeFull();
		head.addComponent(m_headPanel);
		m_core.addComponent(head);
	}

	public abstract void load();
	
	public abstract String getName();
	
	public DashBoardSessionService getService() {
		return m_service;
	}

	public void updateTabHead() {
		m_headPanel.setCaption("User: " + getService().getUser() 
				+". connected to: " + getService().getUrl());  
	}
	
	public ComponentContainer getCore() {
		return m_core;
	}
}
