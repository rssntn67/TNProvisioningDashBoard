package org.opennms.vaadin.provision.dashboard;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.dao.OnmsDao;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@Title("Trentino Network Opennms Provision Dashboard")
@Theme("runo")
@PreserveOnRefresh
public class DashboardUI extends UI {

	private final static Logger logger = Logger.getLogger(DashboardTab.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	protected void init(VaadinRequest request) {
		
		DashBoardService service = (DashBoardService)VaadinSession.getCurrent().getService();
		
		if (service == null) {
			setContent(new Label("Applicazione non disponibile"));
			logger.log(Level.WARNING,"Init Failed per accesso database");
			Notification.show("Init Failed", "Problemi di Accesso al profile database", Notification.Type.ERROR_MESSAGE);
			return;
		}

		DashBoardSessionService sessionservice = new DashBoardSessionService();
    	sessionservice.setService(service);
    	sessionservice.setOnmsDao(new OnmsDao());
    	setContent(new DashboardTabSheet(sessionservice));	    
	}

}
