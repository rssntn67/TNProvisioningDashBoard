package org.opennms.vaadin.provision.dashboard;


import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;

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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;

import elemental.events.KeyboardEvent.KeyCode;


public class LoginBox extends CustomComponent implements ClickListener {

	private DashBoardSessionService m_service;

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -363579873686942087L;

	private static final String s_panellogincaption="- Log In - Trentino Network Provisioning Dashboard 3.0.2 RC 01 Build January 29rd 2017 12:40 CET";
	private Panel m_panel  = new Panel(s_panellogincaption);
    private ComboBox m_select = new ComboBox("Select Domain");
    private TextField m_username = new TextField("Username:");
    private PasswordField m_password = new PasswordField("Password:");
    private Button m_login = new Button("Login");
    private Button m_logout = new Button("Logout");
	private Button m_populateTnSnmpButton  = new Button("Sync TN Snmp Data");
	private Button m_populateSiSnmpButton  = new Button("Sync SI Snmp Data");
    private TabSheet m_tabs;
    
    public LoginBox (TabSheet tabs,DashBoardSessionService service) {
        m_panel.setContent(getLoginBox());
        setCompositionRoot(m_panel);

        m_tabs=tabs;
    	m_service = service;
    	m_login.setImmediate(true);
    	m_login.addClickListener(this);
    	m_logout.addClickListener(this);
    	m_logout.setImmediate(true);
    	m_populateSiSnmpButton.addClickListener(this);
    	m_populateSiSnmpButton.setImmediate(true);
    	m_populateTnSnmpButton.addClickListener(this);
    	m_populateTnSnmpButton.setImmediate(true);
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
	    } else if (event.getButton() == m_populateTnSnmpButton) {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
			logger.info("Sync db with snmp profiles for Requisition: " + DashBoardUtils.TN_REQU_NAME);
			try {
				m_service.syncTnSnmpProfile();
				Notification.show("Sync Snmp profile: " + DashBoardUtils.TN_REQU_NAME, " Done ", Type.HUMANIZED_MESSAGE);
			} catch (Exception e) {
				logger.warning("Sync Snmp profile Failed: " + DashBoardUtils.TN_REQU_NAME + " " + e.getLocalizedMessage());
				Notification.show("Sync Snmp profile Failed: " + DashBoardUtils.TN_REQU_NAME, e.getLocalizedMessage(), Type.ERROR_MESSAGE);
			}
				}
			});
		} else if (event.getButton() == m_populateSiSnmpButton) {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
			logger.info("Sync db with snmp profiles for Requisition: " + DashBoardUtils.SI_REQU_NAME);
			try {
				m_service.syncSiSnmpProfile();
				Notification.show("Sync Snmp profile: " + DashBoardUtils.SI_REQU_NAME, " Done ", Type.HUMANIZED_MESSAGE);
			} catch (Exception e) {
				logger.warning("Sync Snmp profile Failed: " + DashBoardUtils.SI_REQU_NAME + " " + e.getLocalizedMessage());
				Notification.show("Sync Snmp profile Failed: " + DashBoardUtils.SI_REQU_NAME, e.getLocalizedMessage(), Type.ERROR_MESSAGE);
			}
				}
			});
		}
	}

	public void logout() {
		if (m_service.isFastRunning()) {
			Notification.show("Cannot Logged Out", "Fast Sync is Running", Notification.Type.WARNING_MESSAGE);
			return;
		}
		m_username.setValue("");
		m_password.setValue("");
		m_service.logout();
		m_panel.setCaption(s_panellogincaption);
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
	    HorizontalLayout buttonPanel = new HorizontalLayout();
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
		    loggedin.addComponent(conneTable);
		    buttonPanel.addComponent(m_populateTnSnmpButton);
		    buttonPanel.addComponent(m_populateSiSnmpButton);

	    }
	    buttonPanel.addComponent(m_logout);
	    loggedin.addComponent(buttonPanel);
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
		
	
}
