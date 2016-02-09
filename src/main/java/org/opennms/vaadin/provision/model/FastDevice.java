package org.opennms.vaadin.provision.model;

public class FastDevice {

	private final String m_hostname;
	private final String m_ipAddrLan;
	private final String m_netMaskLan;
	private final String m_serialNumber;
	private final String m_addressDescr;
	private final String m_addressName;
	private final String m_addressNumber;
	private final String m_floor;
	private final String m_room;
	private final String m_city;
	private final String m_istat;
	private final String m_snmpprofile;
	private final String m_backupprofile;
	public FastDevice(String hostname, String ipAddrLan, String netMaskLan,
			String serialNumber, String addressDescr, String addressName,
			String addressNumber, String floor, String room, String city,
			String istat, String snmpprofile, String backupprofile) {
		super();
		m_hostname = hostname;
		m_ipAddrLan = ipAddrLan;
		m_netMaskLan = netMaskLan;
		m_serialNumber = serialNumber;
		m_addressDescr = addressDescr;
		m_addressName = addressName;
		m_addressNumber = addressNumber;
		m_floor = floor;
		m_room = room;
		m_city = city;
		m_istat = istat;
		m_snmpprofile = snmpprofile;
		m_backupprofile = backupprofile;
	}
	public String getHostname() {
		return m_hostname;
	}
	public String getIpAddrLan() {
		return m_ipAddrLan;
	}
	public String getNetMaskLan() {
		return m_netMaskLan;
	}
	public String getSerialNumber() {
		return m_serialNumber;
	}
	public String getAddressDescr() {
		return m_addressDescr;
	}
	public String getAddressName() {
		return m_addressName;
	}
	public String getAddressNumber() {
		return m_addressNumber;
	}
	public String getFloor() {
		return m_floor;
	}
	public String getRoom() {
		return m_room;
	}
	public String getCity() {
		return m_city;
	}
	public String getIstat() {
		return m_istat;
	}
	public String getSnmpprofile() {
		return m_snmpprofile;
	}
	public String getBackupprofile() {
		return m_backupprofile;
	}
	
	
	
}
