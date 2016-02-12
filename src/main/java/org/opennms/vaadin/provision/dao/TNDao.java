package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.FastDevice;
import org.opennms.vaadin.provision.model.FastService;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.Vrf;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
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

	private SQLContainer m_snmpprofilecontainer;
	private SQLContainer m_backupprofilecontainer;
	private SQLContainer m_vrfcontainer;
	private SQLContainer m_dnsdomaincontainer;
	private SQLContainer m_dnssubdomaincontainer;

	public TNDao() {
	}

	public void init(String driver, String dburl, String username, String password) throws SQLException {
		m_pool = new SimpleJDBCConnectionPool(driver, dburl, username, password);
        
		TableQuery snmptq = new TableQuery("snmpprofiles", m_pool);
		snmptq.setVersionColumn("versionid");
        m_snmpprofilecontainer = new SQLContainer(snmptq);
        
        TableQuery bcktq = new TableQuery("backupprofiles", m_pool);
		bcktq.setVersionColumn("versionid");
		m_backupprofilecontainer = new SQLContainer(bcktq);

		TableQuery vrftq = new TableQuery("vrf", m_pool);
		vrftq.setVersionColumn("versionid");
	    m_vrfcontainer =  new SQLContainer(vrftq);	
	    
	    TableQuery dnstq = new TableQuery("dnsdomains", m_pool);
	    dnstq.setVersionColumn("versionid");
	    m_dnsdomaincontainer =  new SQLContainer(dnstq);	
	    
	    TableQuery sdnstq = new TableQuery("dnssubdomains", m_pool);
	    sdnstq.setVersionColumn("versionid");
	    m_dnssubdomaincontainer =  new SQLContainer(sdnstq);	
	}

	public void destroy() {
		if (m_pool != null)
			m_pool.destroy();
		m_snmpprofilecontainer=null;
		m_backupprofilecontainer=null;
		m_vrfcontainer =null;
		m_dnsdomaincontainer = null;
		m_dnssubdomaincontainer = null;
	}

	@SuppressWarnings("unchecked")
	public Map<String, SnmpProfile> getSnmpProfiles() {
    	Map<String, SnmpProfile> snmpProfiles = new HashMap<String, SnmpProfile>();
        for (Iterator<?> i = m_snmpprofilecontainer.getItemIds().iterator(); i.hasNext();) {
			Item snmpprofiletableRow = m_snmpprofilecontainer.getItem(i.next());
			snmpProfiles.put(snmpprofiletableRow.getItemProperty("name").getValue().toString(),
					new SnmpProfile(snmpprofiletableRow.getItemProperty("name"),snmpprofiletableRow.getItemProperty("community"), 
							snmpprofiletableRow.getItemProperty("version"), 
							snmpprofiletableRow.getItemProperty("timeout")));
		}
		return snmpProfiles;
	}
	
	public SnmpProfile getSnmpProfile(String name) {
			return getSnmpProfiles().get(name);
	}
		
	@SuppressWarnings("unchecked")
	public Map<String, BackupProfile> getBackupProfiles() {
    	Map<String, BackupProfile> backupProfiles = new HashMap<String, BackupProfile>();
		for (Iterator<?> i = m_backupprofilecontainer.getItemIds().iterator(); i.hasNext();) {
			Item backupprofiletableRow = m_backupprofilecontainer.getItem(i.next());
			backupProfiles.put(backupprofiletableRow.getItemProperty("name").getValue().toString(),
					new BackupProfile(backupprofiletableRow.getItemProperty("name"),backupprofiletableRow.getItemProperty("username"), 
							backupprofiletableRow.getItemProperty("password"), 
							backupprofiletableRow.getItemProperty("enable"),
							backupprofiletableRow.getItemProperty("connection"), 
							backupprofiletableRow.getItemProperty("auto_enable")
							));
		}
		return backupProfiles;
	}

	public BackupProfile getBackupProfile(String name) throws SQLException {
    	return getBackupProfiles().get(name);
	}

	public Container getSnmpProfileContainer() {
		return m_snmpprofilecontainer;
	}

	public Container getBackupProfileContainer() {
		return m_backupprofilecontainer;
	}

	public Container getVrfContainer() {
    	return m_vrfcontainer;
    }

    public Map<String,Vrf> getVrfs() {
    	Map<String, Vrf> vrf = new HashMap<String, Vrf>();
		for (Iterator<?> i = m_vrfcontainer.getItemIds().iterator(); i.hasNext();) {
			Item vrftableRow = m_vrfcontainer.getItem(i.next());
			vrf.put(vrftableRow.getItemProperty("name").getValue().toString(),
					new Vrf(vrftableRow.getItemProperty("name").getValue().toString(),
							vrftableRow.getItemProperty("notifylevel").getValue().toString(),
							vrftableRow.getItemProperty("networklevel").getValue().toString(),
							vrftableRow.getItemProperty("dnsdomain").getValue().toString(),
							vrftableRow.getItemProperty("thresholdlevel").getValue().toString(),
							vrftableRow.getItemProperty("backupprofile").getValue().toString(),
							vrftableRow.getItemProperty("snmpprofile").getValue().toString()
							));
		}
    	return vrf;
    }

    public List<FastDevice> getFastDevices() {
    	return new ArrayList<FastDevice>();
    }
    
    public List<FastService> getFastService() {
    	return new ArrayList<FastService>();
    }

	public SQLContainer getDnsDomainContainer() {
		return m_dnsdomaincontainer;
	}

	public SQLContainer getDnsSubDomainContainer() {
		return m_dnssubdomaincontainer;
	}

	public List<String> getSubdomains() {
    	List<String> subdomains = new ArrayList<String>();
		for (Iterator<?> i = m_dnssubdomaincontainer.getItemIds().iterator(); i.hasNext();) {
			Item dnssubdomaintableRow = m_dnssubdomaincontainer.getItem(i.next());
			subdomains.add(dnssubdomaintableRow.getItemProperty("dnssubdomain").getValue().toString());
		}
		return subdomains;
	}

	public List<String> getDomains() {
    	List<String> domains = new ArrayList<String>();
		for (Iterator<?> i = m_dnsdomaincontainer.getItemIds().iterator(); i.hasNext();) {
			Item dnsdomaintableRow = m_dnsdomaincontainer.getItem(i.next());
			domains.add(dnsdomaintableRow.getItemProperty("dnsdomain").getValue().toString());
		}
		return domains;
	}

}
