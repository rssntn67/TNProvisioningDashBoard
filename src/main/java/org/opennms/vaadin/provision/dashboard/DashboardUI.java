package org.opennms.vaadin.provision.dashboard;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicNode.OnmsSync;

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
import com.vaadin.ui.Notification.Type;

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
		logger.info("Init DashBoardUI: start");
		super.init(request);
		m_tabSheet = new DashboardTabSheet();
		setContent(m_tabSheet);
		logger.info("Init DashBoardUI: end");
	}
	
	public void enabletabs(String username)  {
	    Iterator<Component> ite = m_tabSheet.iterator();
	    while (ite.hasNext()) {
	    		DashboardTab dashboardTab = (DashboardTab) ite.next();
	    		if (getSessionService().getConfig().isTabDisabled(dashboardTab.getName(),username)) {
	    			continue;
	    		}
	    		m_tabSheet.getTab(dashboardTab).setEnabled(true);
	    }
	    m_tabSheet.getLoginBox().setEnabled(true);
	    m_tabSheet.getLoginBox().loginlayout();
	}

	public Map<String, Map<String,Collection<BasicNode>>> getUpdatesMapforTabs() {
		Map<String, Map<String,Collection<BasicNode>>> updatemap = 
				new HashMap<String,Map<String,Collection<BasicNode>>>();
		Iterator<Component> ite = m_tabSheet.iterator();
	    while (ite.hasNext()) {
    		DashboardTab tab = (DashboardTab) ite.next();
    		if (!tab.getUpdatesMap().isEmpty())
    			updatemap.put(tab.getName(), tab.getUpdatesMap());
    	}
	    return updatemap;
	}
	
	public void onLogout() {
		if (getSessionService().isFastRunning()) {
			createfastdialogwindown();
			return;
		}
		Map<String, Map<String,Collection<BasicNode>>> updatemap = 
				getUpdatesMapforTabs();

		if (!updatemap.isEmpty()) {
			createdialogwindown(updatemap);
			return;
		}
		reallylogout();	
	}
	
	public void clearUpdateMap(String requisition, BasicNode.OnmsSync sync) {
		Iterator<Component> ite = m_tabSheet.iterator();
	    while (ite.hasNext()) {
    		DashboardTab tab = (DashboardTab) ite.next();
    		tab.clearUpdateMap(requisition,sync);
	    }
	}

	public void synctrue(String requisition) {
		try {
			getSessionService().synctrue(requisition);
			clearUpdateMap(requisition,OnmsSync.TRUE);
			logger.info("Sync succeed foreign source: " +requisition);
			Notification.show("Sync " + requisition, "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync Failed foreign source: " +requisition + " " + e.getLocalizedMessage());
			Notification.show("Sync Failed foreign source" + requisition, e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}
	}

	public void syncfalse(String requisition) {
		try {
			getSessionService().syncfalse(requisition);
	    	clearUpdateMap(requisition,OnmsSync.FALSE);
			logger.info("Sync rescanExisting=false succeed foreign source: " +requisition);
			Notification.show("Sync rescanExisting=false " + requisition, "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync rescanExisting=false Failed foreign source: " +requisition + " " + e.getLocalizedMessage());
			Notification.show("Sync rescanExisting=false Failed foreign source" + requisition, e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}				
	}

	public void syncdbonly(String requisition) {
		try {
			getSessionService().syncdbonly(requisition);
			clearUpdateMap(requisition,OnmsSync.DBONLY);
			logger.info("Sync rescanExisting=dbonly succeed foreign source: " +requisition);
			Notification.show("Sync rescanExisting=dbonly " + requisition, "Request Sent to Rest Service", Type.HUMANIZED_MESSAGE);
		} catch (Exception e) {
			logger.warning("Sync rescanExisting=dbonly Failed foreign source: " +requisition + " " + e.getLocalizedMessage());
			Notification.show("Sync rescanExisting=dbonly Failed foreign source" + requisition, e.getLocalizedMessage(), Type.ERROR_MESSAGE);
		}						
	}

	public void reallylogout() {
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

	private void createfastdialogwindown() {
		final Window confirm = new Window("FAST integration is running.");
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
    
		windowcontent.addComponent(new Label("L'integrazione con FAST e' in esecuzione."
				+ "Se fai logout blocchi la esecuzione. Confermi il logout?"));
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

	
	private void createdialogwindown(Map<String,Map<String,Collection<BasicNode>>> updatemap) {
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
    
		for (String tabname: updatemap.keySet()) {
		for (String requisition: updatemap.get(tabname).keySet()) {
			Table updatetable = new Table("Operazioni di Sync sospese per Tab: "+ tabname + " Requisition: " + requisition);
			updatetable.setSelectable(false);
			updatetable.setContainerDataSource(DashBoardUtils.getUpdateContainer(updatemap.get(tabname).get(requisition)));
			updatetable.setSizeFull();
			updatetable.setPageLength(3);
			windowcontent.addComponent(updatetable);
		}}

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
		windowcontent.addComponent(new Label(getSessionService().getConfig().getAppName()));
		windowcontent.addComponent(new Label("Versione: " + getSessionService().getConfig().getAppVersion()));
		windowcontent.addComponent(new Label("Build: " + getSessionService().getConfig().getAppBuild()));
		infowindow.setContent(windowcontent);
		infowindow.setModal(true);
		infowindow.setWidth("400px");
        UI.getCurrent().addWindow(infowindow);
	}	

}
