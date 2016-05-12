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

	private Map<String,List<String>> m_serviceToAdd = new HashMap<String, List<String>>();
	private Map<String,List<String>> m_serviceToDel = new HashMap<String, List<String>>();
	private Map<String,List<String>> m_serviceMap   = new HashMap<String, List<String>>();

	private String m_label;
	private String m_descr;
	private String m_hostname;
	private String m_vrf;
	private String m_primary;

	private String m_serverTypeCategory;
	private String m_serverSpecCategory;
	private String m_managedByCategory;
	private String m_notifCategory;
	private String m_optionalByCategory;

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
	
	public SistemiInformativiNode(String label) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";
						
		m_descr="Provided by Provision Dashboard";
		
		m_valid=false;
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
		if (m_city != null && m_city.equals(city))
			return;
		if (city != null)
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
		if (m_address1 !=  null && m_address1.equals(address))
			return;
		if (m_address1 ==  null && address == null)
			return;
		if (address != null ) {
			m_updatemap.add(DashBoardUtils.ADDRESS1);
		}
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
		if ( m_primary != null) 
			m_interfToDel.add(new String(m_primary));

		m_interfToAdd.add(new String(primary));
		m_primary = primary;
	}
		
	public boolean isValid() {
		return m_valid;
	}

	public void setValid(boolean valid) {
		m_valid = valid;
	}	
	
	public void clear() {
		m_updatemap.clear();
		m_interfToDel.clear();
		m_interfToAdd.clear();
		m_categoriesToDel.clear();
		m_categoriesToAdd.clear();
	}

	public Set<String> getUpdatemap() {
		return m_updatemap;
	}

	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}

	public Map<String, List<String>> getServiceToAdd() {
		return m_serviceToAdd;
	}

	public Map<String, List<String>> getServiceToDel() {
		return m_serviceToDel;
	}

	public Map<String, List<String>> getServiceMap() {
		return m_serviceMap;
	}

	public String getLabel() {
		return m_label;
	}

	public String getServerTypeCategory() {
		return m_serverTypeCategory;
	}

	public String getServerSpecCategory() {
		return m_serverSpecCategory;
	}

	public String getManagedByCategory() {
		return m_managedByCategory;
	}

	public String getNotifCategory() {
		return m_notifCategory;
	}

	public String getOptionalByCategory() {
		return m_optionalByCategory;
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

	public void setServerTypeCategory(String serverTypeCategory) {
		if (m_serverTypeCategory != null && m_serverTypeCategory.equals(serverTypeCategory) )
			return;
		if (m_serverTypeCategory != null)
			m_categoriesToDel.add(new String(m_serverTypeCategory));
		if (serverTypeCategory != null)
			m_categoriesToAdd.add(serverTypeCategory);

		m_serverTypeCategory = serverTypeCategory;
	}

	public void setServerSpecCategory(String serverSpecCategory) {
		if (m_serverSpecCategory != null && m_serverSpecCategory.equals(serverSpecCategory) )
			return;
		if (m_serverSpecCategory != null)
			m_categoriesToDel.add(new String(m_serverSpecCategory));
		if (serverSpecCategory != null)
			m_categoriesToAdd.add(serverSpecCategory);
		
		m_serverSpecCategory = serverSpecCategory;
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
		if (m_notifCategory.equals(notifCategory) )
			return;
		if (m_notifCategory != null)
			m_categoriesToDel.add(new String(m_notifCategory));
		if (notifCategory != null)
			m_categoriesToAdd.add(notifCategory);
		
		m_notifCategory = notifCategory;
	}

	public void setOptionalByCategory(String optionalByCategory) {
		if (m_optionalByCategory.equals(optionalByCategory) )
			return;
		if (m_optionalByCategory != null)
			m_categoriesToDel.add(new String(m_optionalByCategory));
		if (optionalByCategory != null)
			m_categoriesToAdd.add(optionalByCategory);
		m_optionalByCategory = optionalByCategory;
	}

	public void setDescription(String description) {
		if (description != null && description.equals(m_description) )
			return;
		m_updatemap.add(DashBoardUtils.DESCRIPTION);
		m_description = description;
	}

	public void setBuilding(String building) {
		if (building != null && building.equals(m_building) )
			return;
		m_updatemap.add(DashBoardUtils.BUILDING);
		m_building = building;
	}

	public void setLeaseExpires(String leaseExpires) {
		if (leaseExpires != null && leaseExpires.equals(m_leaseExpires) )
			return;
		m_updatemap.add(DashBoardUtils.LEASEEXPIRES);
		m_leaseExpires = leaseExpires;
	}

	public void setLease(String lease) {
		if (lease != null && lease.equals(m_lease) )
			return;
		m_updatemap.add(DashBoardUtils.LEASE);
		m_lease = lease;
	}

	public void setVendorPhone(String vendorPhone) {
		if (vendorPhone != null && vendorPhone.equals(m_vendorPhone) )
			return;
		m_updatemap.add(DashBoardUtils.VENDORPHONE);
		m_vendorPhone = vendorPhone;
	}

	public void setVendor(String vendor) {
		if (vendor != null && vendor.equals(m_vendor) )
			return;
		m_updatemap.add(DashBoardUtils.VENDOR);
		m_vendor = vendor;
	}

	public void setSlot(String slot) {
		if (slot != null && slot.equals(m_slot) )
			return;
		m_updatemap.add(DashBoardUtils.SLOT);
		m_slot = slot;
	}

	public void setRack(String rack) {
		if (rack != null && rack.equals(m_rack) )
			return;
		m_updatemap.add(DashBoardUtils.RACK);
		m_rack = rack;
	}

	public void setRoom(String room) {
		if (room != null && room.equals(m_room) )
			return;
		m_updatemap.add(DashBoardUtils.BUILDING);
		m_room = room;
	}

	public void setOperatingSystem(String operatingSystem) {
		if (operatingSystem != null && operatingSystem.equals(m_operatingSystem) )
			return;
		m_updatemap.add(DashBoardUtils.OPERATINGSYSTEM);
		m_operatingSystem = operatingSystem;
	}

	public void setDateInstalled(String dateInstalled) {
		if (dateInstalled != null && dateInstalled.equals(m_dateInstalled) )
			return;
		m_updatemap.add(DashBoardUtils.DATEINSTALLED);
		m_dateInstalled = dateInstalled;
	}

	public void setAssetNumber(String assetNumber) {
		if (assetNumber != null && assetNumber.equals(m_assetNumber) )
			return;
		m_updatemap.add(DashBoardUtils.ASSETNUMBER);
		m_assetNumber = assetNumber;
	}

	public void setSerialNumber(String serialNumber) {
		if (serialNumber != null && serialNumber.equals(m_serialNumber) )
			return;
		m_updatemap.add(DashBoardUtils.SERIALNUMBER);
		m_serialNumber = serialNumber;
	}

	public void setCategory(String category) {
		if (category != null && category.equals(m_category) )
			return;
		m_updatemap.add(DashBoardUtils.CATEGORY);
		m_category = category;
	}

	public void setModelNumber(String modelNumber) {
		if (modelNumber != null && modelNumber.equals(m_modelNumber) )
			return;
		m_updatemap.add(DashBoardUtils.MODELNUMBER);
		m_modelNumber = modelNumber;
	}

	public void setManufacturer(String manufacturer) {
		if (manufacturer != null && manufacturer.equals(m_manufacturer) )
			return;
		m_updatemap.add(DashBoardUtils.MANUFACTURER);
		m_manufacturer = manufacturer;
	}
		
}
