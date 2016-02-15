package org.opennms.vaadin.provision.model;

import com.vaadin.data.Property;

public class FastServiceDevice {

	private final String m_hostname;
	private final String m_ipaddr;
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
	private final boolean m_master;
	private final String m_snmpprofile;
	private final String m_backupprofile;
	private final boolean m_notmonitoring;
	private final boolean m_saveconfig;
	private final String m_notifyCategory;
	private final String m_orderCode;
	private final String m_deviceType;

	public FastServiceDevice(
			Property<String> hostname, 
			Property<String> ipaddr, 
			Property<String> ipAddrLan,
			Property<String> netMaskLan, 
			Property<String> serialNumber, 
			Property<String> addressDescr,
			Property<String> addressName, 
			Property<String> addressNumber, 
			Property<String> floor,
			Property<String> room, 
			Property<String> city, 
			Property<String> istat, 
			Property<Boolean> master,
			Property<String> snmpprofile, 
			Property<String> backupprofile, 
			Property<Boolean> notmonitoring,
			Property<Boolean> saveconfig,
			Property<String> notify, 
			Property<String> orderCode,
			Property<String> deviceType) {
		super();
		if (hostname != null)
			m_hostname = hostname.getValue();
		else 
			m_hostname = null;
		
		if (ipaddr != null)
			m_ipaddr = ipaddr.getValue();
		else 
			m_ipaddr = null;
		
		if (ipAddrLan != null)
			m_ipAddrLan = ipAddrLan.getValue();
		else 
			m_ipAddrLan = null;
		
		if (netMaskLan != null)
			m_netMaskLan = netMaskLan.getValue();
		else
			m_netMaskLan = null;
		
		if (serialNumber != null)
			m_serialNumber = serialNumber.getValue();
		else 
			m_serialNumber = null;
		
		if (addressDescr != null)
			m_addressDescr = addressDescr.getValue();
		else
			m_addressDescr = null;
		
		if (addressName != null)
			m_addressName = addressName.getValue();
		else
			m_addressName = null;
		
		if (addressNumber != null)
			m_addressNumber = addressNumber.getValue();
		else 
			m_addressNumber = null;
		
		if (floor !=  null)
			m_floor = floor.getValue();
		else 
			m_floor = null;
		
		if (room != null)
			m_room = room.getValue();
		else 
			m_room = null;
		
		if (city != null)
			m_city = city.getValue();
		else
			m_city = null;
		
		if (istat != null)
			m_istat = istat.getValue();
		else
			m_istat = null;
		
		if (master != null && master.getValue() != null ) 
			m_master = master.getValue();
		else
			m_master = false;
		
		if (snmpprofile != null)
			m_snmpprofile = snmpprofile.getValue();
		else
			m_snmpprofile = null;
		
		if (backupprofile != null)
			m_backupprofile = backupprofile.getValue();
		else
			m_backupprofile = null;
		
		if (notmonitoring != null && notmonitoring.getValue() != null )
			m_notmonitoring = notmonitoring.getValue();
		else
			m_notmonitoring = false;
		
		if (saveconfig != null && saveconfig.getValue() != null )
			m_saveconfig = saveconfig.getValue();
		else
			m_saveconfig = true;
		
		if (notify != null)
			m_notifyCategory = notify.getValue();
		else
			m_notifyCategory = null;
		
		if (orderCode != null)
			m_orderCode = orderCode.getValue();
		else
			m_orderCode = null;
		
		if (deviceType != null)
			m_deviceType = deviceType.getValue();
		else
			m_deviceType = null;
		
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

	public String getIpaddr() {
		return m_ipaddr;
	}

	public boolean isMaster() {
		return m_master;
	}

	public boolean isNotmonitoring() {
		return m_notmonitoring;
	}

	public boolean isSaveconfig() {
		return m_saveconfig;
	}

	public String getNotifyCategory() {
		return m_notifyCategory;
	}

	public String getOrderCode() {
		return m_orderCode;
	}

	public String getDeviceType() {
		return m_deviceType;
	}

}
