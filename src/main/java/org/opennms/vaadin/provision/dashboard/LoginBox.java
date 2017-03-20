package org.opennms.vaadin.provision.dashboard;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickListener;

import elemental.events.KeyboardEvent.KeyCode;


public class LoginBox extends CustomComponent implements ClickListener {

	private DashBoardSessionService m_service;

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -363579873686942087L;

	private static final String s_panellogincaption="- Log In - ";
	private Panel m_panel  = new Panel();
    private ComboBox m_select = new ComboBox("Select Domain");
    private TextField m_username = new TextField("Username:");
    private PasswordField m_password = new PasswordField("Password:");
    private Button m_login = new Button("Login");
    private Button m_logout = new Button("Logout");
    private Button m_info = new Button("Info");
    private TabSheet m_tabs;
    
    public LoginBox (TabSheet tabs,DashBoardSessionService service) {
        m_tabs=tabs;
    	m_service = service;
        m_panel.setCaption(s_panellogincaption + m_service.getConfig().getAppName());
        m_panel.setContent(getLoginBox());
        setCompositionRoot(m_panel);

    	m_login.addClickListener(this);
    	m_login.setImmediate(true);
    	
    	m_info.addClickListener(this);
    	m_info.setImmediate(true);
    	
    	m_logout.addClickListener(this);
    	m_logout.setImmediate(true);
    	
    	for (String url: m_service.getConfig().getUrls())
    		m_select.addItem(url);
    	m_select.select(m_service.getConfig().getUrls()[0]);
    	m_username.focus();
    	m_login.setClickShortcut(KeyCode.ENTER);

	}

 
    private Component getLoginBox() {
    	VerticalLayout layout = new VerticalLayout();
    	layout.setMargin(true);
        layout.addComponent(m_select);
        layout.addComponent(m_username);
        layout.addComponent(m_password);
        layout.addComponent(m_login);
        return layout;
    }
    
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_login) {
			login();
		} else if (event.getButton() == m_logout) {
	    	logout();
		} else if (event.getButton() == m_info) {
	    	info();
		}
	}

	public void logout() {
		if (m_service.isFastRunning()) {
			Notification.show("Cannot Logged Out", "Fast Sync is Running", Notification.Type.WARNING_MESSAGE);
			return;
		}
		Map<String,List<BasicNode>> updatemap = new HashMap<String,List<BasicNode>>();
		Iterator<Component> ite = m_tabs.iterator();
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
		m_username.setValue("");
		m_password.setValue("");
		m_service.logout();
		m_panel.setCaption(s_panellogincaption + m_service.getConfig().getAppName());
		Notification.show("Logged Out", "Provide username and password to log in", Notification.Type.HUMANIZED_MESSAGE);
		m_panel.setContent(getLoginBox());
		Iterator<Component> ite = m_tabs.iterator();
	    while (ite.hasNext()) {
	    	Component comp = ite.next();
	    	if (comp != this) {
	    		m_tabs.getTab(comp).setEnabled(false);
	    	} else {
	    		m_tabs.setSelectedTab(comp);
	    	}
	    }
	    getUI().getSession().close();
	}
	
	@SuppressWarnings("unchecked")
	private void login() {	
		try {
		    m_service.login(m_select.getValue().toString(),m_username.getValue(),m_password.getValue());
		} catch (ClientHandlerException che) {
			Notification.show("Connection Failed", "Verificare che OpenNMS  sia \'running\': " + m_select.getValue().toString(), Notification.Type.ERROR_MESSAGE);
			logger.log(Level.WARNING,"Login Failed for rest access",che);
			m_username.setValue("");
			m_password.setValue("");
			m_service.logout();
			return;
		} catch (UniformInterfaceException uie) {
			if (uie.getResponse().getStatusInfo() == Status.UNAUTHORIZED)
				Notification.show("Authentication Failed", "Verificare Username e Password", Notification.Type.ERROR_MESSAGE);
			else if (uie.getResponse().getStatusInfo() == Status.FORBIDDEN)
				Notification.show("Authorization Failed", "Verificare che lo user: "+ m_username+" abbia ROLE_PROVISIONING", Notification.Type.ERROR_MESSAGE);
			else
				Notification.show("Login Failed", "Contattare l'amministratore del sistema", Notification.Type.ERROR_MESSAGE);
			logger.log(Level.WARNING,"Login Failed for rest access",uie);
			m_username.setValue("");
			m_password.setValue("");
			m_service.logout();
			return;
		} catch (Exception e) {
			Notification.show("Login Failed", "Contattare l'amministratore del sistema", Notification.Type.ERROR_MESSAGE);
			logger.log(Level.WARNING,"Login Failed for rest access",e);
			m_username.setValue("");
			m_password.setValue("");
			m_service.logout();
			return;
		}
	    m_panel.setCaption("User '"+ m_service.getUser()+"' Logged in");
	    VerticalLayout loggedin= new VerticalLayout();
	    loggedin.setMargin(true);
	    loggedin.setSpacing(true);
	    HorizontalLayout buttonPanel = new HorizontalLayout();
	    buttonPanel.setSizeFull();
	    buttonPanel.addComponent(m_info);
	    buttonPanel.addComponent(m_logout);
	    loggedin.addComponent(buttonPanel);
	    if (m_service.getUser().equals("admin")) {
		    
		    Table conneTable = new Table("Sessioni attive");
		    conneTable.addContainerProperty("User", String.class, null);
		    conneTable.addContainerProperty("Url", String.class, null);
		    conneTable.addContainerProperty("Conn", String.class, null);
		    conneTable.addContainerProperty("Session", String.class, null);
		    conneTable.addContainerProperty("Pool", String.class, null);
		    for (DashBoardSessionService sessionService: ((DashBoardService)m_service.getService()).getActiveSessions()) {
		    	Object newItemId = conneTable.addItem();
		    	Item row1 = conneTable.getItem(newItemId);
		    	row1.getItemProperty("User").setValue(sessionService.getUser());
		    	row1.getItemProperty("Url").setValue(sessionService.getUrl());
		    	row1.getItemProperty("Conn").setValue(sessionService.getConfig().getDbUrl());
		    	row1.getItemProperty("Session").setValue(sessionService.toString());
		    	row1.getItemProperty("Pool").setValue(sessionService.getPool().toString());
		    }
		    VerticalLayout tablelayout = new VerticalLayout();
		    tablelayout.setMargin(true);
		    tablelayout.setSpacing(true);
		    tablelayout.addComponent(conneTable);
		    loggedin.addComponent(tablelayout);

	    }
	    m_panel.setContent(loggedin);
	    
	    
	    Iterator<Component> ite = m_tabs.iterator();
	    while (ite.hasNext()) {
	    	try {
	    		DashboardTab dashboardTab = (DashboardTab) ite.next();
	    		if (m_service.getConfig().isTabDisabled(dashboardTab.getName(),m_username.getValue()))
	    			continue;
	    		m_tabs.getTab(dashboardTab).setEnabled(true);
	    	} catch (Exception e) {
	    		logger.log(Level.INFO,"Log tab always enabled");
	    	}
	    }
	}
	
	private void createdialogwindown(Map<String,List<BasicNode>> updatemap) {
		final Window confirm = new Window("Modifiche effettuate e non sincronizzate");
		Button si = new Button("si");
		si.addClickListener(new ClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				logout();
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
				reallylogout();
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
	
	public void info() {
		final Window infowindow = new Window("Informazioni TNPD");
		VerticalLayout windowcontent = new VerticalLayout();
		windowcontent.setMargin(true);
		windowcontent.setSpacing(true);
		windowcontent.addComponent(new Label(m_service.getConfig().getAppName()));
		windowcontent.addComponent(new Label("Versione: " + m_service.getConfig().getAppVersion()));
		windowcontent.addComponent(new Label("Build: " + m_service.getConfig().getAppBuild()));
		infowindow.setContent(windowcontent);
		infowindow.setModal(true);
		infowindow.setWidth("400px");
        UI.getCurrent().addWindow(infowindow);

	}


}
