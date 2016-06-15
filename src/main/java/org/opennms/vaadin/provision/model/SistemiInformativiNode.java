package org.opennms.vaadin.provision.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.vaadin.provision.core.DashBoardUtils;

public class SistemiInformativiNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private Set<String> m_updatemap = new HashSet<String>();

	private List<String> m_categoriesToAdd = new ArrayList<String>();
	private List<String> m_categoriesToDel = new ArrayList<String>();
	private List<String> m_interfToAdd = new ArrayList<String>();
	private List<String> m_interfToDel = new ArrayList<String>();

	private Map<String,Set<String>> m_serviceToAdd = new HashMap<String, Set<String>>();
	private Map<String,Set<String>> m_serviceToDel = new HashMap<String, Set<String>>();
	// This contains all the ip->service in secondary table but does not contain primary->icmp and primary-snmp
	private Map<String,Set<String>> m_serviceMap;  

	private String m_label;
	private String m_descr;
	private String m_hostname;
	private String m_vrf;
	private String m_primary;

	private String[] m_serverLevelCategory;
	private String m_managedByCategory;
	private String m_notifCategory;
	private String m_optionalCategory;
	private String m_prodCategory;
	private String m_TrentinoNetworkCategory;

	private String m_snmpProfile;
	
	private String m_city;
	private String m_address1;
	private String m_description;
	private String m_building;
	private String m_leaseExpires;
	private String m_lease;
	private String m_vendorPhone;
	private String m_vendor;
	private String m_slot;
	private String m_rack;
	private String m_room;
	private String m_operatingSystem;
	private String m_dateInstalled;
	private String m_assetNumber;
	private String m_serialNumber;
	private String m_category;
	private String m_modelNumber;
	private String m_manufacturer;

	private String m_foreignId;
	
	protected boolean m_valid = true;
	
	public SistemiInformativiNode(
			Map<String, Set<String>> serviceMap, String descr, String hostname,
			String vrf, String primary, String[] serverLevelCategory,
			String managedByCategory, String notifCategory,
			String optionalCategory, String prodCategory, String tnCategory,
			String city, String address1, String description, String building,
			String leaseExpires, String lease, String vendorPhone,
			String vendor, String slot, String rack, String room,
			String operatingSystem, String dateInstalled, String assetNumber,
			String serialNumber, String category, String modelNumber,
			String manufacturer, String foreignId, boolean valid) {
		super();
		m_serviceMap = serviceMap;
		m_descr = descr;
		m_hostname = hostname;
		m_vrf = vrf;
		m_primary = primary;
		m_serverLevelCategory = serverLevelCategory;
		m_managedByCategory = managedByCategory;
		m_notifCategory = notifCategory;
		m_optionalCategory = optionalCategory;
		m_prodCategory = prodCategory;
		m_TrentinoNetworkCategory = tnCategory;
		m_city = city;
		m_address1 = address1;
		m_description = description;
		m_building = building;
		m_leaseExpires = leaseExpires;
		m_lease = lease;
		m_vendorPhone = vendorPhone;
		m_vendor = vendor;
		m_slot = slot;
		m_rack = rack;
		m_room = room;
		m_operatingSystem = operatingSystem;
		m_dateInstalled = dateInstalled;
		m_assetNumber = assetNumber;
		m_serialNumber = serialNumber;
		m_category = category;
		m_modelNumber = modelNumber;
		m_manufacturer = manufacturer;
		m_foreignId = foreignId;
		m_valid = valid;
	}

	public SistemiInformativiNode(String label) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";
						
		m_descr="Provided by Provision Dashboard";
		
		m_valid=false;
		m_serviceMap = new HashMap<String, Set<String>>();
	}
	
	public void clear() {
		m_updatemap.clear();
		m_interfToAdd.clear();
		m_interfToDel.clear();
		m_categoriesToDel.clear();
		m_categoriesToAdd.clear();
		m_serviceToAdd.clear();
		m_serviceToDel.clear();
	}


	public String getForeignId() {
		return m_foreignId;
	}

	public String getNodeLabel() {
		if (m_label == null)
			return m_hostname.toLowerCase() + "." + m_vrf;
		return m_label;
	}

	public List<String> getCategoriesToAdd() {
		return m_categoriesToAdd;
	}

	public List<String> getCategoriesToDel() {
		return m_categoriesToDel;
	}

	public List<String> getInterfToAdd() {
		return m_interfToAdd;
	}

	public List<String> getInterfToDel() {
		return m_interfToDel;
	}
	
	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		if (city == null)
			city="";
		if (city.equals(m_city))
			return;
		m_updatemap.add(DashBoardUtils.CITY);
		m_city = city;		
	}
		
	public String getVrf() {
		return m_vrf;
	}

	public void setVrf(String vrf) {
		if (m_vrf != null && m_vrf.equals(vrf))
			return;
		m_vrf = vrf;
		m_updatemap.add(DashBoardUtils.VRF);
	}

    public String getDescr() {
		return m_descr;
	}

	public void setDescr(String descr) {
		m_descr = descr;
	}
	
	public String getSnmpProfile() {
		return m_snmpProfile;
	}

	public void setSnmpProfileWithOutUpdating(String snmpProfile) {
		m_snmpProfile = snmpProfile;
	}
	
	public void setSnmpProfile(String snmpProfile) {
		if (m_snmpProfile != null && m_snmpProfile.equals(snmpProfile))
			return;
		m_updatemap.add(DashBoardUtils.SNMP_PROFILE);
		m_snmpProfile = snmpProfile;
	}
	
	public String getAddress1() {
		return m_address1;
	}
	public void setAddress1(String address) {
		if (address == null)
			address = "";
		if (address.equals(m_address1))
			return;
		m_updatemap.add(DashBoardUtils.ADDRESS1);
		m_address1 = address;
	}
	
	public String getHostname() {
		return m_hostname;
	}
	
	public void setHostname(String hostname) {
		if (m_hostname != null && m_hostname.equals(hostname))
			return;
		m_label = null;
		m_hostname = hostname;
		m_updatemap.add(DashBoardUtils.HOST);
	}

	public String getPrimary() {
		return m_primary;
	}

	public void setPrimary(String primary) {
		if (m_primary != null && m_primary.equals(primary))
			return;
		if ( m_primary != null && !m_serviceMap.containsKey(m_primary)) 
			m_interfToDel.add(new String(m_primary));
		if (m_primary != null && m_serviceMap.containsKey(m_primary)) {
			if (!m_serviceToDel.containsKey(m_primary))
				m_serviceToDel.put(m_primary, new HashSet<String>());
			m_serviceToDel.get(m_primary).add("ICMP");
			m_serviceToDel.get(m_primary).add("SNMP");
		}
		if (!m_serviceMap.containsKey(primary))
			m_interfToAdd.add(primary);
		if (!m_serviceToAdd.containsKey(primary))
			m_serviceToAdd.put(primary, new HashSet<String>());
		m_serviceToAdd.get(primary).add("ICMP");
		m_serviceToAdd.get(primary).add("SNMP");
		m_updatemap.add(DashBoardUtils.PRIMARY);
		
		m_primary = primary;
	}
		
	public boolean isValid() {
		return m_valid;
	}

	public void setValid(boolean valid) {
		m_valid = valid;
	}	
	
	public Set<String> getUpdatemap() {
		return m_updatemap;
	}

	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}

	public Map<String, Set<String>> getServiceToAdd() {
		return m_serviceToAdd;
	}

	public Map<String, Set<String>> getServiceToDel() {
		return m_serviceToDel;
	}

	public Map<String, Set<String>> getServiceMap() {
		return m_serviceMap;
	}

	public String getLabel() {
		return m_label;
	}

	public String[] getServerLevelCategory() {
		return m_serverLevelCategory;
	}

	public String getManagedByCategory() {
		return m_managedByCategory;
	}

	public String getNotifCategory() {
		return m_notifCategory;
	}

	public String getOptionalCategory() {
		return m_optionalCategory;
	}

	public String getDescription() {
		return m_description;
	}

	public String getBuilding() {
		return m_building;
	}

	public String getLeaseExpires() {
		return m_leaseExpires;
	}

	public String getLease() {
		return m_lease;
	}

	public String getVendorPhone() {
		return m_vendorPhone;
	}

	public String getVendor() {
		return m_vendor;
	}

	public String getSlot() {
		return m_slot;
	}

	public String getRack() {
		return m_rack;
	}

	public String getRoom() {
		return m_room;
	}

	public String getOperatingSystem() {
		return m_operatingSystem;
	}

	public String getDateInstalled() {
		return m_dateInstalled;
	}

	public String getAssetNumber() {
		return m_assetNumber;
	}

	public String getSerialNumber() {
		return m_serialNumber;
	}

	public String getCategory() {
		return m_category;
	}

	public String getModelNumber() {
		return m_modelNumber;
	}

	public String getManufacturer() {
		return m_manufacturer;
	}

	public String getProdCategory() {
		return m_prodCategory;
	}

	public String getTrentinoNetworkCategory() {
		return m_TrentinoNetworkCategory;
	}


	public void setServerLevelCategory(String[] serverLevelCategory) {
		
		if (m_serverLevelCategory == null && serverLevelCategory == null)
			return;
		
		if (m_serverLevelCategory != null && serverLevelCategory == null) {
			if (m_serverLevelCategory[0] != null)
				m_categoriesToDel.add(new String(m_serverLevelCategory[0]));
			if (m_serverLevelCategory[1] != null)
				m_categoriesToDel.add(new String(m_serverLevelCategory[1]));
			m_serverLevelCategory = serverLevelCategory;
			return;
		}
		
		if (m_serverLevelCategory == null && serverLevelCategory != null) {
			if (serverLevelCategory[0] != null)
				m_categoriesToAdd.add(new String(serverLevelCategory[0]));
			if (serverLevelCategory[1] != null)
				m_categoriesToAdd.add(new String(serverLevelCategory[1]));
			m_serverLevelCategory = serverLevelCategory;
			return;
		}

		if (m_serverLevelCategory[0] != null && m_serverLevelCategory[0].equals(serverLevelCategory[0]) && 
			    m_serverLevelCategory[1] != null && m_serverLevelCategory[1].equals(serverLevelCategory[1])) {
				return;
		}
		

		if (m_serverLevelCategory[0] != null && m_serverLevelCategory[0].equals(serverLevelCategory[0])) {
			if (m_serverLevelCategory[1] != null)
				m_categoriesToDel.add(new String(m_serverLevelCategory[1]));
			if (serverLevelCategory[1] != null)
				m_categoriesToAdd.add(serverLevelCategory[1]);
		} else if (m_serverLevelCategory[1] != null && m_serverLevelCategory[1].equals(serverLevelCategory[1])) {
			if (m_serverLevelCategory[0] != null)
				m_categoriesToDel.add(new String(m_serverLevelCategory[0]));
			if (serverLevelCategory[0] != null)
				m_categoriesToAdd.add(serverLevelCategory[0]);
		}
		m_serverLevelCategory = serverLevelCategory;
	}

	public void setTrentinoNetworkCategory(String tnCategory) {
		if (m_TrentinoNetworkCategory != null && m_TrentinoNetworkCategory.equals(tnCategory) )
			return;
		m_categoriesToAdd.add(tnCategory);
		
		m_TrentinoNetworkCategory = tnCategory;
	}

	public void setProdCategory(String prodCategory) {
		if (m_prodCategory != null && m_prodCategory.equals(prodCategory) )
			return;
		if (m_prodCategory != null)
			m_categoriesToDel.add(new String(m_prodCategory));
		if (prodCategory != null)
			m_categoriesToAdd.add(prodCategory);
		
		m_prodCategory = prodCategory;
	}


	public void setManagedByCategory(String managedByCategory) {
		if (m_managedByCategory != null && m_managedByCategory.equals(managedByCategory) )
			return;
		if (m_managedByCategory != null)
			m_categoriesToDel.add(new String(m_managedByCategory));
		if (managedByCategory != null)
			m_categoriesToAdd.add(managedByCategory);
		
		m_managedByCategory = managedByCategory;
	}

	public void setNotifCategory(String notifCategory) {
		if (m_notifCategory != null && m_notifCategory.equals(notifCategory) )
			return;
		if (m_notifCategory != null)
			m_categoriesToDel.add(new String(m_notifCategory));
		if (notifCategory != null)
			m_categoriesToAdd.add(notifCategory);
		
		m_notifCategory = notifCategory;
	}

	public void setOptionalCategory(String optionalCategory) {
		if (m_optionalCategory != null && m_optionalCategory.equals(optionalCategory) )
			return;
		if (m_optionalCategory != null)
			m_categoriesToDel.add(new String(m_optionalCategory));
		if (optionalCategory != null)
			m_categoriesToAdd.add(optionalCategory);
		m_optionalCategory = optionalCategory;
	}

	public void setDescription(String description) {
		if (description == null)
			description="";
		if (description.equals(m_description) )
			return;
		m_updatemap.add(DashBoardUtils.DESCRIPTION);
		m_description = description;
	}

	public void setBuilding(String building) {
		if (building == null)
			building="";
		if (building.equals(m_building) )
			return;
		m_updatemap.add(DashBoardUtils.BUILDING);
		m_building = building;
	}

	public void setLeaseExpires(String leaseExpires) {
		if (leaseExpires == null)
			leaseExpires="";
		if (leaseExpires.equals(m_leaseExpires) )
			return;
		m_updatemap.add(DashBoardUtils.LEASEEXPIRES);
		m_leaseExpires = leaseExpires;
	}

	public void setLease(String lease) {
		if (lease == null)
			lease="";
		if (lease.equals(m_lease) )
			return;
		m_updatemap.add(DashBoardUtils.LEASE);
		m_lease = lease;
	}

	public void setVendorPhone(String vendorPhone) {
		if (vendorPhone == null)
			vendorPhone="";
		if (vendorPhone.equals(m_vendorPhone) )
			return;
		m_updatemap.add(DashBoardUtils.VENDORPHONE);
		m_vendorPhone = vendorPhone;
	}

	public void setVendor(String vendor) {
		if (vendor == null)
			vendor="";
		if (vendor.equals(m_vendor) )
			return;
		m_updatemap.add(DashBoardUtils.VENDOR);
		m_vendor = vendor;
	}

	public void setSlot(String slot) {
		if (slot == null)
			slot="";
		if (slot.equals(m_slot) )
			return;
		m_updatemap.add(DashBoardUtils.SLOT);
		m_slot = slot;
	}

	public void setRack(String rack) {
		if (rack == null)
			rack="";
		if (rack.equals(m_rack) )
			return;
		m_updatemap.add(DashBoardUtils.RACK);
		m_rack = rack;
	}

	public void setRoom(String room) {
		if (room == null)
			room="";
		if (room.equals(m_room) )
			return;
		m_updatemap.add(DashBoardUtils.ROOM);
		m_room = room;
	}

	public void setOperatingSystem(String operatingSystem) {
		if (operatingSystem == null)
			operatingSystem="";
		if (operatingSystem.equals(m_operatingSystem) )
			return;
		m_updatemap.add(DashBoardUtils.OPERATINGSYSTEM);
		m_operatingSystem = operatingSystem;
	}

	public void setDateInstalled(String dateInstalled) {
		if (dateInstalled == null)
			dateInstalled="";
		if (dateInstalled.equals(m_dateInstalled) )
			return;
		m_updatemap.add(DashBoardUtils.DATEINSTALLED);
		m_dateInstalled = dateInstalled;
	}

	public void setAssetNumber(String assetNumber) {
		if (assetNumber == null)
			assetNumber="";
		if (assetNumber.equals(m_assetNumber) )
			return;
		m_updatemap.add(DashBoardUtils.ASSETNUMBER);
		m_assetNumber = assetNumber;
	}

	public void setSerialNumber(String serialNumber) {
		if (serialNumber == null)
			serialNumber="";
		if (serialNumber.equals(m_serialNumber) )
			return;
		m_updatemap.add(DashBoardUtils.SERIALNUMBER);
		m_serialNumber = serialNumber;
	}

	public void setCategory(String category) {
		if (category == null)
			category="Server";
		if (category.equals(m_category) )
			return;
		m_updatemap.add(DashBoardUtils.CATEGORY);
		m_category = category;
	}

	public void setModelNumber(String modelNumber) {
		if (modelNumber == null)
			modelNumber="";
		if (modelNumber.equals(m_modelNumber) )
			return;
		m_updatemap.add(DashBoardUtils.MODELNUMBER);
		m_modelNumber = modelNumber;
	}

	public void setManufacturer(String manufacturer) {
		if (manufacturer == null)
			manufacturer="";
		if (manufacturer.equals(m_manufacturer) )
			return;
		m_updatemap.add(DashBoardUtils.MANUFACTURER);
		m_manufacturer = manufacturer;
	}
	
	public void addService(String ip, String service) {
		if ("ICMP".equals(service) || "SNMP".equals(service))
				return;
		if (m_interfToDel.contains(ip))
			m_interfToDel.remove(ip);
		if (!m_serviceMap.containsKey(ip)) {
			if (!ip.equals(m_primary))
				m_interfToAdd.add(ip);
			m_serviceMap.put(ip, new HashSet<String>());
		}
		m_serviceMap.get(ip).add(service);
		
		if (!m_serviceToAdd.containsKey(ip))
			m_serviceToAdd.put(ip, new HashSet<String>());
		m_serviceToAdd.get(ip).add(service);
	}

	public void delService(String ip, String service) {
		if ("ICMP".equals(service) || "SNMP".equals(service))
			return;
		if (!m_serviceMap.containsKey(ip))
			return;
		m_serviceMap.get(ip).remove(service);
		boolean removeip = false;
		if (m_serviceMap.get(ip).isEmpty()) 
			removeip = true;
		if (removeip) {
			m_serviceMap.remove(ip);
			if (!ip.equals(m_primary)) {
				m_serviceToDel.remove(ip);
				m_interfToDel.add(ip);
			}
		} else {
			if (!m_serviceToDel.containsKey(ip))
				m_serviceToDel.put(ip, new HashSet<String>());
			m_serviceToDel.get(ip).add(service);
		}			
	}

}
