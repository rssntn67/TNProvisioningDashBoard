package org.opennms.vaadin.provision.model;

import java.util.HashMap;
import java.util.Set;

import org.opennms.vaadin.provision.core.DashBoardUtils;

public class MediaGatewayNode extends BasicNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
		
	private String m_backupProfile;		
	private String m_networkCategory;
	
	
	public MediaGatewayNode(String label, String nc) {
		super(label);
		m_networkCategory = nc; 						
	}
	
	public MediaGatewayNode(
			String descr, 
			String hostname,
			String vrf, 
			String primary, 
			String parent,
			String parentId,
			String networkCategory, 
			String snmpProfile, 
			String backupProfile,
			String city, 
			String address1, 
			String building, 
			String foreignId) {
		super(new HashMap<String, Set<String>>(),descr,hostname,vrf,primary,parent,parentId,snmpProfile,city,address1,foreignId,building);
		m_networkCategory = networkCategory;
		m_backupProfile = backupProfile;
	}

	public String getNetworkCategory() {
		return m_networkCategory;
	}

	public void setNetworkCategory(String networkCategory) {
		if (networkCategory.equals(m_networkCategory))
			return;
		setOnmsSyncOperations(OnmsSync.TRUE);
		m_categoriesToDel.add(new String(m_networkCategory));
		m_categoriesToAdd.add(networkCategory);
		m_networkCategory = networkCategory;
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
}
