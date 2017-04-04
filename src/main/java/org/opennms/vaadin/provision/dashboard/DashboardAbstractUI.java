package org.opennms.vaadin.provision.dashboard;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@PreserveOnRefresh
public class DashboardAbstractUI extends UI {

	private final static Logger logger = Logger.getLogger(DashboardTab.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	private DashBoardSessionService m_sessionservice;
	private DashBoardService m_service;
	protected void init(VaadinRequest request) {
		
		m_sessionservice = (DashBoardSessionService) VaadinSession.getCurrent();
		m_service = (DashBoardService)m_sessionservice.getService();
		
		if (!m_service.ready()) {
   			layoutInitError("file di configurazione in errore", 
					"L'applicazione non e' disponibile",
					"File configurazione non disponibile. Contattare l'amministratore di sistema");
   			return;
		}
		
		try {
			m_sessionservice.init();
		} catch (Exception e) {
			logger.log(Level.SEVERE,e.getLocalizedMessage(),e);
			layoutInitError("Init Failed per accesso database", 
				"L'applicazione non e' disponibile",
				"Accesso al database non dispobile, contattare l'amministratore di sistema");
			return;
		}
	}

	private void layoutInitError(String error, String message, String label) {
		AbsoluteLayout layout = new AbsoluteLayout();
		layout.setWidth("800px");
		layout.setHeight("600px");
		Panel panel = new Panel(message);
		panel.setContent(new Label(label));
		panel.setWidth("500px");
		layout.addComponent(panel, "left: 150px; top: 100px;");
		setContent(layout);
		logger.log(Level.WARNING, error);
		Notification.show(error, label, Notification.Type.ERROR_MESSAGE);
	    getUI().getSession().close();		
	}

	public DashBoardSessionService getSessionservice() {
		return m_sessionservice;
	}

	public DashBoardService getService() {
		return m_service;
	}
	
	
}
