package org.opennms.vaadin.provision.dashboard;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("Trentino Network Opennms Provision Dashboard")
@Theme("runo")
@PreserveOnRefresh
public class DashboardUI extends DashboardAbstractUI {

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());
	DashboardTabSheet m_tabSheet;
	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	protected void init(VaadinRequest request) {
		super.init(request);
		m_tabSheet = new DashboardTabSheet();
		setContent(m_tabSheet);
	}
	
	public void login(String url, String username, String password) throws ClientHandlerException,UniformInterfaceException, Exception  {
		getSessionService().login(url,username,password);
	    Iterator<Component> ite = m_tabSheet.iterator();
	    while (ite.hasNext()) {
	    	try {
	    		DashboardTab dashboardTab = (DashboardTab) ite.next();
	    		if (getService().getConfig().isTabDisabled(dashboardTab.getName(),username))
	    			continue;
	    		m_tabSheet.getTab(dashboardTab).setEnabled(true);
	    	} catch (Exception e) {
	    		logger.log(Level.WARNING,"Not a DashboardTab", e);
	    	}
	    }
	    m_tabSheet.getLoginBox().setEnabled(true);
	}
	
	public void onLogout() {
		if (getSessionService().isFastRunning()) {
			Notification.show("Cannot Logged Out", "Fast Sync is Running", Notification.Type.WARNING_MESSAGE);
			return;
		}
		Map<String,Collection<BasicNode>> updatemap = new HashMap<String,Collection<BasicNode>>();
		Iterator<Component> ite = m_tabSheet.iterator();
	    while (ite.hasNext()) {
	    	Component comp = ite.next();
	    	if (comp instanceof RequisitionTab) {
	    		RequisitionTab tab = (RequisitionTab) comp;
	    		if (!tab.getUpdates().isEmpty())
	    			updatemap.put(tab.getRequisitionName(),tab.getUpdates());
	    	}
    	}

		if (!updatemap.isEmpty()) {
			createdialogwindown(updatemap);
			return;
		}
		reallylogout();	
	}
	
	public void reallylogout() {
		getSessionService().logout();
		Iterator<Component> ite = m_tabSheet.iterator();
	    while (ite.hasNext()) {
	    	Component comp = ite.next();
	    	if (comp != m_tabSheet.getLoginBox()) {
	    		m_tabSheet.getTab(comp).setEnabled(false);
	    	} else {
	    		m_tabSheet.setSelectedTab(comp);
	    	}
	    }
		m_tabSheet.getLoginBox().logout();
	    getUI().getSession().close();
	}

	
	private void createdialogwindown(Map<String,Collection<BasicNode>> updatemap) {
		final Window confirm = new Window("Modifiche effettuate e non sincronizzate");
		Button si = new Button("si");
		si.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				reallylogout();
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
    
		for (String requisition: updatemap.keySet()) {
			Table updatetable = new Table("Operazioni di Sync sospese per Requisition: " + requisition);
			updatetable.setSelectable(false);
			updatetable.setContainerDataSource(DashBoardUtils.getUpdateContainer(updatemap.get(requisition)));
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

	public void onInfo() {
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

	
	

}
