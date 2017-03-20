package org.opennms.vaadin.provision.model;

import java.util.Map;
import java.util.Set;

import org.opennms.vaadin.provision.core.DashBoardUtils;

public class TrentinoNetworkNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private Categoria m_networkCategory;
	private String m_notifCategory;
	private String m_threshCategory;
	private String m_slaCategory;
	private Set<String> m_optionalCategory;

	private String m_backupProfile;
	
	private String m_circuitId;
		
	public TrentinoNetworkNode(String label, Categoria vrf,String foreignSource) {
		super(label,vrf.getDnsdomain(),vrf.getSnmpprofile(),
				foreignSource);

		m_networkCategory = vrf; 
		m_notifCategory = vrf.getNotifylevel();
		m_threshCategory = vrf.getThresholdlevel();
		m_backupProfile = vrf.getBackupprofile();
	}
	
	public TrentinoNetworkNode(
			Map<String,Set<String>> serviceMap,
			String descr, 
			String hostname,
			String vrf, 
			String primary, 
			String parent,
			String parentId,
			Categoria networkCategory, 
			String notifCategory,
			String threshCategory, 
			String snmpProfile, 
			String backupProfile,
			String city, 
			String address1, 
			String foreignId, 
			String slaCategory,
			String building,
			String circuitId,
			Set<String> optionCategory,
			String foreignSource) {
		super(serviceMap,descr,hostname,vrf,primary,parent,parentId,snmpProfile,city,address1,
				foreignId,building,foreignSource);
		m_networkCategory = networkCategory;
		m_notifCategory = notifCategory;
		m_threshCategory = threshCategory;
		m_slaCategory = slaCategory;
		m_optionalCategory = optionCategory;
		m_backupProfile = backupProfile;
		m_circuitId = circuitId;
	}

	public Set<String> getOptionalCategory() {
		return m_optionalCategory;
	}

	public Categoria getNetworkCategory() {
		return m_networkCategory;
	}

	public void setNetworkCategory(Categoria networkCategory) {
		if (networkCategory == null && m_networkCategory == null)
			return;
			
		if (networkCategory == null && m_networkCategory != null){
			m_categoriesToDel.add(m_networkCategory.getName());
			m_categoriesToDel.add(m_networkCategory.getNetworklevel());
			setOnmsSyncOperations(OnmsSync.TRUE);
			m_networkCategory = networkCategory;
			return;
		}
		
		if (networkCategory != null && m_networkCategory == null){
			m_categoriesToAdd.add(networkCategory.getName());
			m_categoriesToAdd.add(networkCategory.getNetworklevel());
			setOnmsSyncOperations(OnmsSync.TRUE);
			m_networkCategory = networkCategory;
			return;
		}

		if (networkCategory.getName().equals(m_networkCategory.getName()))
			return;
		

		setOnmsSyncOperations(OnmsSync.TRUE);
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
		setOnmsSyncOperations(OnmsSync.TRUE);
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
		setOnmsSyncOperations(OnmsSync.TRUE);
		if (m_threshCategory != null) {
			m_categoriesToDel.add(m_threshCategory);
		}
		if (threshCategory != null) {
			m_categoriesToAdd.add(threshCategory);
		}		
		m_threshCategory = threshCategory;
	}
			
	public String getBackupProfile() {
		return m_backupProfile;
	}

	public void setBackupProfile(String backupProfile) {
		if (m_backupProfile != null && m_backupProfile.equals(backupProfile))
			return;
		setOnmsSyncOperations(OnmsSync.DBONLY);
		m_updatemap.add(DashBoardUtils.BACKUP_PROFILE);
		m_backupProfile = backupProfile;
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
			setOnmsSyncOperations(OnmsSync.DBONLY);
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
		if (slaCategory == null && m_slaCategory == null)
			return;
		setOnmsSyncOperations(OnmsSync.TRUE);
		if (m_slaCategory != null) {
			m_categoriesToDel.add(m_slaCategory);
		}
		if (slaCategory != null) { 
			m_categoriesToAdd.add(slaCategory);
		}
		m_slaCategory = slaCategory;
	}
	
	public void setOptionalCategory(Set<String> optionalCategory) {
		if (m_optionalCategory != null && m_optionalCategory.equals(optionalCategory) )
			return;
		if (m_optionalCategory == null && optionalCategory == null )
			return;
		setOnmsSyncOperations(OnmsSync.TRUE);
		if (m_optionalCategory != null)
			m_categoriesToDel.addAll(m_optionalCategory);
		if (optionalCategory != null)
			m_categoriesToAdd.addAll(optionalCategory);
		m_optionalCategory = optionalCategory;
	}	
}
