package org.opennms.vaadin.provision.dashboard;



import java.util.Collection;
import java.util.Map;

import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.SyncOperationNode;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

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
	private DashBoardSessionService m_service;
	private VerticalLayout m_core;
	private Panel m_headPanel;
	private HorizontalLayout m_head = new HorizontalLayout();
    private Button m_logout = new Button("Logout");
    private Button m_info = new Button("Info");
    private LoginBox m_loginBox;

	/*
	 * After UI class is created, init() is executed. You should build and wire
	 * up your user interface here.
	 */
	DashboardTab(LoginBox login, DashBoardSessionService service) {
		m_loginBox = login;
    	m_logout.addClickListener(this);
    	m_logout.setImmediate(true);
     	m_info.addClickListener(this);
    	m_info.setImmediate(true);
		m_service = service;
		m_core = new VerticalLayout();
		setCompositionRoot(m_core);
		m_head.setSizeFull();
		m_head.addComponent(m_logout);	
		m_head.addComponent(m_info);	
		m_head.setMargin(true);
		m_head.setSpacing(true);
		m_headPanel = new Panel(m_head);
		m_core.addComponent(m_headPanel);
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
	
	public HorizontalLayout getHead() {
		return m_head;
	}
	
	public ComponentContainer getCore() {
		return m_core;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_logout) {
	    	logout();
	    } else if (event.getButton() == m_info) {
	    	info();
	    }
	}

	private void info() {
		final Window infowindow = new Window("Informazioni TNPD");
		VerticalLayout windowcontent = new VerticalLayout();
		windowcontent.setMargin(true);
		windowcontent.setSpacing(true);
		windowcontent.addComponent(new Label(getService().getConfig().getAppName()));
		windowcontent.addComponent(new Label("Versione: " + getService().getConfig().getAppVersion()));
		windowcontent.addComponent(new Label("Build: " + getService().getConfig().getAppBuild()));
		infowindow.setContent(windowcontent);
		infowindow.setModal(true);
		infowindow.setWidth("400px");
        UI.getCurrent().addWindow(infowindow);

	}

	
	private void logout() {
		if (m_service.isFastRunning()) {
			Notification.show("Cannot Logged Out", "Fast Sync is Running", Notification.Type.WARNING_MESSAGE);
			return;
		}
		Map <String, Map<String, BasicNode>> updates = m_service.getUpdates();
		for (Map<String, BasicNode> map: updates.values()) {
			if (!map.isEmpty()) {
				createdialogwindown(updates);
				return;
			}
		}
		m_loginBox.logout();
	    
	}
	
	private void createdialogwindown(Map <String, Map<String, BasicNode>> updates) {
		final Window confirm = new Window("Modifiche effettuate e non sincronizzate");
		Button si = new Button("si");
		si.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				m_loginBox.logout();
				confirm.close();
			}
		});
		
		Button no = new Button("no");
		no.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				confirm.close();
			}
		});
				
		VerticalLayout windowcontent = new VerticalLayout();
		windowcontent.setMargin(true);
		windowcontent.setSpacing(true);
    
		for (String requisition: updates.keySet()) {
			Map<String, BasicNode> requpdates = updates.get(requisition);
			if (requpdates == null || requpdates.size() == 0)
				continue;
			Table updatetable = new Table("Operazioni di Sync sospese per Requisition: " + requisition);
			updatetable.setSelectable(false);
			updatetable.setContainerDataSource(getUpdates(requpdates.values()));
			updatetable.setSizeFull();
			updatetable.setPageLength(3);
			windowcontent.addComponent(updatetable);
		}

		windowcontent.addComponent(new Label("Alcune Modifiche che sono state effettuate "
				+ "alle Requisition richiedono le "
				+ "operazioni di sync sopra elencate. Confermi il logout?"));
		HorizontalLayout buttonbar = new HorizontalLayout();
		buttonbar.setMargin(true);
		buttonbar.setSpacing(true);
    	buttonbar.addComponent(si);
		buttonbar.addComponent(no);
		windowcontent.addComponent(buttonbar);
	    confirm.setContent(windowcontent);
        confirm.setModal(true);
        confirm.setWidth("600px");
        UI.getCurrent().addWindow(confirm);
		
	}

	public BeanItemContainer<SyncOperationNode> getUpdates(Collection<BasicNode> nodes) {
		BeanItemContainer<SyncOperationNode> updates 
		= new BeanItemContainer<SyncOperationNode>(SyncOperationNode.class);
		for (BasicNode node: nodes) {
			updates.addBean(new SyncOperationNode(node));
		}
		return updates;
	}


}
