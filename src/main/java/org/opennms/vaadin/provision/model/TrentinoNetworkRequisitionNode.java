package org.opennms.vaadin.provision.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.TNDao;
import org.opennms.vaadin.provision.dashboard.ProvisionDashboardValidationException;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

import static org.opennms.vaadin.provision.core.DashBoardUtils.hasInvalidDnsBind9Label;
import static org.opennms.vaadin.provision.core.DashBoardUtils.hasInvalidDnsBind9LabelSize;
import static org.opennms.vaadin.provision.core.DashBoardUtils.hasInvalidDnsBind9Size;
import static org.opennms.vaadin.provision.core.DashBoardUtils.hasUnSupportedDnsDomain;


public class TrentinoNetworkRequisitionNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	MultivaluedMap<String, String> m_updatemap = new MultivaluedMapImpl();
    List<RequisitionAsset> m_assetsToPut = new ArrayList<RequisitionAsset>();

    List<RequisitionCategory> m_categoriesToAdd = new ArrayList<RequisitionCategory>();
    List<RequisitionCategory> m_categoriesToDel = new ArrayList<RequisitionCategory>();
    
    List<RequisitionInterface> m_interfToAdd = new ArrayList<RequisitionInterface>();
    List<RequisitionInterface> m_interfToDel = new ArrayList<RequisitionInterface>();

    protected String m_descr;
	protected String m_hostname;
	protected String m_vrf;
	protected String m_primary;
	protected IndexedContainer m_secondary = new IndexedContainer();

	protected String m_parent;
	protected String m_parentId;

	protected String[] m_networkCategory;
	protected String m_notifCategory;
	protected String m_threshCategory;

	protected String m_snmpProfile;
	protected String m_backupProfile;

	protected String m_city;
	protected String m_address1;
	
	protected String m_foreignId;
	
	protected boolean m_valid = true;
	
	public TrentinoNetworkRequisitionNode(String label) {
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
	public TrentinoNetworkRequisitionNode(RequisitionNode requisitionNode, String parent) {
		m_foreignId = requisitionNode.getForeignId();
		if (m_foreignId == null)
			m_valid = false;
		String nodelabel = requisitionNode.getNodeLabel();
		if (nodelabel == null)
			m_valid=false;

		m_parentId = requisitionNode.getParentForeignId();
		m_parent=parent;
		
		for (String vrf: TNDao.m_vrfs) {
			if (requisitionNode.getNodeLabel().endsWith("."+vrf)) {
				m_vrf = vrf;
				m_hostname = requisitionNode.getNodeLabel().substring(0,requisitionNode.getNodeLabel().indexOf(vrf)-1);
				break;
			}
		}

		if (m_vrf == null || m_hostname == null)
			m_valid = false;
		
		m_secondary.addContainerProperty("indirizzo ip", String.class, null);
		for (RequisitionInterface ip: requisitionNode.getInterfaces()) {
			if (ip.getSnmpPrimary() == null)
				m_valid=false;
			if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
				m_primary = ip.getIpAddr();
				m_descr = ip.getDescr();
			} else {
				Item ipItem = m_secondary.getItem(m_secondary.addItem());
				ipItem.getItemProperty("indirizzo ip").setValue(ip.getIpAddr()); 
			}
		}
		
		if (m_primary == null)
			m_valid = false;
		
		for (String[] networkCategory: TNDao.m_network_categories) {
			if (requisitionNode.getCategory(networkCategory[0]) != null && requisitionNode.getCategory(networkCategory[1]) != null) {
				m_networkCategory = networkCategory;
				break;
			}
		}
		if (m_networkCategory == null)
			m_valid = false;

		for (String notifCategory: TNDao.m_notif_categories) {
			if (requisitionNode.getCategory(notifCategory) != null ) {
				m_notifCategory = notifCategory;
				break;
			}
		}
		if (m_notifCategory == null)
			m_valid = false;
		
		for (String threshCategory: TNDao.m_thresh_categories) {
			if (requisitionNode.getCategory(threshCategory) != null ) {
				m_threshCategory = threshCategory;
				break;
			}
		}
		if (m_threshCategory == null)
			m_valid = false;
		
		if (requisitionNode.getCity() != null)
			m_city = requisitionNode.getCity();
		else
			m_valid = false;
		
		if (requisitionNode.getAsset(OnmsDao.ADDRESS) != null)
			m_address1 = requisitionNode.getAsset(OnmsDao.ADDRESS).getValue();
		else
			m_valid = false;
		
		if (hasInvalidDnsBind9Size(nodelabel))
			m_valid = false;
		if (hasInvalidDnsBind9LabelSize(nodelabel))
			m_valid = false;
		if (hasInvalidDnsBind9Label(nodelabel))
			m_valid = false;
		if (m_hostname != null && hasUnSupportedDnsDomain(m_hostname,nodelabel,TNDao.m_sub_domains))
			m_valid = false;		
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

	public List<RequisitionCategory> getCategoriesToAdd() {
		return m_categoriesToAdd;
	}

	public List<RequisitionCategory> getCategoriesToDel() {
		return m_categoriesToDel;
	}

	public List<RequisitionInterface> getInterfToAdd() {
		return m_interfToAdd;
	}

	public List<RequisitionInterface> getInterfToDel() {
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
			m_categoriesToDel.add(new RequisitionCategory(m_networkCategory[0]));
			m_categoriesToDel.add(new RequisitionCategory(m_networkCategory[1]));
		}
		
		if (networkCategory != null) {
			m_categoriesToAdd.add(new RequisitionCategory(networkCategory[0]));
			m_categoriesToAdd.add(new RequisitionCategory(networkCategory[1]));
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
			m_categoriesToDel.add(new RequisitionCategory(m_notifCategory));
		}
		if (notifCategory != null) { 
			m_categoriesToAdd.add(new RequisitionCategory(notifCategory));
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
			m_categoriesToDel.add(new RequisitionCategory(m_threshCategory));
		}
		if (threshCategory != null) {
			m_categoriesToAdd.add(new RequisitionCategory(threshCategory));
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
			RequisitionInterface iface = new RequisitionInterface();
			iface.setIpAddr(m_primary);
			m_interfToDel.add(iface);
		}

		if (primary != null) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setSnmpPrimary(org.opennms.netmgt.model.PrimaryType.PRIMARY);
			iface.setIpAddr(primary);
			iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
			iface.setDescr("Provided by Provision Dashboard");
			setDescr(iface.getDescr());
			
			m_interfToAdd.add(iface);
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
}
