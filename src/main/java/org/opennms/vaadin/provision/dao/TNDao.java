package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;

import org.opennms.vaadin.provision.model.BackupProfileContainer;
import org.opennms.vaadin.provision.model.DnsDomainContainer;
import org.opennms.vaadin.provision.model.DnsSubDomainContainer;
import org.opennms.vaadin.provision.model.FastServiceDeviceContainer;
import org.opennms.vaadin.provision.model.FastServiceLinkContainer;
import org.opennms.vaadin.provision.model.JobContainer;
import org.opennms.vaadin.provision.model.SnmpProfileContainer;
import org.opennms.vaadin.provision.model.VrfContainer;

import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;

public class TNDao {

	public static final String[] m_network_levels = {
		"Backbone",
		"Distribuzione",
		"Accesso"
	};

	public static final String[] m_notify_levels = {
		"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"
	};

	public static final String[] m_threshold_levels = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};
	
	private JDBCConnectionPool m_pool; 

	private SnmpProfileContainer       m_snmpprofilecontainer;
	private BackupProfileContainer     m_backupprofilecontainer;
	private VrfContainer               m_vrfcontainer;
	private DnsDomainContainer         m_dnsdomaincontainer;
	private DnsSubDomainContainer      m_dnssubdomaincontainer;
	private FastServiceDeviceContainer m_fastservicedevicecontainer;
	private FastServiceLinkContainer   m_fastservicelinkcontainer;
	private JobContainer               m_jobcontainer;

	public TNDao() {
	}

	public void init(String driver, String dburl, String username, String password) throws SQLException {
		m_pool = new SimpleJDBCConnectionPool(driver, dburl, username, password);
        
		TableQuery snmptq = new TableQuery("snmpprofiles", m_pool);
		snmptq.setVersionColumn("versionid");
        m_snmpprofilecontainer = new SnmpProfileContainer(snmptq);
        
        TableQuery bcktq = new TableQuery("backupprofiles", m_pool);
		bcktq.setVersionColumn("versionid");
		m_backupprofilecontainer = new BackupProfileContainer(bcktq);

		TableQuery vrftq = new TableQuery("vrf", m_pool);
		vrftq.setVersionColumn("versionid");
	    m_vrfcontainer =  new VrfContainer(vrftq);	
	    
	    TableQuery dnstq = new TableQuery("dnsdomains", m_pool);
	    dnstq.setVersionColumn("versionid");
	    m_dnsdomaincontainer =  new DnsDomainContainer(dnstq);	
	    
	    TableQuery sdnstq = new TableQuery("dnssubdomains", m_pool);
	    sdnstq.setVersionColumn("versionid");
	    m_dnssubdomaincontainer =  new DnsSubDomainContainer(sdnstq);

	    m_fastservicedevicecontainer = new FastServiceDeviceContainer
	    		(new FreeformQuery("select * from fastservicedevices", m_pool));

		m_fastservicelinkcontainer = new FastServiceLinkContainer
				(new FreeformQuery("select * from fastservicelink", m_pool));

	    TableQuery jtq = new TableQuery("jobs", m_pool);
	    jtq.setVersionColumn("versionid");
		m_jobcontainer = new JobContainer(jtq);
		
	}

	public void destroy() {
		if (m_pool != null)
			m_pool.destroy();
		m_snmpprofilecontainer=null;
		m_backupprofilecontainer=null;
		m_vrfcontainer =null;
		m_dnsdomaincontainer = null;
		m_dnssubdomaincontainer = null;
		m_fastservicedevicecontainer = null;
		m_fastservicelinkcontainer = null;
		m_jobcontainer = null;
	}
	
	public SnmpProfileContainer getSnmpProfileContainer() {
		return m_snmpprofilecontainer;
	}

	public BackupProfileContainer getBackupProfileContainer() {
		return m_backupprofilecontainer;
	}

	public VrfContainer getVrfContainer() {
    	return m_vrfcontainer;
    }

	public FastServiceDeviceContainer getFastServiceDeviceContainer() {
		return m_fastservicedevicecontainer;
    }
    
	public FastServiceLinkContainer getFastServiceLinkContainer() {
		return m_fastservicelinkcontainer;
    }

	public DnsDomainContainer getDnsDomainContainer() {
		return m_dnsdomaincontainer;
	}

	public DnsSubDomainContainer getDnsSubDomainContainer() {
		return m_dnssubdomaincontainer;
	}

	public JobContainer getJobContainer() {
		return m_jobcontainer;
	}

	public JDBCConnectionPool getPool() {
		return m_pool;
	}
}
