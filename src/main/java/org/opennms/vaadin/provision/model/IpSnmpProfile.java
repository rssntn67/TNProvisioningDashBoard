package org.opennms.vaadin.provision.model;

public class IpSnmpProfile {
	String m_ip;
	String m_snmprofile;
	
	public IpSnmpProfile() {
		
	}

	public IpSnmpProfile(String ip,
			String snmpprofile
			) {
		super();
		m_ip=ip;
		m_snmprofile = snmpprofile;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public String getSnmprofile() {
		return m_snmprofile;
	}

	public void setSnmprofile(String snmprofile) {
		m_snmprofile = snmprofile;
	}
	
	
	

}

