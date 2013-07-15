package org.opennms.vaadin.provision.dashboard;


import java.util.Iterator;

import org.opennms.rest.client.JerseyClientImpl;

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

public class LoginBox extends CustomComponent implements ClickListener {

	protected final static String[] URL_LIST = new String[] {
		"http://demo.arsinfo.it/opennms/rest",
		"http://demo.arsinfo.it:8980/opennms/rest",
		"http://localhost:8980/opennms/rest"
	};

	private DashboardService m_service;

	/**
	 * 
	 */
	private static final long serialVersionUID = -363579873686942087L;

	private Panel m_panel  = new Panel("Log In");
    private ComboBox m_select = new ComboBox("Select Domain");
    private TextField m_username = new TextField("Username:");
    private PasswordField m_password = new PasswordField("Password:");
    private Button m_login = new Button("Login");
    private Button m_logout = new Button("Logout");
    private TabSheet m_tabs;
    
    public LoginBox (TabSheet tabs,DashboardService service) {
    	m_tabs=tabs;
    	m_service = service;
    	m_login.setImmediate(true);
    	m_login.addClickListener(this);
    	m_logout.addClickListener(this);
    	m_logout.setImmediate(true);
    	for (String url: URL_LIST)
    		m_select.addItem(url);

    	init();
        
    }

	private void init() {
    	VerticalLayout layout = new VerticalLayout();
    	layout.setMargin(true);
        layout.addComponent(m_select);
        layout.addComponent(m_username);
        layout.addComponent(m_password);
        layout.addComponent(m_login);
        m_panel.setContent(layout);
        setCompositionRoot(m_panel);
	}

    
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_login) 
			restlogin();
	    else if (event.getButton() == m_logout) {
	    	restlogout();
	    	init();
	    }
	}

	private void restlogout() {
		m_username.setValue("");
		m_password.setValue("");
		m_panel.setCaption("Log In");
		m_service.setJerseyClient(null);
		Notification.show("Logged Out", "Provide username and password to log in", Notification.Type.HUMANIZED_MESSAGE);
	    Iterator<Component> ite = m_tabs.getComponentIterator();
	    while (ite.hasNext()) {
	    	Component comp = ite.next();
	    	if (comp != this)
	    	m_tabs.getTab(comp).setEnabled(false);
	    }
	}
	
	private void restlogin() {	
		try {
			m_service.setJerseyClient(
					new JerseyClientImpl(
		            m_select.getValue().toString(),m_username.getValue(),m_password.getValue()));
			if (m_service.getJerseyClient() != null ) {
			    m_service.check();
			}
		} catch (Exception e) {
			Notification.show("Login Failed", "Check Username and Password", Notification.Type.ERROR_MESSAGE);
			m_username.setValue("");
			m_password.setValue("");
			return;
		}
	    m_panel.setCaption("Logged in");
	    m_panel.setContent(m_logout);
	    Iterator<Component> ite = m_tabs.getComponentIterator();
	    while (ite.hasNext()) {
	    	m_tabs.getTab(ite.next()).setEnabled(true);
	    }
	}
		
	
}
