package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.Set;

import org.opennms.vaadin.provision.config.DashBoardConfig;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServletService;


public class DashBoardService extends VaadinServletService implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
			
	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

    Set<DashBoardSessionService> m_sessions = new HashSet<DashBoardSessionService>();
    DashBoardConfig m_config;
    
	public DashBoardService(DashboardServlet servlet,
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		super(servlet, deploymentConfiguration);
	}
		
	@Override
	public void init() throws ServiceException {
		super.init();
		m_config = new DashBoardConfig();
	}
	
	@Override
	public DashBoardSessionService createVaadinSession(VaadinRequest request) throws 
		ServiceException {

		try {
			m_config.reload();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "cannot reload configuration file", e);
			throw new ServiceException(e);
		}

		DashBoardSessionService session = new DashBoardSessionService(this);
		session.setConfig(m_config);
		JDBCConnectionPool pool;
		try {
				pool =	new SimpleJDBCConnectionPool(
					"org.postgresql.Driver", m_config.getDbUrl(), m_config
							.getDbUsername(), m_config.getDbPassword());
			logger.info("created connection pool to database: " + m_config.getDbUrl());
		} catch (SQLException e) {
			logger.log(Level.SEVERE,
					"cannot create collection pool", e);
			throw new ServiceException(e);
		}
		session.setPool(pool);		

		logger.info("registering DashBoardSessionService: " + session + " with pool: " + pool);
		m_sessions.add(session);
		return session;
	}

	public Set<DashBoardSessionService> getActiveSessions() {
		return m_sessions;
	}
		
	public DashBoardConfig getConfig() {
		return m_config;
	}
	
	public synchronized void unregisterSession(DashBoardSessionService session) {
		session.cleanSessionObjects();
		logger.info("unregistering DashBoardSessionService: " + session);
		m_sessions.remove(session);		
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
