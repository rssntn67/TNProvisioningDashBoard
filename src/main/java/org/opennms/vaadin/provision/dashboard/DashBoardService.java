package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.config.DashBoardConfig;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServletService;


public class DashBoardService extends VaadinServletService implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
			
	private JDBCConnectionPool m_pool; 
	private DashBoardConfig m_config;
    private boolean m_init = false;

	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	public DashBoardService(DashboardServlet servlet,
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		super(servlet, deploymentConfiguration);
	}
		
	public void init() throws ServiceException {
		super.init();
		try {
			m_config = new DashBoardConfig();
			m_config.reload();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"createServletService: cannot init configuration file", e);
			throw new ServiceException("Cannot init conf file", e);
		}

		try {
			m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", m_config.getDbUrl(), m_config.getDbUsername(), m_config.getDbPassword());
			logger.info("connected to database: " + getConfig().getDbUrl());
		} catch (Exception e) {
			logger.log(Level.SEVERE,"createServletService: cannot init postgres", e);
			throw new ServiceException("Cannot init database", e);
		}
		m_init = true;
	}
	
	public boolean ready() {
		return m_init;
	}

	public DashBoardConfig getConfig() {
		return m_config;
	}

	public void setConfig(DashBoardConfig config) {
		m_config = config;
	}

	public JDBCConnectionPool getPool() {
		return m_pool;
	}

	public void setPool(JDBCConnectionPool pool) {
		m_pool = pool;
	}
	

}
