package org.opennms.vaadin.provision.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.vaadin.provision.core.DashBoardUtils;

public class BasicNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	// This contains all the ip->service in secondary table but does not contain primary->icmp and primary-snmp
	private Map<String,Set<String>> m_serviceMap;  

	protected Set<String> m_updatemap = new HashSet<String>();

	protected List<String> m_categoriesToAdd = new ArrayList<String>();
	protected List<String> m_categoriesToDel = new ArrayList<String>();
    
	protected List<String> m_interfToAdd = new ArrayList<String>();
	protected List<String> m_interfToDel = new ArrayList<String>();

	private Map<String,Set<String>> m_serviceToAdd = new HashMap<String, Set<String>>();
	private Map<String,Set<String>> m_serviceToDel = new HashMap<String, Set<String>>();

	private String m_label;
	private String m_descr;
	private String m_hostname;
	private String m_vrf;
	private String m_primary;
	private String m_parent;
	private String m_snmpProfile;
	private String m_city;
	private String m_address1;
	private String m_building;
	private String m_foreignId;
	protected boolean m_valid = true;
		
	public BasicNode(String label) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";
						
		m_descr="Provided by Provision Dashboard";
		
		m_valid=false;
		m_serviceMap = new HashMap<String, Set<String>>();
	}

	public BasicNode(String label, String vrf, String snmpProfile) {
		m_label = label;
		m_primary="0.0.0.0";
		m_hostname="";
				
		m_city="";
		m_address1="";

		m_vrf = vrf;
		m_snmpProfile = snmpProfile;
						
		m_descr="Provided by Provision Dashboard";
		
		m_valid=false;
		m_serviceMap = new HashMap<String, Set<String>>();
	}

	public BasicNode(
			Map<String,Set<String>> serviceMap,
			String descr, 
			String hostname,
			String vrf, 
			String primary, 
			String parent,
			String snmpProfile, 
			String city, 
			String address1, 
			String foreignId, 
			String building) {

		m_label = null;
		m_serviceMap = serviceMap;
		m_descr = descr;
		m_hostname = hostname;
		m_vrf = vrf;
		m_primary = primary;
		m_parent = parent;
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

	public List<String> getInterfToAdd() {
		return m_interfToAdd;
	}

	public List<String> getInterfToDel() {
		return m_interfToDel;
	}

	public Map<String, Set<String>> getServiceToAdd() {
		return m_serviceToAdd;
	}

	public Map<String, Set<String>> getServiceToDel() {
		return m_serviceToDel;
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
		if ( m_primary != null && !m_serviceMap.containsKey(m_primary)) 
			m_interfToDel.add(new String(m_primary));
		if (m_primary != null && m_serviceMap.containsKey(m_primary)) {
			if (!m_serviceToDel.containsKey(m_primary))
				m_serviceToDel.put(m_primary, new HashSet<String>());
			m_serviceToDel.get(m_primary).add("ICMP");
		}
		if (!m_serviceMap.containsKey(primary))
			m_interfToAdd.add(primary);
		if (!m_serviceToAdd.containsKey(primary))
			m_serviceToAdd.put(primary, new HashSet<String>());
		m_serviceToAdd.get(primary).add("ICMP");
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
	
	public Map<String,Set<String>> getServiceMap() {
		return m_serviceMap;
	}
	
	public Set<String> getSecondary() {
		return m_serviceMap.keySet();
	}
	
	public void setSecondary(Set<String> secondary) {
		m_serviceMap.clear();
		for (String ip: secondary) {
			Set<String> service = new HashSet<String>();
			service.add("ICMP");
			m_serviceMap.put(ip, service);
		}
	}

}