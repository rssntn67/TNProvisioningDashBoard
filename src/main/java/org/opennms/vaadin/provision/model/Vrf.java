package org.opennms.vaadin.provision.model;

public class Vrf {
	private final String m_name; 
	private final String m_notifylevel;
	private final String m_networklevel;
	private final String m_dnsdomain;
	private final String m_thresholdlevel;
	private final String m_backupprofile;
	private final String m_snmpprofile;
	public Vrf(String name, String notifylevel, String networklevel,
			String dnsdomain, String thresholdlevel, String backupprofile,
			String snmpprofile) {
		super();
		m_name = name;
		m_notifylevel = notifylevel;
		m_networklevel = networklevel;
		m_dnsdomain = dnsdomain;
		m_thresholdlevel = thresholdlevel;
		m_backupprofile = backupprofile;
		m_snmpprofile = snmpprofile;
	}
	public String getName() {
		return m_name;
	}
	public String getNotifylevel() {
		return m_notifylevel;
	}
	public String getNetworklevel() {
		return m_networklevel;
	}
	public String getDnsdomain() {
		return m_dnsdomain;
	}
	public String getThresholdlevel() {
		return m_thresholdlevel;
	}
	public String getBackupprofile() {
		return m_backupprofile;
	}
	public String getSnmpprofile() {
		return m_snmpprofile;
	}
	
}
