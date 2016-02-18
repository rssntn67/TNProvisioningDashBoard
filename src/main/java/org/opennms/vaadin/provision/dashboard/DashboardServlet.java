package org.opennms.vaadin.provision.dashboard;

import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.TNDao;

import com.vaadin.server.VaadinServlet;

public class DashboardServlet extends VaadinServlet {

	private final static Logger logger = Logger.getLogger(DashboardServlet.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 729121367536078497L;

	public DashboardServlet() {
		super();
	}
	
	 @Override
	protected void servletInitialized()
	            throws ServletException {

	        super.servletInitialized();		    
			logger.info("servletInitialized: calling.");
	}

	@Override 
	protected DashBoardService createServletService(com.vaadin.server.DeploymentConfiguration deploymentConfiguration) {
		DashBoardConfig config = new DashBoardConfig();
		TNDao tnDao= new TNDao();
		DashBoardService service = new DashBoardService(getCurrent(), deploymentConfiguration);
	    service.setConfig(config);
	    service.setTnDao(tnDao);

		logger.info("createServletService: creating Dashboard Service: " + service);

	    return service;
	};
	
	
}
