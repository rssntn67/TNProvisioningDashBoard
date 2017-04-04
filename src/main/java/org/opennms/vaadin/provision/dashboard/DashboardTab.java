package org.opennms.vaadin.provision.dashboard;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
public abstract class DashboardTab extends CustomComponent implements ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4694567853140078034L;
	private VerticalLayout m_core;
	private Panel m_headPanel;
	private HorizontalLayout m_head = new HorizontalLayout();
	private VerticalLayout m_left = new VerticalLayout();;
	private VerticalLayout m_right = new VerticalLayout();;
    private Button m_logout = new Button("Logout");
    private Button m_info = new Button("Info");

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	DashboardTab() {
    	m_logout.addClickListener(this);
    	m_logout.setImmediate(true);
     	m_info.addClickListener(this);
    	m_info.setImmediate(true);
		m_core = new VerticalLayout();
		setCompositionRoot(m_core);

		HorizontalLayout head = new HorizontalLayout();
		head.setSizeFull();
		head.addComponent(m_info);	
		head.addComponent(m_logout);	
		head.setComponentAlignment(m_info, Alignment.MIDDLE_LEFT);
		head.setComponentAlignment(m_logout, Alignment.MIDDLE_LEFT);
		head.setMargin(true);
		head.setSpacing(true);
		
		m_head.setMargin(true);
		m_head.setSpacing(true);
		
		HorizontalSplitPanel headsplitPanel = new HorizontalSplitPanel();
		headsplitPanel.addComponent(head);
		headsplitPanel.addComponent(m_head);
		headsplitPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
		headsplitPanel.setSplitPosition(30,Unit.PERCENTAGE);

		m_headPanel = new Panel(headsplitPanel);
		m_core.addComponent(m_headPanel);
		
		HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.addComponent(m_left);
		splitPanel.addComponent(m_right);
		splitPanel.setStyleName(Reindeer.SPLITPANEL_SMALL);
		splitPanel.setSplitPosition(30,Unit.PERCENTAGE);

		m_core.addComponent(splitPanel);
	}

	public abstract void load();	
	public abstract String getName();

	public DashBoardSessionService getService(){
		return ((DashboardAbstractUI)getUI()).getSessionService();
	};
	
	public void updateTabHead() {
		m_headPanel.setCaption("User: " + getService().getUser() 
				+". connected to: " + getService().getUrl());  
	}
	
	public HorizontalLayout getHead() {
		return m_head;
	}
	
	public ComponentContainer getCore() {
		return m_core;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_logout) {
	    	((DashboardUI)getUI()).onLogout();
	    } else if (event.getButton() == m_info) {
	    	((DashboardUI)getUI()).onInfo();
	    }
	}
	
	public VerticalLayout getLeft() {
		return m_left;
	}
	
	public VerticalLayout getRight() {
		return m_right;
	}


	
}
