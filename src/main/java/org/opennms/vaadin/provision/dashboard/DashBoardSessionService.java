package org.opennms.vaadin.provision.dashboard;

import static org.opennms.vaadin.provision.core.DashBoardUtils.hasInvalidDnsBind9Label;
import static org.opennms.vaadin.provision.core.DashBoardUtils.hasUnSupportedDnsDomain;
import static org.opennms.vaadin.provision.core.DashBoardUtils.hasInvalidIp;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.web.svclayer.model.SnmpInfo;
import org.opennms.vaadin.provision.config.DashBoardConfig;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.BackupProfileDao;
import org.opennms.vaadin.provision.dao.DnsDomainDao;
import org.opennms.vaadin.provision.dao.DnsSubDomainDao;
import org.opennms.vaadin.provision.dao.FastServiceDeviceDao;
import org.opennms.vaadin.provision.dao.FastServiceLinkDao;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.VrfDao;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.Vrf;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.model.OnmsIpInterface;

import com.vaadin.data.util.BeanContainer;

public class DashBoardSessionService implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
	private final static Logger logger = Logger.getLogger(DashBoardSessionService.class.getName());	
			
	private Map<String,String> m_foreignIdNodeLabelMap = new HashMap<String, String>();
	private Map<String,String> m_nodeLabelForeignIdMap = new HashMap<String, String>();
	private Collection<String> m_primaryipcollection = new ArrayList<String>();

	private OnmsDao m_onmsDao;
	private String m_user;
	private DashBoardService m_service;
	
	public DashBoardConfig getConfig() {
		return m_service.getConfig();
	}
	
	public SnmpProfileDao getSnmpProfileContainer() {
		return m_service.getSnmpProfileContainer();
	}

	public BackupProfileDao getBackupProfileContainer() {
		return m_service.getBackupProfileContainer();
	}

	public VrfDao getVrfContainer() {
    	return m_service.getVrfContainer();
    }

	public FastServiceDeviceDao getFastServiceDeviceContainer() {
		return m_service.getFastServiceDeviceContainer();
    }
    
	public FastServiceLinkDao getFastServiceLinkContainer() {
		return m_service.getFastServiceLinkContainer();
    }

	public DnsDomainDao getDnsDomainContainer() {
		return m_service.getDnsDomainContainer();
	}

	public DnsSubDomainDao getDnsSubDomainContainer() {
		return m_service.getDnsSubDomainContainer();
	}

	public JobDao getJobContainer() {
		return m_service.getJobContainer();
	}

	public DashBoardService getService() {
		return m_service;
	}

	public void setService(DashBoardService service) {
		m_service = service;
	}
	
    public OnmsDao getOnmsDao() {
		return m_onmsDao;
	}

	public void setOnmsDao(OnmsDao onmsDao) {
		m_onmsDao = onmsDao;
	}

	public void logout() {
		logger.info("logged out: " + m_user);
		m_onmsDao.destroy();
	}
	
	public void login(String url, String username, String password) {
		logger.info("loggin user: " + username + "@" + url);
		m_onmsDao.setJerseyClient(
				new JerseyClientImpl(url,username,password));
		m_onmsDao.getSnmpInfo("127.0.0.1");
		logger.info("logged in user: " + username + "@" + url);
		m_user = username;
	}
	
	@SuppressWarnings("deprecation")
	public BeanContainer<String, TrentinoNetworkNode> getRequisitionContainer(String label, String foreignSource) {
		BeanContainer<String, TrentinoNetworkNode> requisitionContainer = new BeanContainer<String, TrentinoNetworkNode>(TrentinoNetworkNode.class);
		
		Map<String, BackupProfile> backupprofilemap = m_service.getBackupProfileContainer().getBackupProfileMap();
		requisitionContainer.setBeanIdProperty(label);
		
		Collection<Vrf> vrfs = m_service.getVrfContainer().getVrfMap().values();
		List<String> domains = m_service.getDnsDomainContainer().getDomains();
		List<String> subdomains = m_service.getDnsSubDomainContainer().getSubdomains();

		logger.info("getting requisition: " + foreignSource );
		
		Requisition req = m_onmsDao.getRequisition(foreignSource);
    	m_foreignIdNodeLabelMap.clear();
    	m_nodeLabelForeignIdMap.clear();
    	m_primaryipcollection.clear();
		
		for (RequisitionNode node : req.getNodes()) {
			logger.info("parsing requisition: " + foreignSource +", foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
			m_foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
		}
		
		for (RequisitionNode node : req.getNodes()) {
			boolean valid = true;
			String foreignId = node.getForeignId();
			if (foreignId == null)
				valid = false;
			String nodelabel = node.getNodeLabel();
			if (nodelabel == null)
				valid=false;

			String vrf = null;
			String hostname = null;
			for (String tnvrf: domains) {
				if (nodelabel.endsWith("."+tnvrf)) {
					vrf = tnvrf;
					hostname = nodelabel.substring(0,nodelabel.indexOf(vrf)-1);
					break;
				}
			}

			if (vrf == null) {
				hostname = nodelabel;
				valid = false;
			}
			
			String primary = null;
			String descr = null;
			List<String> secondary = new ArrayList<String>();
			for (RequisitionInterface ip: node.getInterfaces()) {
				logger.info("parsing requisition: " + foreignSource +", foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() == null)
					valid=false;
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} else {
					secondary.add(ip.getIpAddr());
				}
			}
			
			if (primary == null)
				valid = false;
			else if (hasInvalidIp(primary))
				valid = false;
				
			Vrf networkCategory = null;
			for (Vrf lvrf: vrfs) {
				if (node.getCategory(lvrf.getName()) != null && node.getCategory(lvrf.getNetworklevel()) != null) {
					networkCategory = lvrf;
					break;
				}
			}
			if (networkCategory == null)
				valid = false;

			String notifCategory=null;
			for (String fcat: DashBoardUtils.m_notify_levels) {
				if (node.getCategory(fcat) != null ) {
					notifCategory = fcat;
					break;
				}
			}
			if (notifCategory == null)
				valid = false;
			
			String threshCategory = null;
			for (String tcat: DashBoardUtils.m_threshold_levels) {
				if (node.getCategory(tcat) != null ) {
					threshCategory = tcat;
					break;
				}
			}
			if (threshCategory == null)
				valid = false;
			
			String city = null;
			if (node.getCity() != null)
				city = node.getCity();
			else
				valid = false;
			
			String address1 = null;
			if (node.getAsset(DashBoardUtils.ADDRESS1) != null)
				address1 = node.getAsset(DashBoardUtils.ADDRESS1).getValue();
			else
				valid = false;
			
			if (hasInvalidDnsBind9Label(nodelabel))
				valid = false;
			if (hostname != null && hasUnSupportedDnsDomain(hostname,nodelabel,subdomains))
				valid = false;		
			
			String parent = null;
			if (node.getParentForeignId() != null)
				parent =m_foreignIdNodeLabelMap.get(node.getParentForeignId());
			
			String snmpProfile =  null;
			logger.info("parsing requisition: " + foreignSource +", foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "secondary: " + secondary);

			TrentinoNetworkNode tnnode = new TrentinoNetworkNode(
					descr, 
					hostname, 
					vrf, 
					primary, 
					parent, 
					networkCategory, 
					notifCategory, 
					threshCategory, 
					snmpProfile, 
					DashBoardUtils.getBackupProfile(node, backupprofilemap), 
					city, 
					address1, 
					foreignId, 
					valid, 
					secondary.toArray(new String[secondary.size()]));
			requisitionContainer.addBean(tnnode);
			m_primaryipcollection.add(primary);
		}
		return requisitionContainer;
	}
	
	public void deleteNode(String foreignSource, RequisitionNode node) {
		if (node == null)
			return;
		for (RequisitionInterface riface: node.getInterfaces())
			m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(riface.getIpAddr()));
		m_onmsDao.deleteRequisitionNode(foreignSource, node.getForeignId());
	}
	
	public void createRequisition(String foreignSource) {
		logger.info("creating requisition: " + foreignSource );
		m_onmsDao.createRequisition(foreignSource);
	}

	public String getSnmpProfileName(String ip) throws SQLException {
		SnmpInfo info = m_onmsDao.getSnmpInfo(ip);
		for (Entry<String, SnmpProfile> snmpprofileentry : m_service.getSnmpProfileContainer().getSnmpProfileMap().entrySet()) {
			if (info.getReadCommunity().equals(snmpprofileentry.getValue().getCommunity()) &&
				info.getVersion().equals(snmpprofileentry.getValue().getVersion()) &&	
				info.getTimeout().intValue() == Integer.parseInt(snmpprofileentry.getValue().getTimeout()))
				return snmpprofileentry.getKey();
		}
		return null;
	}

	public void addSecondaryInterface(String foreignSource,String foreignId,String ipaddress, String descr) {
		RequisitionInterface ipsecondary = new RequisitionInterface();
		ipsecondary.setIpAddr(ipaddress);
		ipsecondary.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
		ipsecondary.setDescr(descr);
		ipsecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(ipsecondary.getIpAddr()));
		m_onmsDao.addRequisitionInterface(foreignSource, foreignId, ipsecondary);
	}

	
	public void addSecondaryInterface(String foreignSource,String foreignId,String ipaddress) {
		addSecondaryInterface(foreignSource, foreignId, ipaddress,"Provided by Provision Dashboard");
	}
	
	public void deleteNode(String foreignSource,TrentinoNetworkNode tnnode) {
		logger.info("Deleting node with foreignId: " + tnnode.getForeignId() + " primary: " + tnnode.getPrimary());
		if (tnnode.getPrimary() != null)
			m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(tnnode.getPrimary()));
		if (tnnode.getSecondary() != null) {
			for (String iface: tnnode.getSecondary())
				m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(iface));
		}
		m_onmsDao.deleteRequisitionNode(foreignSource, tnnode.getForeignId());
		String nodelabel = m_foreignIdNodeLabelMap.remove(tnnode.getForeignId());
		m_nodeLabelForeignIdMap.remove(nodelabel);
		m_primaryipcollection.remove(tnnode.getPrimary());
	}

	public void deleteInterface(String foreignSource,String foreignId, String ipaddr) {
		logger.info("Deleting policy for interface: " + ipaddr);
		m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(ipaddr));
		logger.info("Deleting interface" + ipaddr+" with foreignId: " + foreignId );
		m_onmsDao.deleteRequisitionInterface(foreignSource, foreignId, ipaddr);
	}
	
	public Collection<String> getNodeLabels() {
		return m_foreignIdNodeLabelMap.values();
	}

	public Collection<String> getForeignIds() {
		return m_foreignIdNodeLabelMap.keySet();
	}
		
	public Collection<String> getPrimaryIpCollection() {
		return m_primaryipcollection;
	}

	public List<String> getIpAddresses(String foreignSource,String nodelabel) {
	   	List<String> ipaddresses = new ArrayList<String>();
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
    	queryParams.add("label", nodelabel);
    	queryParams.add("foreignSource", foreignSource);

		OnmsNodeList nodes = m_onmsDao.findNodes(queryParams);
		if (nodes.isEmpty() || nodes.size() >1)
			return ipaddresses; 
        for (OnmsIpInterface ipinterface: m_onmsDao.getNodeIpInterfaces(nodes.getObjects().iterator().next().getId()))
        	ipaddresses.add(ipinterface.getIpAddress().getHostAddress());        
        return ipaddresses;
	}

	public Set<String> checkUniqueNodeLabel() {
		Set<String> labels = new HashSet<String>();
		Set<String> duplicated = new HashSet<String>();
		for (String label: m_foreignIdNodeLabelMap.values()) {
			if (labels.contains(label)) {
				duplicated.add(label);
			} else {
				labels.add(label);
			}
		}
		return duplicated;
	}
	
	public Set<String> checkUniqueForeignId() {
		Set<String> labels = new HashSet<String>();
		Set<String> duplicated = new HashSet<String>();
		for (String label: getForeignIds()) {
			if (labels.contains(label)) {
				duplicated.add(label);
			} else {
				labels.add(label);
			}
		}
		return duplicated;
	}

	public Set<String> checkUniquePrimary() {
		Set<String> primaries = new HashSet<String>();
		Set<String> duplicated = new HashSet<String>();
		for (String ip: getPrimaryIpCollection()) {
			if (primaries.contains(ip)) {
				duplicated.add(ip);
			} else {
				primaries.add(ip);
			}
		}
		return duplicated;
	}

	public boolean hasDuplicatedPrimary(String primary) {
		return m_primaryipcollection.contains(primary);
	}
	
	public boolean hasDuplicatedForeignId(String hostname) {
		for (String label: getForeignIds()) {
			if (label.equals(hostname)) 
				return true;
		}
		return false;
	}

	public boolean hasDuplicatedNodelabel(String nodelabel) {
		for (String label: getNodeLabels()) {
			if (label.toLowerCase(Locale.ENGLISH).equals(nodelabel.toLowerCase(Locale.ENGLISH))) 
				return true;
		}
		return false;
	}

	public void addNode(String foreignSource, TrentinoNetworkNode node) throws SQLException {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(node.getForeignId());
		
		requisitionNode.setNodeLabel(node.getNodeLabel());
		
		if (node.getParent() != null) {
			requisitionNode.setParentForeignId(m_nodeLabelForeignIdMap.get(node.getParent()));
		}
		
		requisitionNode.setCity(node.getCity());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getPrimary());
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
		iface.setDescr(node.getDescr());
		
		requisitionNode.putInterface(iface);

		if (node.getNetworkCategory() != null) {
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory().getName()));
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory().getNetworklevel()));
		}
		
		if (node.getNotifCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifCategory()));
		
		if (node.getThreshCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getThreshCategory()));
		
		if (node.getCity() != null && node.getAddress1() != null)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getCity() + " - " + node.getAddress1()));
		
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset("address1", node.getAddress1() ));
		
		for ( RequisitionAsset asset : m_service.getBackupProfileContainer().getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getPrimary(), m_service.getSnmpProfileContainer().getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(foreignSource, requisitionNode);
		logger.info("Adding policy for interface: " + node.getPrimary());
		m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(node.getPrimary()));
		
		m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
		m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		m_primaryipcollection.add(node.getPrimary());
		node.clear();
			
	}

	public void updateNode(String foreignSource, String foreignId,
			String primary, String descr, Map<String, String> update,
			List<String> interfaceToDel, List<String> interfaceToAdd,
			List<String> categoriesToDel, List<String> categoriesToAdd, BackupProfile bck) {
		if (update.containsKey(DashBoardUtils.SNMP_PROFILE) && primary != null)
			m_onmsDao.setSnmpInfo(primary, m_service.getSnmpProfileContainer()
					.getSnmpProfile(update.get(DashBoardUtils.SNMP_PROFILE))
					.getSnmpInfo());

		MultivaluedMap<String, String> updatemap = new MultivaluedMapImpl();
		List<RequisitionAsset> assetsToPut = new ArrayList<RequisitionAsset>();

		if (update.containsKey(DashBoardUtils.PARENT))
			updatemap.add("parent-foreign-id", m_nodeLabelForeignIdMap
					.get(update.get(DashBoardUtils.PARENT)));
		if (update.containsKey(DashBoardUtils.LABEL))
			updatemap.add("node-label", update.get(DashBoardUtils.LABEL));
		if (update.containsKey(DashBoardUtils.CITY))
			updatemap.add("city", update.get(DashBoardUtils.CITY));
		if (update.containsKey(DashBoardUtils.ADDRESS1))
			assetsToPut.add(new RequisitionAsset("address1", update
					.get(DashBoardUtils.ADDRESS1)));
		if (update.containsKey(DashBoardUtils.DESCRIPTION))
			assetsToPut.add(new RequisitionAsset("description", update
					.get(DashBoardUtils.DESCRIPTION)));
		if (update.containsKey(DashBoardUtils.BUILDING))
			assetsToPut.add(new RequisitionAsset("building", update
					.get(DashBoardUtils.BUILDING)));
		if (update.containsKey(DashBoardUtils.CIRCUITID)) {
			updatemap.add("building", update.get(DashBoardUtils.CIRCUITID));
			assetsToPut.add(new RequisitionAsset("circuitId", update
					.get(DashBoardUtils.CIRCUITID)));
		}

		if (!updatemap.isEmpty())
			m_onmsDao
					.updateRequisitionNode(foreignSource, foreignId, updatemap);

		for (String ip : interfaceToDel) {
			m_onmsDao.deletePolicy(foreignSource,
					DashBoardUtils.getPolicyName(ip));
			m_onmsDao.deleteRequisitionInterface(foreignSource, foreignId, ip);
			m_primaryipcollection.remove(ip);
		}

		for (String ip : interfaceToAdd) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setIpAddr(ip);
			iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			iface.setDescr(descr);
			if (primary.equals(ip)) {
				m_primaryipcollection.add(ip);
				iface.putMonitoredService(new RequisitionMonitoredService(
						"SNMP"));
				iface.setSnmpPrimary(PrimaryType.PRIMARY);
			} else {
				iface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			}
			m_onmsDao.addRequisitionInterface(foreignSource, foreignId, iface);
			m_onmsDao.addOrReplacePolicy(foreignSource,
					DashBoardUtils.getPolicyWrapper(ip));
		}

		for (RequisitionAsset asset : assetsToPut) {
			m_onmsDao.addRequisitionAsset(foreignSource, foreignId, asset);
		}

		for (String category : categoriesToDel) {
			m_onmsDao.deleteRequisitionCategory(foreignSource, foreignId,
					category);
		}

		for (String category : categoriesToAdd) {
			m_onmsDao.addRequisitionCategory(foreignSource, foreignId,
					new RequisitionCategory(category));
		}

		if (update.containsKey(DashBoardUtils.BACKUP_PROFILE) && bck != null) {
			for (RequisitionAsset asset : bck.getRequisitionAssets().getAssets()) {
				m_onmsDao.addRequisitionAsset(foreignSource, foreignId, asset);
			}
		}
		if (update.containsKey(DashBoardUtils.LABEL)) {
			m_foreignIdNodeLabelMap.put(foreignId,
					update.get(DashBoardUtils.LABEL));
			m_nodeLabelForeignIdMap.put(update.get(DashBoardUtils.LABEL),
					foreignId);
		}

	}

	public void updateNode(String foreignSource, TrentinoNetworkNode node) {
		Map<String, String> update = new HashMap<String, String>();

		if (node.getUpdatemap().contains(DashBoardUtils.SNMP_PROFILE)
				&& node.getPrimary() != null)
			update.put(DashBoardUtils.SNMP_PROFILE, node.getSnmpProfile());
		if (node.getUpdatemap().contains(DashBoardUtils.PARENT))
			update.put(DashBoardUtils.PARENT,
					m_nodeLabelForeignIdMap.get(node.getParent()));
		if (node.getUpdatemap().contains(DashBoardUtils.HOST)
				|| node.getUpdatemap().contains(DashBoardUtils.VRF))
			update.put(DashBoardUtils.LABEL, node.getNodeLabel());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY))
			update.put(DashBoardUtils.CITY, node.getCity());
		if (node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.ADDRESS1, node.getAddress1());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY)
				|| node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.DESCRIPTION, node.getCity() + " - "
					+ node.getAddress1());
		if (node.getUpdatemap().contains(DashBoardUtils.BACKUP_PROFILE)
				&& node.getBackupProfile() != null)
			update.put(DashBoardUtils.BACKUP_PROFILE, node.getBackupProfile());
		BackupProfile bck = m_service.getBackupProfileContainer().get(node.getBackupProfile());
		updateNode(foreignSource, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),bck);
		node.clear();
	}
	
	public void addNode(String foreignSource, String foreignId, FastServiceDevice node, FastServiceLink link, Vrf vrf, Set<String> secondary) {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(foreignId);
		requisitionNode.setNodeLabel(foreignId+"."+vrf.getDnsdomain());
		
		if (node.getCity() != null)
			requisitionNode.setCity(node.getCity());
		if (link.getDeliveryCode() != null)
			requisitionNode.setBuilding(link.getDeliveryCode());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getIpaddr());
		iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
		iface.setDescr("provided by FAST");
		requisitionNode.putInterface(iface);

		for (String ip: secondary) {
			RequisitionInterface ifacesecondary = new RequisitionInterface();
			ifacesecondary.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			ifacesecondary.setIpAddr(ip);
			ifacesecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			ifacesecondary.setDescr("provided by FAST");
			requisitionNode.putInterface(ifacesecondary);
		}

		requisitionNode.putCategory(new RequisitionCategory(vrf.getNetworklevel()));
		requisitionNode.putCategory(new RequisitionCategory(vrf.getName()));
		
		if (node.getNotifyCategory().equals(DashBoardUtils.m_fast_default_notify))
			requisitionNode.putCategory(new RequisitionCategory(vrf.getNotifylevel()));
		else
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifyCategory()));
			
		
		requisitionNode.putCategory(new RequisitionCategory(vrf.getThresholdlevel()));
		
		StringBuffer address1 = new StringBuffer();
		if (node.getAddressDescr() != null)
			address1.append(node.getAddressDescr());
		if (node.getAddressName() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(node.getAddressName());
		}
		if (node.getAddressNumber() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(node.getAddressNumber());
		}
		
		if (node.getCity() != null &&  address1.length() > 0)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getCity() + " - " + address1.toString()));
		
		if (address1.length() > 0 )
			requisitionNode.putAsset(new RequisitionAsset("address1", address1.toString()));
		
		if (link.getDeliveryCode() != null)
			requisitionNode.putAsset(new RequisitionAsset("circuitId", link.getDeliveryCode()));
		
		if (node.getIstat() != null && link.getSiteCode() !=  null )
			requisitionNode.putAsset(new RequisitionAsset("building", node.getIstat()+"-"+link.getSiteCode()));

		for ( RequisitionAsset asset : m_service.getBackupProfileContainer().getBackupProfile(node.getBackupprofile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getIpaddr(), m_service.getSnmpProfileContainer().getSnmpProfile(node.getSnmpprofile()).getSnmpInfo());
			
		logger.info("Adding node with foreignId: " + foreignId + " primary: " + node.getIpaddr());
		m_onmsDao.addRequisitionNode(foreignSource, requisitionNode);
		logger.info("Adding policy for interface: " + node.getIpaddr());
		m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(node.getIpaddr()));
		for (String ip: secondary) {
			logger.info("Adding policy for interface: " + ip);
			m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(ip));			
		}

	}
	public String getUser() {
		return m_user;
	}

	public void setUser(String user) {
		m_user = user;
	}
		
    public boolean isFastRunning() {
    	return m_service.getJobContainer().isFastRunning();
    }
    
    public void sync(String foregnSource) {
    	m_onmsDao.sync(foregnSource);
    }

}
