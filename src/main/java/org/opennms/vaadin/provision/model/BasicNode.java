package org.opennms.vaadin.provision.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;

public class BasicNode implements Serializable {
		
	private static final Logger logger = Logger.getLogger(BasicNode.class.getName());

	public enum OnmsState {
		NEW,
		DELETE,
		UPDATE,
		REPLACE,
		NONE;
	}
	
	public enum OnmsSync {
		DBONLY,
		FALSE,
		TRUE;		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private Set<OnmsSync> m_syncoperations = new HashSet<BasicNode.OnmsSync>();
	private OnmsState m_onmstate = OnmsState.NONE;

	// This contains all the ip->service in secondary table but does not contain primary->icmp and primary-snmp
	private Map<BasicInterface,Set<String>> m_serviceMap;  

	protected Set<String> m_updatemap = new HashSet<String>();

	protected List<String> m_categoriesToAdd = new ArrayList<String>();
	protected List<String> m_categoriesToDel = new ArrayList<String>();
    
	protected List<BasicInterface> m_interfToAdd = new ArrayList<BasicInterface>();
	protected List<BasicInterface> m_interfToDel = new ArrayList<BasicInterface>();

	private Map<BasicInterface,Set<String>> m_serviceToAdd = new HashMap<BasicInterface, Set<String>>();
	private Map<BasicInterface,Set<String>> m_serviceToDel = new HashMap<BasicInterface, Set<String>>();

	private String m_label;
	private String m_descr;
	private String m_hostname;
	private String m_vrf;
	private String m_primary;
	private String m_parent;
	private String m_parentId;
	private String m_snmpProfile;
	private String m_city;
	private String m_address1;
	private String m_building;
	private String m_foreignId;
	private String m_foreignSource;
	
	private boolean m_valid = true;
		
	public BasicNode(String label,
			String foreignSource) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";
						
		m_descr=DashBoardUtils.DESCR_TNPD;
		
		m_valid=false;
		m_serviceMap = new HashMap<BasicInterface, Set<String>>();
		m_foreignSource=foreignSource;
		m_onmstate = OnmsState.NEW;
	}

	public BasicNode(String label, 
			String vrf, 
			String snmpProfile,
			String foreignSource) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";

		m_vrf = vrf;
		m_snmpProfile = snmpProfile;
						
		m_descr=DashBoardUtils.DESCR_TNPD;
		
