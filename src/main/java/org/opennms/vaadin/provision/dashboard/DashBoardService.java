package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;
import java.util.logging.Logger;

import java.util.HashSet;
import java.util.Set;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletService;


public class DashBoardService extends VaadinServletService implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
			
    private boolean m_init = false;

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

    Set<DashBoardSessionService> m_sessions = new HashSet<DashBoardSessionService>();
    
	public DashBoardService(DashboardServlet servlet,
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		super(servlet, deploymentConfiguration);
	}
		
	public void init() throws ServiceException {
		super.init();
		m_init = true;
	}
	
	public boolean ready() {
		return m_init;
	}

	@Override
	public DashBoardSessionService createVaadinSession(VaadinRequest request) throws 
		ServiceException {

		DashBoardSessionService sessionservice = new DashBoardSessionService(this);
		logger.info("initing session:" + sessionservice);

		registerSession(sessionservice);
		return sessionservice;
	}

	public void registerSession(DashBoardSessionService session) {
		logger.info("registering DashBoardSessionService: " + session);
		m_sessions.add(session);
	}
	
	public void unregisterSession(DashBoardSessionService session) {
		logger.info("unregistering DashBoardSessionService: " + session);
		m_sessions.remove(session);		
	}


}
