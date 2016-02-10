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

	public static final String[] m_notif_categories = {
		"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"
	};

	public static final String[] m_thresh_categories = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};
	
	private JDBCConnectionPool m_pool; 

	private SQLContainer m_snmpprofilecontainer;
	private SQLContainer m_backupprofilecontainer;
	private SQLContainer m_vrfcontainer;
	private SQLContainer m_dnsdomaincontainer;
	private SQLContainer m_dnssubdomaincontainer;
	Map<String, SnmpProfile> m_snmpProfiles = new HashMap<String, SnmpProfile>();
	Map<String, BackupProfile> m_backupProfiles = new HashMap<String, BackupProfile>();
	Map<String, Vrf> m_vrf = new HashMap<String, Vrf>();
	List<String> m_subdomains = new ArrayList<String>();
	List<String> m_domains = new ArrayList<String>();

	public TNDao() {
	}

	@SuppressWarnings("unchecked")
	public void init(String driver, String dburl, String username, String password) throws SQLException {
		m_pool = new SimpleJDBCConnectionPool(driver, dburl, username, password);
        
        m_snmpprofilecontainer = new SQLContainer(new TableQuery("snmpprofiles", m_pool));
        for (Iterator<?> i = m_snmpprofilecontainer.getItemIds().iterator(); i.hasNext();) {
			Item snmpprofiletableRow = m_snmpprofilecontainer.getItem(i.next());
			m_snmpProfiles.put(snmpprofiletableRow.getItemProperty("name").getValue().toString(),
					new SnmpProfile(snmpprofiletableRow.getItemProperty("name"),snmpprofiletableRow.getItemProperty("community"), 
							snmpprofiletableRow.getItemProperty("version"), 
							snmpprofiletableRow.getItemProperty("timeout")));
		}
        
		m_backupprofilecontainer = new SQLContainer(new TableQuery("backupprofiles", m_pool));
		for (Iterator<?> i = m_backupprofilecontainer.getItemIds().iterator(); i.hasNext();) {
			Item backupprofiletableRow = m_backupprofilecontainer.getItem(i.next());
			m_backupProfiles.put(backupprofiletableRow.getItemProperty("name").getValue().toString(),
					new BackupProfile(backupprofiletableRow.getItemProperty("name"),backupprofiletableRow.getItemProperty("username"), 
							backupprofiletableRow.getItemProperty("password"), 
							backupprofiletableRow.getItemProperty("enable"),
							backupprofiletableRow.getItemProperty("connection"), 
							backupprofiletableRow.getItemProperty("auto_enable")
							));
		}

	    m_vrfcontainer =  new SQLContainer(new TableQuery("vrf", m_pool));	
		for (Iterator<?> i = m_vrfcontainer.getItemIds().iterator(); i.hasNext();) {
			Item vrftableRow = m_vrfcontainer.getItem(i.next());
			m_vrf.put(vrftableRow.getItemProperty("name").getValue().toString(),
					new Vrf(vrftableRow.getItemProperty("name").getValue().toString(),
							vrftableRow.getItemProperty("notifylevel").getValue().toString(),
							vrftableRow.getItemProperty("networklevel").getValue().toString(),
							vrftableRow.getItemProperty("dnsdomain").getValue().toString(),
							vrftableRow.getItemProperty("thresholdlevel").getValue().toString(),
							vrftableRow.getItemProperty("backupprofile").getValue().toString(),
							vrftableRow.getItemProperty("snmpprofile").getValue().toString()
							));
		}

	    m_dnsdomaincontainer =  new SQLContainer(new TableQuery("dnsdomains", m_pool));	
		for (Iterator<?> i = m_dnsdomaincontainer.getItemIds().iterator(); i.hasNext();) {
			Item dnsdomaintableRow = m_dnsdomaincontainer.getItem(i.next());
			m_domains.add(dnsdomaintableRow.getItemProperty("dnsdomain").getValue().toString());
		}

	    m_dnssubdomaincontainer =  new SQLContainer(new TableQuery("dnssubdomains", m_pool));	
		for (Iterator<?> i = m_dnssubdomaincontainer.getItemIds().iterator(); i.hasNext();) {
			Item dnssubdomaintableRow = m_dnssubdomaincontainer.getItem(i.next());
			m_subdomains.add(dnssubdomaintableRow.getItemProperty("dnssubdomain").getValue().toString());
		}


	}

	public void destroy() {
		if (m_pool != null)
			m_pool.destroy();
		m_snmpprofilecontainer=null;
		m_backupprofilecontainer=null;
		m_snmpProfiles.clear();
		m_backupProfiles.clear();
	}

	public Map<String, SnmpProfile> getSnmpProfiles() {
		return m_snmpProfiles;
	}
	
	public SnmpProfile getSnmpProfile(String name) {
		if (m_snmpProfiles != null)
			return m_snmpProfiles.get(name);
		return null;
	}
		
	public Map<String, BackupProfile> getBackupProfiles() {
		return m_backupProfiles;
	}

	public BackupProfile getBackupProfile(String name) throws SQLException {
    	return m_backupProfiles.get(name);
	}

	public Container getSnmpProfileContainer() {
		return m_snmpprofilecontainer;
	}

	public Container getBackupProfileContainer() {
		return m_backupprofilecontainer;
	}

	public Container getVrfContainer() throws SQLException {
    	return m_vrfcontainer;
    }

    public Map<String,Vrf> getVrfs() {
    	return m_vrf;
    }

    public List<FastDevice> getFastDevices() {
    	return new ArrayList<FastDevice>();
    }
    
    public List<FastService> getFastService() {
    	return new ArrayList<FastService>();
    }

	public SQLContainer getDnsdomaincontainer() {
		return m_dnsdomaincontainer;
	}

	public SQLContainer getDnssubdomaincontainer() {
		return m_dnssubdomaincontainer;
	}

	public List<String> getSubdomains() {
		return m_subdomains;
	}

	public List<String> getDomains() {
		return m_domains;
	}

}
