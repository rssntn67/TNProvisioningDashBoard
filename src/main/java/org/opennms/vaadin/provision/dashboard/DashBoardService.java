package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
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
			
    private boolean m_init = false;

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

    Set<DashBoardSessionService> m_sessions = new HashSet<DashBoardSessionService>();
    Queue<JDBCConnectionPool> m_pools = new LinkedList<JDBCConnectionPool>();
    DashBoardConfig m_config;
    
	public DashBoardService(DashboardServlet servlet,
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		super(servlet, deploymentConfiguration);
	}
		
	@Override
	public void init() throws ServiceException {
		
		m_config = new DashBoardConfig();
		try {
			m_config.reload();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "init: cannot init configuration file", e);
			throw new ServiceException(e);
		}
		
		try {
			for (int i=0;i<3;i++)
			m_pools.add(new SimpleJDBCConnectionPool(
					"org.postgresql.Driver", m_config.getDbUrl(), m_config
							.getDbUsername(), m_config.getDbPassword()));
			logger.info("created connection pool to database: " + m_config.getDbUrl());
		} catch (SQLException e) {
			logger.log(Level.SEVERE,
					"cannot create collection pools", e);
			throw new ServiceException(e);
		}
		
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
		registerSession(sessionservice);
		return sessionservice;
	}

	public Set<DashBoardSessionService> getActiveSessions() {
		return m_sessions;
	}
	
	public Queue<JDBCConnectionPool> getPools() {
		return m_pools;
	}
	
	public DashBoardConfig getConfig() {
		return m_config;
	}

	public synchronized void registerSession(DashBoardSessionService session) {
		session.setConfig(m_config);
		JDBCConnectionPool pool = m_pools.remove();
		session.setPool(pool);
		logger.info("registering DashBoardSessionService: " + session + " with pool: " + pool);
		m_pools.offer(pool);
		m_sessions.add(session);
	}
	
	public synchronized void unregisterSession(DashBoardSessionService session) {
		logger.info("unregistering DashBoardSessionService: " + session);
		m_sessions.remove(session);		
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}
