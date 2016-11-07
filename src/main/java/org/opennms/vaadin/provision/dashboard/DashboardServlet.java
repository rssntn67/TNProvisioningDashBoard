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
        super.servletInitialized();		    
	}

	@Override 
	protected DashBoardService createServletService(DeploymentConfiguration deploymentConfiguration) throws ServiceException  {
		DashBoardService service = new DashBoardService(this, deploymentConfiguration);
		logger.info("createServletService: created Dashboard Service: " + service);
		service.init();
		logger.info("createServletService: init Dashboard Service: " + service);
	    return service;
	}
	
	@Override
    public void sessionDestroy(SessionDestroyEvent event) {
		logger.info("sessionDestroy: destroy Dashboard Session Service: " + event.getSession());
		DashBoardService service = (DashBoardService) event.getService();
		DashBoardSessionService sessionservice = (DashBoardSessionService) event.getSession();
		logger.info("sessionDestroy: logout user: " + sessionservice.getUser());
		service.unregisterSession(sessionservice);
		sessionservice.logout();
    }
}
