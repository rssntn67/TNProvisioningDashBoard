package org.opennms.vaadin.provision.dashboard;

import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinServlet;

public class DashboardServlet extends VaadinServlet implements SessionDestroyListener{

	private final static Logger logger = Logger.getLogger(DashboardServlet.class.getName());


	/**
	 * 
	 */
	private static final long serialVersionUID = 729121367536078497L;
	
	@Override
	protected void servletInitialized()
	            throws ServletException {
		getService().addSessionDestroyListener(this);
        super.servletInitialized();		    
	}

	@Override 
	protected DashBoardService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException  {
		logger.info("creating Dashboard Servlet Service ");
		DashBoardService service = new DashBoardService(this, deploymentConfiguration);
		return service;
	}
	
	@Override
    public void sessionDestroy(SessionDestroyEvent event) {
		logger.info("cleaning Dashboard Session Service: " + event.getSession());
		DashBoardService service = (DashBoardService) event.getService();
		DashBoardSessionService sessionservice = (DashBoardSessionService) event.getSession();
		service.unregisterSession(sessionservice);
    }
}
