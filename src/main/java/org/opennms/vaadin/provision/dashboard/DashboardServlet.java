package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.dao.BackupProfileDao;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.DnsSubDomainDao;
import org.opennms.vaadin.provision.dao.FastServiceDeviceDao;
import org.opennms.vaadin.provision.dao.FastServiceLinkDao;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.VrfDao;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.VaadinServlet;

public class DashboardServlet extends VaadinServlet {

	private final static Logger logger = Logger.getLogger(DashboardServlet.class.getName());

	private JDBCConnectionPool m_pool; 

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
		logger.info("servletInitialized: calling.");
        super.servletInitialized();		    
	}

	@Override 
	protected DashBoardService createServletService(DeploymentConfiguration deploymentConfiguration)  {
		DashBoardConfig config = new DashBoardConfig();
		SnmpProfileDao       snmpprofilecontainer;
		BackupProfileDao     backupprofilecontainer;
		VrfDao               vrfcontainer;
		DnsDomainDao         dnsdomaincontainer;
		DnsSubDomainDao      dnssubdomaincontainer;
		FastServiceDeviceDao fastservicedevicecontainer;
		FastServiceLinkDao   fastservicelinkcontainer;
		JobDao               jobcontainer;

		try {
		m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", config.getDbUrl(), config.getDbUsername(), config.getDbPassword());
        
		TableQuery snmptq = new TableQuery("snmpprofiles", m_pool);
		snmptq.setVersionColumn("versionid");
        snmpprofilecontainer = new SnmpProfileDao(snmptq);
        
        TableQuery bcktq = new TableQuery("backupprofiles", m_pool);
		bcktq.setVersionColumn("versionid");
		backupprofilecontainer = new BackupProfileDao(bcktq);

		TableQuery vrftq = new TableQuery("vrf", m_pool);
		vrftq.setVersionColumn("versionid");
	    vrfcontainer =  new VrfDao(vrftq);	
	    
	    TableQuery dnstq = new TableQuery("dnsdomains", m_pool);
	    dnstq.setVersionColumn("versionid");
	    dnsdomaincontainer =  new DnsDomainDao(dnstq);	
	    
	    TableQuery sdnstq = new TableQuery("dnssubdomains", m_pool);
	    sdnstq.setVersionColumn("versionid");
	    dnssubdomaincontainer =  new DnsSubDomainDao(sdnstq);

	    fastservicedevicecontainer = new FastServiceDeviceDao
	    		(new FreeformQuery("select * from fastservicedevices", m_pool));

		fastservicelinkcontainer = new FastServiceLinkDao
				(new FreeformQuery("select * from fastservicelink", m_pool));

	    TableQuery jtq = new TableQuery("jobs", m_pool);
	    jtq.setVersionColumn("versionid");
		jobcontainer = new JobDao(jtq);
		logger.info("connected to database: " + config.getDbUrl());
		} catch (SQLException e) {
		logger.log(Level.SEVERE,"createServletService: cannot init postgres", e);
		return null;
		}

		DashBoardService service = new DashBoardService(getCurrent(), deploymentConfiguration);
	    service.setConfig(config);
	    service.setBackupprofilecontainer(backupprofilecontainer);
	    service.setDnsdomaincontainer(dnsdomaincontainer);
	    service.setDnssubdomaincontainer(dnssubdomaincontainer);
	    service.setFastservicedevicecontainer(fastservicedevicecontainer);
	    service.setFastservicelinkcontainer(fastservicelinkcontainer);
	    service.setJobcontainer(jobcontainer);
	    service.setSnmpprofilecontainer(snmpprofilecontainer);
	    service.setVrfcontainer(vrfcontainer);

		logger.info("createServletService: created Dashboard Service: " + service);

	    return service;
	};
	
	
}
