package org.opennms.vaadin.provision.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.vaadin.provision.core.DashBoardUtils;

public class TrentinoNetworkNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private Set<String> m_updatemap = new HashSet<String>();

	private List<String> m_categoriesToAdd = new ArrayList<String>();
	private List<String> m_categoriesToDel = new ArrayList<String>();
    
	private List<String> m_interfToAdd = new ArrayList<String>();
	private List<String> m_interfToDel = new ArrayList<String>();

	private String m_label;
	private String m_descr;
	private String m_hostname;
	private String m_vrf;
	private String m_primary;

	private String[] m_secondary;
	private String m_parent;

	private Categoria m_networkCategory;
	private String m_notifCategory;
	private String m_threshCategory;
	private String m_slaCategory;

	private String m_snmpProfile;
	private String m_backupProfile;
	
	private String m_city;
	private String m_address1;
	private String m_building;
	private String m_circuitId;
	
	private String m_foreignId;
	
	protected boolean m_valid = true;
	
	public TrentinoNetworkNode(String label, Categoria vrf) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";

		m_networkCategory = vrf; 
		m_vrf = vrf.getDnsdomain();
		m_notifCategory = vrf.getNetworklevel();
		m_threshCategory = vrf.getThresholdlevel();
		m_backupProfile = vrf.getBackupprofile();
		m_snmpProfile = vrf.getSnmpprofile();
						
		m_descr="Provided by Provision Dashboard";
		
		m_valid=false;
	}
	
	public TrentinoNetworkNode(
			String descr, 
			String hostname,
			String vrf, 
			String primary, 
			String parent,
			Categoria networkCategory, 
			String notifCategory,
			String threshCategory, 
			String snmpProfile, 
			String backupProfile,
			String city, 
			String address1, 
			String foreignId, 
			boolean valid, 
			String[] secondary,
			String slaCategory,
			String building,
			String circuitId) {
		super();
		m_label = null;
		m_descr = descr;
		m_hostname = hostname;
		m_vrf = vrf;
		m_primary = primary;
		m_parent = parent;
		m_networkCategory = networkCategory;
		m_notifCategory = notifCategory;
		m_threshCategory = threshCategory;
		m_snmpProfile = snmpProfile;
		m_backupProfile = backupProfile;
		m_city = city;
		m_address1 = address1;
		m_foreignId = foreignId;
		m_valid = valid;
		m_secondary = secondary;
		m_slaCategory = slaCategory;
		m_building = building;
		m_circuitId = circuitId;

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

	public String getParent() {
		return m_parent;
	}

	public void setParent(String parentNodeLabel) {
		if (m_parent != null && m_parent.equals(parentNodeLabel))
			return;
		if (m_parent == null && parentNodeLabel == null) 
			return;		
		m_parent = parentNodeLabel;
		m_updatemap.add(DashBoardUtils.PARENT);
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

	public Categoria getNetworkCategory() {
		return m_networkCategory;
	}

	public void setNetworkCategory(Categoria networkCategory) {
		if (networkCategory.getName().equals(m_networkCategory.getName()))
			return;
		m_categoriesToDel.add(m_networkCategory.getName());
		m_categoriesToAdd.add(networkCategory.getName());
		
		if (!networkCategory.getNetworklevel().equals(m_networkCategory.getNetworklevel())) {
			m_categoriesToDel.add(m_networkCategory.getNetworklevel());
			m_categoriesToAdd.add(networkCategory.getNetworklevel());
		}
		m_networkCategory = networkCategory;
	}
	
	public String getNotifCategory() {
		return m_notifCategory;
	}
	
	public void setNotifCategory(String notifCategory) {
		if (notifCategory != null && notifCategory.equals(m_notifCategory))
			return;
		if (m_notifCategory != null) {
			m_categoriesToDel.add(m_notifCategory);
		}
		if (notifCategory != null) { 
			m_categoriesToAdd.add(notifCategory);
		}
		m_notifCategory = notifCategory;
	}
	
	public String getThreshCategory() {
		return m_threshCategory;
	}

	public void setThreshCategory(String threshCategory) {
		if (threshCategory != null && threshCategory.equals(m_threshCategory))
			return;
		if (m_threshCategory != null) {
			m_categoriesToDel.add(m_threshCategory);
		}
		if (threshCategory != null) {
			m_categoriesToAdd.add(threshCategory);
		}		
		m_threshCategory = threshCategory;
	}
	
	public String getVrf() {
		return m_vrf;
	}

	public void setVrf(String vrf) {
		if (m_vrf != null && m_vrf.equals(vrf))
			return;
		m_vrf = vrf;
		m_updatemap.add(DashBoardUtils.CAT);
	}

    public String getDescr() {
		return m_descr;
	}

	public void setDescr(String descr) {
		m_descr = descr;
	}
	public String[] getSecondary() {
		return m_secondary;
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
	
	public String getBackupProfile() {
		return m_backupProfile;
	}

	public void setBackupProfile(String backupProfile) {
		if (m_backupProfile != null && m_backupProfile.equals(backupProfile))
			return;
		m_updatemap.add(DashBoardUtils.BACKUP_PROFILE);
		m_backupProfile = backupProfile;
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
		m_updatemap.add(DashBoardUtils.PRIMARY);
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
	
	public void setSecondary(String[] secondary) {
		m_secondary=secondary;
	}

	public String getBuilding() {
		return m_building;
	}

	public void setBuilding(String building) {
		if (m_building !=  null && m_building.equals(building))
			return;
		if (m_building ==  null && building == null)
			return;
		if (building != null ) {
			m_updatemap.add(DashBoardUtils.BUILDING);
		}
		m_building = building;
	}

	public String getCircuitId() {
		return m_circuitId;
	}

	public void setCircuitId(String circuitId) {
		if (m_circuitId !=  null && m_circuitId.equals(circuitId))
			return;
		if (m_circuitId ==  null && circuitId == null)
			return;
		if (circuitId != null ) {
			m_updatemap.add(DashBoardUtils.CIRCUITID);
		}
		m_circuitId = circuitId;
	}

	public String getSlaCategory() {
		return m_slaCategory;
	}

	public void setSlaCategory(String slaCategory) {
		if (slaCategory != null && slaCategory.equals(m_slaCategory))
			return;
		if (m_slaCategory != null) {
			m_categoriesToDel.add(m_slaCategory);
		}
		if (slaCategory != null) { 
			m_categoriesToAdd.add(slaCategory);
		}
		m_slaCategory = slaCategory;
	}
}
