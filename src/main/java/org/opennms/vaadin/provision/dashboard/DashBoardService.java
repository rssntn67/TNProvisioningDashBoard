package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.dao.BackupProfileDao;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.DnsSubDomainDao;
import org.opennms.vaadin.provision.dao.FastServiceDeviceDao;
import org.opennms.vaadin.provision.dao.FastServiceLinkDao;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.dao.JobLogDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.VrfDao;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServletService;


public class DashBoardService extends VaadinServletService implements Serializable {

	public boolean isInitdb() {
		return m_initdb;
	}

	public void setInitdb(boolean initdb) {
		m_initdb = initdb;
	}

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
			
	private JDBCConnectionPool m_pool; 
	private DashBoardConfig m_config;
	private SnmpProfileDao       m_snmpprofilecontainer;
	private BackupProfileDao     m_backupprofilecontainer;
	private VrfDao               m_vrfcontainer;
	private DnsDomainDao         m_dnsdomaincontainer;
	private DnsSubDomainDao      m_dnssubdomaincontainer;
	private FastServiceDeviceDao m_fastservicedevicecontainer;
	private FastServiceLinkDao   m_fastservicelinkcontainer;
	private JobDao               m_jobcontainer;
	private JobLogDao            m_joblogcontainer;
    private boolean              m_initdb;
	
	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());

	public DashBoardService(DashboardServlet servlet,
			DeploymentConfiguration deploymentConfiguration) throws ServiceException {
		super(servlet, deploymentConfiguration);
	}
		
	public void init() throws ServiceException{
		super.init();
		try {
		m_config = new DashBoardConfig();
		m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", m_config.getDbUrl(), m_config.getDbUsername(), m_config.getDbPassword());
        
		TableQuery snmptq = new TableQuery("snmpprofiles", m_pool);
		snmptq.setVersionColumn("versionid");
        m_snmpprofilecontainer = new SnmpProfileDao(snmptq);
        
        TableQuery bcktq = new TableQuery("backupprofiles", m_pool);
		bcktq.setVersionColumn("versionid");
		m_backupprofilecontainer = new BackupProfileDao(bcktq);

		TableQuery vrftq = new TableQuery("vrf", m_pool);
		vrftq.setVersionColumn("versionid");
	    m_vrfcontainer =  new VrfDao(vrftq);	
	    
	    TableQuery dnstq = new TableQuery("dnsdomains", m_pool);
	    dnstq.setVersionColumn("versionid");
	    m_dnsdomaincontainer =  new DnsDomainDao(dnstq);	
	    
	    TableQuery sdnstq = new TableQuery("dnssubdomains", m_pool);
	    sdnstq.setVersionColumn("versionid");
	    m_dnssubdomaincontainer =  new DnsSubDomainDao(sdnstq);

	    m_fastservicedevicecontainer = new FastServiceDeviceDao
	    		(new FreeformQuery("select * from fastservicedevices", m_pool));

		m_fastservicelinkcontainer = new FastServiceLinkDao
				(new FreeformQuery("select * from fastservicelink", m_pool));

	    TableQuery jtq = new TableQuery("jobs", m_pool);
	    jtq.setVersionColumn("versionid");
		m_jobcontainer = new JobDao(jtq);
		
		TableQuery jltq = new TableQuery("joblogs", m_pool);
	    jltq.setVersionColumn("versionid");
		m_joblogcontainer = new JobLogDao(jltq);

		logger.info("connected to database: " + m_config.getDbUrl());
		} catch (Exception e) {
		logger.log(Level.SEVERE,"createServletService: cannot init postgres", e);
		m_initdb=false;
		return;
		}
		m_initdb = true;

	}
	public DashBoardConfig getConfig() {
		return m_config;
	}

	public void setConfig(DashBoardConfig config) {
		m_config = config;
	}
	
	public SnmpProfileDao getSnmpProfileContainer() {
		return m_snmpprofilecontainer;
	}

	public BackupProfileDao getBackupProfileContainer() {
		return m_backupprofilecontainer;
	}

	public VrfDao getVrfContainer() {
    	return m_vrfcontainer;
    }

	public FastServiceDeviceDao getFastServiceDeviceContainer() {
		return m_fastservicedevicecontainer;
    }
    
	public FastServiceLinkDao getFastServiceLinkContainer() {
		return m_fastservicelinkcontainer;
    }

	public DnsDomainDao getDnsDomainContainer() {
		return m_dnsdomaincontainer;
	}

	public DnsSubDomainDao getDnsSubDomainContainer() {
		return m_dnssubdomaincontainer;
	}

	public JobDao getJobContainer() {
		return m_jobcontainer;
	}

	public JobLogDao getJobLogContainer() {
		return m_joblogcontainer;
	}

	public void setSnmpprofilecontainer(SnmpProfileDao snmpprofilecontainer) {
		m_snmpprofilecontainer = snmpprofilecontainer;
	}

	public void setBackupprofilecontainer(BackupProfileDao backupprofilecontainer) {
		m_backupprofilecontainer = backupprofilecontainer;
	}

	public void setVrfcontainer(VrfDao vrfcontainer) {
		m_vrfcontainer = vrfcontainer;
	}

	public void setDnsdomaincontainer(DnsDomainDao dnsdomaincontainer) {
		m_dnsdomaincontainer = dnsdomaincontainer;
	}

	public void setDnssubdomaincontainer(DnsSubDomainDao dnssubdomaincontainer) {
		m_dnssubdomaincontainer = dnssubdomaincontainer;
	}

	public void setFastservicedevicecontainer(
			FastServiceDeviceDao fastservicedevicecontainer) {
		m_fastservicedevicecontainer = fastservicedevicecontainer;
	}

	public void setFastservicelinkcontainer(
			FastServiceLinkDao fastservicelinkcontainer) {
		m_fastservicelinkcontainer = fastservicelinkcontainer;
	}

	public void setJobcontainer(JobDao jobcontainer) {
		m_jobcontainer = jobcontainer;
	}	

	public void setJobLogcontainer(JobLogDao joblogcontainer) {
		m_joblogcontainer = joblogcontainer;
	}	

}