		m_valid=false;
		m_serviceMap = new HashMap<BasicInterface, Set<String>>();
		m_foreignSource=foreignSource;
		m_onmstate = OnmsState.NEW;
	}

	public BasicNode(
			Map<BasicInterface,Set<String>> serviceMap,
			String descr, 
			String hostname,
			String vrf, 
			String primary, 
			String parent,
			String parentId,
			String snmpProfile, 
			String city, 
			String address1, 
			String foreignId, 
			String building,
			String foreignSource) {

		m_label = null;
		m_serviceMap = serviceMap;
		m_descr = descr;
		m_hostname = hostname;
		m_vrf = vrf;
		m_primary = primary;
		m_parent = parent;
		m_parentId = parentId;
		m_snmpProfile = snmpProfile;
		m_city = city;
		m_address1 = address1;
		m_foreignId = foreignId;
		m_building = building;
	}

	public void clear() {
		m_updatemap.clear();
		m_interfToDel.clear();
		m_interfToAdd.clear();
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

	public List<BasicInterface> getInterfToAdd() {
		return m_interfToAdd;
	}

	public List<BasicInterface> getInterfToDel() {
		return m_interfToDel;
	}

	public Map<BasicInterface, Set<String>> getServiceToAdd() {
		return m_serviceToAdd;
	}

	public Map<BasicInterface, Set<String>> getServiceToDel() {
		return m_serviceToDel;
	}
	public String getParentId() {
		return m_parentId;
	}

	public void setParentId(String parentId) {
		m_parentId=parentId;
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
		setOnmsSyncOperations(OnmsSync.DBONLY);
	}
	
	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		if (m_city != null && m_city.equals(city))
			return;
		if (city != null) {
			m_updatemap.add(DashBoardUtils.CITY);
			setOnmsSyncOperations(OnmsSync.DBONLY);
		}
		m_city = city;		
	}

	public String getVrf() {
		return m_vrf;
	}

	public void setVrf(String vrf) {
		if (m_vrf != null && m_vrf.equals(vrf))
			return;
		m_vrf = vrf;
		m_updatemap.add(DashBoardUtils.CAT);
		setOnmsSyncOperations(OnmsSync.DBONLY);
	}

    public String getDescr() {
		return m_descr;
	}

	public void setDescr(String descr) {
		if (m_descr != null && m_descr.equals(descr))
			return;
		m_descr = descr;
		m_updatemap.add(DashBoardUtils.DESCR);
		setOnmsSyncOperations(OnmsSync.DBONLY);
	}
	
	public String getSnmpProfile() {
		return m_snmpProfile;
	}

	public void setSnmpProfileWithOutUpdating(String snmpProfile) {
		if (snmpProfile == null)
			m_valid = false;
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
			setOnmsSyncOperations(OnmsSync.DBONLY);
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
		setOnmsSyncOperations(OnmsSync.DBONLY);
	}

	public String getPrimary() {
		return m_primary;
	}

	public void setPrimary(String primary) {
		logger.info("new primary: " + primary + " old primary: " + m_primary);
		if (primary == null)
			return;
		if (m_primary != null && m_primary.equals(primary))
			return;
		
		logger.info("set primary update");
		String oldprimary = new String(m_primary);
		m_primary = primary;
		m_updatemap.add(DashBoardUtils.PRIMARY);
		if (oldprimary != null) {
			logger.info("delete old primary:" + oldprimary);
			BasicInterface opi = new BasicInterface();
			opi.setIp(oldprimary);
			opi.setDescr(m_descr);
			opi.setOnmsprimary(OnmsPrimary.P);
			BasicService bs = new BasicService(opi);
			bs.setService("ICMP");
			delService(bs);
			BasicService bs1 = new BasicService(opi);
			bs1.setService("ICMP");
			delService(bs1);
		}
		logger.info("add new primary:" + m_primary);
		BasicInterface primaryi=new BasicInterface();
		primaryi.setDescr(m_descr);
		primaryi.setIp(m_primary);
		primaryi.setOnmsprimary(OnmsPrimary.P);
		BasicService bs = new BasicService(primaryi);
		bs.setService("ICMP");
		addService(bs);
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
			setOnmsSyncOperations(OnmsSync.DBONLY);
		}
		m_building = building;
	}
	
	public void addService(BasicService bs) {
		BasicInterface ip = bs.getInterface();
		String service = bs.getService();
		if (!m_serviceMap.containsKey(ip)) {
			m_serviceMap.put(ip, new HashSet<String>());
		}
		if (m_serviceMap.get(ip).contains(service))
			return;
		m_serviceMap.get(ip).add(service);
		
		// clean delete
		boolean addinterface = true;
		boolean addservice = true;
		if (m_interfToDel.contains(ip)) {
			m_interfToDel.remove(ip);
			addinterface = false;
		} else	if (m_serviceToDel.containsKey(ip) && m_serviceToDel.get(ip).contains(service)) {
			m_serviceToDel.get(ip).remove(service);
			if (m_serviceToDel.get(ip).isEmpty())
				m_serviceToDel.remove(ip);
			addservice=false;
		}
	
		if (addinterface 
				&& m_serviceMap.get(ip).size() == 1)
			m_interfToAdd.add(ip);
		if (addservice) {
			if (!m_serviceToAdd.containsKey(ip))
				m_serviceToAdd.put(ip, new HashSet<String>());
			m_serviceToAdd.get(ip).add(service);
		}
		setOnmsSyncOperations(OnmsSync.DBONLY);
	}

	public void delService(BasicService bs) {
		if (bs == null)
			return;
		BasicInterface ip = bs.getInterface();
		String service = bs.getService();
		if (!m_serviceMap.containsKey(ip))
			return;
		if (ip.equals(m_primary) && "ICMP".equals(service))
			return;

		if (!m_serviceMap.get(ip).remove(service))
			return;
		
		if (m_serviceMap.get(ip).isEmpty())
			m_serviceMap.remove(ip);
		
		boolean addinterfacetodelete = true;
		boolean addservicetodelte = true;
		if (m_serviceToAdd.containsKey(ip)) {
			if (m_serviceToAdd.get(ip).remove(service))
				addservicetodelte=false;
			if (m_serviceToAdd.get(ip).isEmpty()) {
				m_serviceToAdd.remove(ip);
				if (m_interfToAdd.remove(ip))
					addinterfacetodelete=false;
			}
		}

		if (addinterfacetodelete && !m_serviceMap.containsKey(ip)) 
			m_interfToDel.add(ip);
		
		if (addservicetodelte) {
			if (!m_serviceToDel.containsKey(ip))
				m_serviceToDel.put(ip, new HashSet<String>());
			m_serviceToDel.get(ip).add(service);
		}		
		setOnmsSyncOperations(OnmsSync.DBONLY);
	}
	
	public BasicInterface getInterface(String ipaddress) {
		if (ipaddress == null)
			return null;
		for (BasicInterface bi: m_serviceMap.keySet()) {
			if (ipaddress.equals(bi.getIp()))
				return bi;
		}
		return null;
	}
	
	public Map<BasicInterface,Set<String>> getServiceMap() {
		return m_serviceMap;
	}
			
	public Set<OnmsSync> getSyncOperations() {
		return m_syncoperations;
	}

	public void setReplaceState() {
		m_syncoperations.clear();
		m_syncoperations.add(OnmsSync.FALSE);
		m_onmstate = OnmsState.REPLACE;
	}
	
	public void setDeleteState() {
		m_syncoperations.clear();
		m_syncoperations.add(OnmsSync.FALSE);
		m_onmstate = OnmsState.DELETE;
	}

	public void setUpdateState() {
		m_onmstate = OnmsState.UPDATE;
	}

	public void setNoneState() {
		m_onmstate = OnmsState.NONE;
		m_syncoperations.clear();
	}

	public OnmsState getOnmstate() {
		return m_onmstate;
	}

	public void deleteOnmsSyncOperation(OnmsSync syncoperation) {
		m_syncoperations.remove(syncoperation);
		if (m_syncoperations.isEmpty())
			m_onmstate =  OnmsState.NONE;
	}
	
	public void setOnmsSyncOperations(OnmsSync syncoperation) {
		if (m_onmstate == OnmsState.DELETE || m_onmstate == OnmsState.NEW || m_onmstate == OnmsState.REPLACE) {
			m_syncoperations.clear();
			m_syncoperations.add(OnmsSync.FALSE);
		} else {
			m_onmstate = OnmsState.UPDATE;
			m_syncoperations.add(syncoperation);
		}
	}

	public String getForeignSource() {
		return m_foreignSource;
	}

	public void setForeignSource(String foreignSource) {
		m_foreignSource = foreignSource;
	}


}
