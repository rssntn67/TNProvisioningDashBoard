package org.opennms.vaadin.provision.dashboard;


import java.io.Serializable;

import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.dao.BackupProfileDao;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.DnsSubDomainDao;
import org.opennms.vaadin.provision.dao.FastServiceDeviceDao;
import org.opennms.vaadin.provision.dao.FastServiceLinkDao;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.VrfDao;

import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;


public class DashBoardService extends VaadinServletService implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
			
	private DashBoardConfig m_config;
	private SnmpProfileDao       m_snmpprofilecontainer;
	private BackupProfileDao     m_backupprofilecontainer;
	private VrfDao               m_vrfcontainer;
	private DnsDomainDao         m_dnsdomaincontainer;
	private DnsSubDomainDao      m_dnssubdomaincontainer;
	private FastServiceDeviceDao m_fastservicedevicecontainer;
	private FastServiceLinkDao   m_fastservicelinkcontainer;
	private JobDao               m_jobcontainer;
    
	public DashBoardService(VaadinServlet servlet,
			DeploymentConfiguration deploymentConfiguration) {
		super(servlet, deploymentConfiguration);
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

}
