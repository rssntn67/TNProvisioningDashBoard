package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;
import java.sql.SQLException;

import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.dao.TNDao;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;


public class DashBoardService extends VaadinServletService implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
			
	private TNDao m_tnDao;
	private DashBoardConfig m_config;

	private boolean m_initDb = true;
    
	public DashBoardService(VaadinServlet servlet,
			DeploymentConfiguration deploymentConfiguration) {
		super(servlet, deploymentConfiguration);
	}
	
    public void init() throws SQLException {
    	if (m_initDb) {
    		m_tnDao.init("org.postgresql.Driver", m_config.getDbUrl(), m_config.getDbUsername(), m_config.getDbPassword());
    		m_initDb = false;
    	}
    }
	
	public TNDao getTnDao() {
		return m_tnDao;
	}

	public void setTnDao(TNDao tnDao) {
		m_tnDao = tnDao;
	}

	public DashBoardConfig getConfig() {
		return m_config;
	}

	public void setConfig(DashBoardConfig config) {
		m_config = config;
	}
	
	

}
