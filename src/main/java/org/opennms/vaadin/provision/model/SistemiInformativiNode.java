package org.opennms.vaadin.provision.model;

import java.util.Map;
import java.util.Set;

import org.opennms.vaadin.provision.core.DashBoardUtils;

public class SistemiInformativiNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private String[] m_serverLevelCategory;
	private String m_managedByCategory;
	private String m_notifCategory;
	private Set<String> m_optionalCategory;
	private String m_prodCategory;
	private String m_TrentinoNetworkCategory;
	
	private String m_description;
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
	
	public SistemiInformativiNode(
			Map<String, Set<String>> serviceMap, String descr, String hostname,
			String parent, String snmpProfile,
			String vrf, String primary, String[] serverLevelCategory,
			String managedByCategory, String notifCategory,
			Set<String> optionalCategory, String prodCategory, String tnCategory,
			String city, String address1, String description, String building,
			String leaseExpires, String lease, String vendorPhone,
			String vendor, String slot, String rack, String room,
			String operatingSystem, String dateInstalled, String assetNumber,
			String serialNumber, String category, String modelNumber,
			String manufacturer, String foreignId) {
		super(serviceMap,descr,hostname,vrf,primary,parent,snmpProfile,city,address1,foreignId,building);
		m_serverLevelCategory = serverLevelCategory;
		m_managedByCategory = managedByCategory;
		m_notifCategory = notifCategory;
		m_optionalCategory = optionalCategory;
		m_prodCategory = prodCategory;
		m_TrentinoNetworkCategory = tnCategory;
		m_description = description;
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
	}

	public SistemiInformativiNode(String label) {
		super(label);
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

	public Set<String> getOptionalCategory() {
		return m_optionalCategory;
	}

	public String getDescription() {
		return m_description;
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
			setOnmsSyncOperations(OnmsSync.TRUE);
			return;
		}
		
		if (m_serverLevelCategory == null && serverLevelCategory != null) {
			if (serverLevelCategory[0] != null)
				m_categoriesToAdd.add(new String(serverLevelCategory[0]));
			if (serverLevelCategory[1] != null)
				m_categoriesToAdd.add(new String(serverLevelCategory[1]));
			m_serverLevelCategory = serverLevelCategory;
			setOnmsSyncOperations(OnmsSync.TRUE);
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
		setOnmsSyncOperations(OnmsSync.TRUE);
		m_serverLevelCategory = serverLevelCategory;
	}

	public void setTrentinoNetworkCategory(String tnCategory) {
		if (m_TrentinoNetworkCategory != null && m_TrentinoNetworkCategory.equals(tnCategory) )
			return;
		if (m_TrentinoNetworkCategory != null)
			m_categoriesToDel.add(m_TrentinoNetworkCategory);
		m_categoriesToAdd.add(tnCategory);
		setOnmsSyncOperations(OnmsSync.TRUE);
		m_TrentinoNetworkCategory = tnCategory;
	}

	public void setProdCategory(String prodCategory) {
		if (m_prodCategory != null && m_prodCategory.equals(prodCategory) )
			return;
		if (m_prodCategory != null)
			m_categoriesToDel.add(new String(m_prodCategory));
		if (prodCategory != null)
			m_categoriesToAdd.add(prodCategory);
		setOnmsSyncOperations(OnmsSync.TRUE);		
		m_prodCategory = prodCategory;
	}


	public void setManagedByCategory(String managedByCategory) {
		if (m_managedByCategory != null && m_managedByCategory.equals(managedByCategory) )
			return;
		if (m_managedByCategory != null)
			m_categoriesToDel.add(new String(m_managedByCategory));
		if (managedByCategory != null)
			m_categoriesToAdd.add(managedByCategory);
		
		setOnmsSyncOperations(OnmsSync.TRUE);
		m_managedByCategory = managedByCategory;
	}

	public void setNotifCategory(String notifCategory) {
		if (m_notifCategory != null && m_notifCategory.equals(notifCategory) )
			return;
		if (m_notifCategory == null && notifCategory == null )
			return;
		if (m_notifCategory != null)
			m_categoriesToDel.add(new String(m_notifCategory));
		if (notifCategory != null)
			m_categoriesToAdd.add(notifCategory);
		
		setOnmsSyncOperations(OnmsSync.TRUE);
		m_notifCategory = notifCategory;
	}

	public void setOptionalCategory(Set<String> optionalCategory) {
		
		if (m_optionalCategory != null && m_optionalCategory.equals(optionalCategory) )
			return;
		if (m_optionalCategory != null)
			m_categoriesToDel.addAll(m_optionalCategory);
		if (optionalCategory != null)
			m_categoriesToAdd.addAll(optionalCategory);
		setOnmsSyncOperations(OnmsSync.TRUE);
		m_optionalCategory = optionalCategory;
	}

	public void setDescription(String description) {
		if (description == null)
			description="";
		if (description.equals(m_description) )
			return;
		m_updatemap.add(DashBoardUtils.DESCRIPTION);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_description = description;
	}

	public void setLeaseExpires(String leaseExpires) {
		if (leaseExpires == null)
			leaseExpires="";
		if (leaseExpires.equals(m_leaseExpires) )
			return;
		m_updatemap.add(DashBoardUtils.LEASEEXPIRES);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_leaseExpires = leaseExpires;
	}

	public void setLease(String lease) {
		if (lease == null)
			lease="";
		if (lease.equals(m_lease) )
			return;
		m_updatemap.add(DashBoardUtils.LEASE);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_lease = lease;
	}

	public void setVendorPhone(String vendorPhone) {
		if (vendorPhone == null)
			vendorPhone="";
		if (vendorPhone.equals(m_vendorPhone) )
			return;
		m_updatemap.add(DashBoardUtils.VENDORPHONE);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_vendorPhone = vendorPhone;
	}

	public void setVendor(String vendor) {
		if (vendor == null)
			vendor="";
		if (vendor.equals(m_vendor) )
			return;
		m_updatemap.add(DashBoardUtils.VENDOR);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_vendor = vendor;
	}

	public void setSlot(String slot) {
		if (slot == null)
			slot="";
		if (slot.equals(m_slot) )
			return;
		m_updatemap.add(DashBoardUtils.SLOT);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_slot = slot;
	}

	public void setRack(String rack) {
		if (rack == null)
			rack="";
		if (rack.equals(m_rack) )
			return;
		m_updatemap.add(DashBoardUtils.RACK);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_rack = rack;
	}

	public void setRoom(String room) {
		if (room == null)
			room="";
		if (room.equals(m_room) )
			return;
		m_updatemap.add(DashBoardUtils.ROOM);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_room = room;
	}

	public void setOperatingSystem(String operatingSystem) {
		if (operatingSystem == null)
			operatingSystem="";
		if (operatingSystem.equals(m_operatingSystem) )
			return;
		m_updatemap.add(DashBoardUtils.OPERATINGSYSTEM);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_operatingSystem = operatingSystem;
	}

	public void setDateInstalled(String dateInstalled) {
		if (dateInstalled == null)
			dateInstalled="";
		if (dateInstalled.equals(m_dateInstalled) )
			return;
		m_updatemap.add(DashBoardUtils.DATEINSTALLED);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_dateInstalled = dateInstalled;
	}

	public void setAssetNumber(String assetNumber) {
		if (assetNumber == null)
			assetNumber="";
		if (assetNumber.equals(m_assetNumber) )
			return;
		m_updatemap.add(DashBoardUtils.ASSETNUMBER);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_assetNumber = assetNumber;
	}

	public void setSerialNumber(String serialNumber) {
		if (serialNumber == null)
			serialNumber="";
		if (serialNumber.equals(m_serialNumber) )
			return;
		m_updatemap.add(DashBoardUtils.SERIALNUMBER);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_serialNumber = serialNumber;
	}

	public void setCategory(String category) {
		if (category == null)
			category="Server";
		if (category.equals(m_category) )
			return;
		m_updatemap.add(DashBoardUtils.CATEGORY);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_category = category;
	}

	public void setModelNumber(String modelNumber) {
		if (modelNumber == null)
			modelNumber="";
		if (modelNumber.equals(m_modelNumber) )
			return;
		m_updatemap.add(DashBoardUtils.MODELNUMBER);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_modelNumber = modelNumber;
	}

	public void setManufacturer(String manufacturer) {
		if (manufacturer == null)
			manufacturer="";
		if (manufacturer.equals(m_manufacturer) )
			return;
		m_updatemap.add(DashBoardUtils.MANUFACTURER);
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_manufacturer = manufacturer;
	}
}
