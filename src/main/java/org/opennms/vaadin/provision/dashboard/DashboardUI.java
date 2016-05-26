package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.dao.OnmsDao;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
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
@Title("Trentino Network Opennms Provision Dashboard")
@Theme("runo")
@PreserveOnRefresh
public class DashboardUI extends UI {

	private final static Logger logger = Logger.getLogger(DashboardTab.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -5948892618258879832L;

	protected void init(VaadinRequest request) {

		DashBoardService service = (DashBoardService) VaadinSession
				.getCurrent().getService();

		if (!service.ready()) {
			layoutInitError("Init Failed per accesso database", 
					"L'applicazione non e' disponibile",
					"Accesso al database non dispobile, contattare l'amministratore di sistema");
			return;
		}

		DashBoardSessionService sessionservice = new DashBoardSessionService();
		sessionservice.setService(service);
		sessionservice.setOnmsDao(new OnmsDao());
		try {
			sessionservice.init();
		} catch (SQLException e) {
			layoutInitError(e.getMessage(), 
					"L'applicazione non e' disponibile", 
					"Accesso al database non dispobile, riprovare piu' tardi");
			return;
		}
		setContent(new DashboardTabSheet(sessionservice));
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
		Notification
				.show(error,
						label,
						Notification.Type.ERROR_MESSAGE);
	}

}
