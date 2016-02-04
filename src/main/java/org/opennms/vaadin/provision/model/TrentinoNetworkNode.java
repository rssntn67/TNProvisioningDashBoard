package org.opennms.vaadin.provision.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.TNDao;
import org.opennms.vaadin.provision.dashboard.ProvisionDashboardValidationException;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

public class TrentinoNetworkNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private MultivaluedMap<String, String> m_updatemap = new MultivaluedMapImpl();
	private List<RequisitionAsset> m_assetsToPut = new ArrayList<RequisitionAsset>();

	private List<String> m_categoriesToAdd = new ArrayList<String>();
	private List<String> m_categoriesToDel = new ArrayList<String>();
    
	private List<String> m_interfToAdd = new ArrayList<String>();
	private List<String> m_interfToDel = new ArrayList<String>();

	private String m_descr;
	private String m_hostname;
	private String m_vrf;
	private String m_primary;
	private IndexedContainer m_secondary = new IndexedContainer();

	private String m_parent;
	private String m_parentId;

	private String[] m_networkCategory;
	private String m_notifCategory;
	private String m_threshCategory;

	private String m_snmpProfile;
	private String m_backupProfile;
	private boolean m_snmpProfileUpdated=false;
	private boolean m_backupProfileUpdated=false;
	
	private String m_city;
	private String m_address1;
	
	private String m_foreignId;
	
	protected boolean m_valid = true;
	
	public TrentinoNetworkNode(String label) {
		m_hostname="";
		
		m_primary="0.0.0.0";
		
		m_city="";
		m_address1="";

		m_networkCategory = TNDao.m_network_categories[0]; 
		m_vrf = TNDao.m_network_categories[0][2];
		m_notifCategory = TNDao.m_network_categories[0][3];
		m_threshCategory = TNDao.m_network_categories[0][4];
		m_backupProfile = TNDao.m_network_categories[0][5];
		m_snmpProfile = TNDao.m_network_categories[0][6];
						
		m_descr="Imported from Provision Dashboard";
		m_secondary.addContainerProperty("indirizzo ip", String.class, null);
		
		m_valid=false;
	}

	
	@SuppressWarnings("unchecked")
	public TrentinoNetworkNode(String descr, String hostname,
			String vrf, String primary, String parent, String parentId,
			String[] networkCategory, String notifCategory,
			String threshCategory, String snmpProfile, String backupProfile,
			String city, String address1, String foreignId, boolean valid, String[] secondary) {
		super();
		m_descr = descr;
		m_hostname = hostname;
		m_vrf = vrf;
		m_primary = primary;
		m_parent = parent;
		m_parentId = parentId;
		m_networkCategory = networkCategory;
		m_notifCategory = notifCategory;
		m_threshCategory = threshCategory;
		m_snmpProfile = snmpProfile;
		m_backupProfile = backupProfile;
		m_city = city;
		m_address1 = address1;
		m_foreignId = foreignId;
		m_valid = valid;
		m_secondary.addContainerProperty("indirizzo ip", String.class, null);
		for (String ip: secondary) {
			Item ipItem = m_secondary.getItem(m_secondary.addItem());
			ipItem.getItemProperty("indirizzo ip").setValue(ip); 
		}

	}


	public String getForeignId() {
		return m_foreignId;
	}

	public String getNodeLabel() {
		return m_hostname.toLowerCase() + "." + m_vrf;		
	}

	public List<RequisitionAsset> getAssetsToPut() {
		return m_assetsToPut;
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
	}
	
	public String getParentId() {
		return m_parentId;
	}

	public void setParentId(String parentId) {
		if (m_parentId != null && m_parent.equals(parentId))
			return;
		if (m_parentId == null && parentId == null) 
			return;
		m_updatemap.add("parent-foreign-id", parentId);
		m_parentId = parentId;
	}

	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		if (m_city != null && m_city.equals(city))
			return;
		RequisitionAsset assetdescription = new RequisitionAsset(OnmsDao.DESCRIPTION, city + " - " + m_address1);
		if (city != null) {
			m_updatemap.add(OnmsDao.CITY, city);
			m_assetsToPut.add(assetdescription);
		}
		m_city = city;
		
	}

	public String[] getNetworkCategory() {
		return m_networkCategory;
	}

	public void setNetworkCategory(String[] networkCategory) {
		if (networkCategory != null && Arrays.equals(m_networkCategory,networkCategory))
			return;
		if (m_networkCategory != null) {
			m_categoriesToDel.add(m_networkCategory[0]);
			m_categoriesToDel.add(m_networkCategory[1]);
		}
		
		if (networkCategory != null) {
			m_categoriesToAdd.add(networkCategory[0]);
			m_categoriesToAdd.add(networkCategory[1]);
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

	public void setVrf(String vrf) throws ProvisionDashboardValidationException {
		if (m_vrf != null && m_vrf.equals(vrf))
			return;
		String nodelabel=m_hostname.toLowerCase() + "." + vrf;
		m_updatemap.add("node-label", nodelabel);
		m_vrf = vrf;
	}

    public String getDescr() {
		return m_descr;
	}

	public void setDescr(String descr) {
		m_descr = descr;
	}
	public IndexedContainer getSecondary() {
		return m_secondary;
	}
	
	public String getSnmpProfile() {
		return m_snmpProfile;
	}

	public void setSnmpProfile(String snmpProfile) {
		if (snmpProfile == null)
			m_valid = false;
		if (m_snmpProfile != null && m_snmpProfile.equals(snmpProfile))
			return;
		m_snmpProfileUpdated = true;
		m_snmpProfile = snmpProfile;
	}
	
	public String getBackupProfile() {
		return m_backupProfile;
	}

	public void setBackupProfile(String backupProfile) {
		if (backupProfile == null)
			m_valid=false;
		if (m_backupProfile != null && m_backupProfile.equals(backupProfile))
			return;
		m_backupProfileUpdated=true;
		m_backupProfile = backupProfile;
	}

	public String getAddress1() {
		return m_address1;
	}
	public void setAddress1(String address) {
		if (m_address1 !=  null && m_address1.equals(address))
			return;
		if (address != null ) {
			m_assetsToPut.add(new RequisitionAsset(OnmsDao.ADDRESS, address));
			m_assetsToPut.add(new RequisitionAsset(OnmsDao.DESCRIPTION, m_city + " - " + address));
		}
		m_address1 = address;
	}
	
	public String getHostname() {
		return m_hostname;
	}
	
	public void setHostname(String hostname) throws ProvisionDashboardValidationException {
		if (m_hostname != null && m_hostname.equals(hostname))
			return;
		m_hostname = hostname;
		String nodelabel=hostname.toLowerCase() + "." + m_vrf;
		m_updatemap.add("node-label", nodelabel);
	}

	public String getPrimary() {
		return m_primary;
	}
	
	public void setPrimary(String primary) {
		if (m_primary != null && m_primary.equals(primary))
			return;
		if (m_primary != null) {
			m_interfToDel.add(m_primary);
		}

		if (primary != null) {
			m_interfToAdd.add(primary);
		}
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
		m_assetsToPut.clear();
		m_categoriesToDel.clear();
		m_categoriesToAdd.clear();
	}

	public boolean isSnmpProfileUpdated() {
		return m_snmpProfileUpdated;
	}

	public boolean isBackupProfileUpdated() {
		return m_backupProfileUpdated;
	}

	public MultivaluedMap<String, String> getUpdatemap() {
		return m_updatemap;
	}


	public void setForeignId(String foreignId) {
		m_foreignId = foreignId;
	}
}
