package org.opennms.vaadin.provision.dashboard;


import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.model.BasicNode;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import elemental.events.KeyboardEvent.KeyCode;


public class LoginBox extends DashboardTab {

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
    
    public LoginBox () {
    	super();
        m_panel.setCaption(s_panellogincaption + getService().getConfig().getAppName());
        m_panel.setContent(getLoginBox());
        setCompositionRoot(m_panel);

    	m_login.addClickListener(this);
    	m_login.setImmediate(true);
    	    	
    	for (String url: getService().getConfig().getUrls())
    		m_select.addItem(url);
    	m_select.select(getService().getConfig().getUrls()[0]);
    	m_username.focus();
    	m_login.setClickShortcut(KeyCode.ENTER);
	}

    public void load() {
    	
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
		super.buttonClick(event);
	}

	public void logout() {
		m_username.setValue("");
		m_password.setValue("");
		m_panel.setCaption(s_panellogincaption + getService().getConfig().getAppName());
		m_panel.setContent(new Label("Exit Work Session : please reload page to log into"));
	}

	private void closeSession() {
		getService().cleanSessionObjects();
		logout();
		getUI().getSession().close();
	}
	
	@SuppressWarnings("unchecked")
	private void login() {	
		try {
			((DashboardUI)getUI()).login(m_select.getValue().toString(),m_username.getValue(),m_password.getValue());
		} catch (ClientHandlerException che) {
			Notification.show("Connection Failed", "Verificare che OpenNMS  sia \'running\': " + m_select.getValue().toString(), Notification.Type.ERROR_MESSAGE);
			logger.log(Level.WARNING,"Login Failed for rest access",che);
			closeSession();
			return;
		} catch (UniformInterfaceException uie) {
			if (uie.getResponse().getStatusInfo() == Status.UNAUTHORIZED) {
				Notification.show("Authentication Failed", "Verificare Username e Password", Notification.Type.ERROR_MESSAGE);
			} else if (uie.getResponse().getStatusInfo() == Status.FORBIDDEN) {
				Notification.show("Authorization Failed", "Verificare che lo user: "+ m_username+" abbia ROLE_PROVISIONING", Notification.Type.ERROR_MESSAGE);
			} else {
				Notification.show("Login Failed", "Contattare l'amministratore del sistema", Notification.Type.ERROR_MESSAGE);
			}
			logger.log(Level.WARNING,"Login Failed for rest access",uie);
			closeSession();
			return;
		} catch (Exception e) {
			Notification.show("Login Failed", "Contattare l'amministratore del sistema", Notification.Type.ERROR_MESSAGE);
			logger.log(Level.WARNING,"Login Failed for rest access",e);
			closeSession();
			return;
		}
		
		try {
			getService().init();
			logger.info("Dashboard Servlet Service: init completed");
		} catch (SQLException e) {
			Notification.show("Init Failed", "Contattare l'amministratore del sistema", Notification.Type.ERROR_MESSAGE);
			logger.log(Level.WARNING,"Init Failed for rest access",e);
			closeSession();
		}
	    m_panel.setCaption("User '"+ getService().getUser()+"' Logged in");

	    VerticalLayout loggedin= new VerticalLayout();
	    loggedin.setMargin(true);
	    loggedin.setSpacing(true);

	    loggedin.addComponent(getLeftHead());
	    
	    if (getService().getUser().equals("admin")) {
		    
		    Table conneTable = new Table("Sessioni attive");
		    conneTable.addContainerProperty("User", String.class, null);
		    conneTable.addContainerProperty("Url", String.class, null);
		    conneTable.addContainerProperty("Conn", String.class, null);
		    conneTable.addContainerProperty("Session", String.class, null);
		    conneTable.addContainerProperty("Pool", String.class, null);
		    for (DashBoardSessionService sessionService: ((DashBoardService)getService().getService()).getActiveSessions()) {
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
	}
	
	@Override
	public String getName() {
		return "LoginBox";
	}

	@Override
	public void resetUpdateMap() {
	}

	@Override
	public Map<String, Collection<BasicNode>> getUpdatesMap() {
		return new HashMap<String, Collection<BasicNode>>();
	}


}
