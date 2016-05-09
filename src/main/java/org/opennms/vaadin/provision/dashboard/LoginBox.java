package org.opennms.vaadin.provision.dashboard;


import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickListener;

import elemental.events.KeyboardEvent.KeyCode;


public class LoginBox extends CustomComponent implements ClickListener {

	private DashBoardSessionService m_service;

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -363579873686942087L;

	private static final String s_panellogincaption="- Log In - Trentino Network Provisioning Dashboard 3.0.0 Build May 4th 2016";
	private Panel m_panel  = new Panel(s_panellogincaption);
    private ComboBox m_select = new ComboBox("Select Domain");
    private TextField m_username = new TextField("Username:");
    private PasswordField m_password = new PasswordField("Password:");
    private Button m_login = new Button("Login");
    private Button m_logout = new Button("Logout");
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
		if (event.getButton() == m_login) 
			login();
	    else if (event.getButton() == m_logout) {
	    	logout();
	    }
	}

	private void logout() {
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
	    	if (comp != this)
	    	m_tabs.getTab(comp).setEnabled(false);
	    }
	    getUI().getSession().close();
	    getUI().getPage().setLocation(getUI().getPage().getLocation());
	    
	}
	
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
	    m_panel.setCaption("Logged in");
	    m_panel.setContent(m_logout);
	    
	    @SuppressWarnings("deprecation")
	    Iterator<Component> ite = m_tabs.getComponentIterator();
	    while (ite.hasNext()) {
	    	m_tabs.getTab(ite.next()).setEnabled(true);
	    }
	}
		
	
}
