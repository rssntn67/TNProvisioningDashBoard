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
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.TNDao;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.Vrf;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.model.OnmsIpInterface;

import com.vaadin.data.util.BeanContainer;


public class DashBoardService implements Serializable {
    /**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());	
			
	private Map<String,String> m_foreignIdNodeLabelMap;
	private Map<String,String> m_nodeLabelForeignIdMap;
	private Collection<String> m_primaryipcollection;
	private OnmsDao m_onmsDao;
	private TNDao m_tnDao;
	private DashBoardConfig m_config;

	
	public OnmsDao getOnmsDao() {
		return m_onmsDao;
	}

	public void setOnmsDao(OnmsDao onmsDao) {
		m_onmsDao = onmsDao;
	}

	public TNDao getTnDao() {
		return m_tnDao;
	}

	public void setTnDao(TNDao tnDao) {
		m_tnDao = tnDao;
	}

	public DashBoardConfig getConfig() {
		return m_config;
	}

	public void setConfig(DashBoardConfig config) {
		m_config = config;
	}

	public DashBoardService() {
    }
	
	public void logout() {
		logger.info("logged out");
		m_onmsDao.destroy();
		m_tnDao.destroy();
	}
	
	public void login(String url, String username, String password) throws SQLException {
		logger.info("loggin user: " + username + "@" + url);
		m_onmsDao.setJerseyClient(
				new JerseyClientImpl(url,username,password));
		m_onmsDao.getSnmpInfo("127.0.0.1");
		logger.info("logged in user: " + username + "@" + url);
		m_tnDao.init("org.postgresql.Driver", m_config.getDbUrl(), m_config.getDbUsername(), m_config.getDbPassword());
		logger.info("connected to database: " + m_config.getDbUrl());
	}

	public BeanContainer<String, TrentinoNetworkNode> getRequisitionContainer(String label, String foreignSource,Map<String, BackupProfile> backupprofilemap) {
		BeanContainer<String, TrentinoNetworkNode> requisitionContainer = new BeanContainer<String, TrentinoNetworkNode>(TrentinoNetworkNode.class);
		m_foreignIdNodeLabelMap = new HashMap<String, String>();
		m_nodeLabelForeignIdMap = new HashMap<String, String>();
		m_primaryipcollection = new ArrayList<String>();
		requisitionContainer.setBeanIdProperty(label);
		logger.info("getting requisition: " + foreignSource );
		Requisition req = m_onmsDao.getRequisition(foreignSource);
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
			for (String tnvrf: m_tnDao.getDomains()) {
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
			for (Vrf lvrf: m_tnDao.getVrfs().values()) {
				if (node.getCategory(lvrf.getName()) != null && node.getCategory(lvrf.getNetworklevel()) != null) {
					networkCategory = lvrf;
					break;
				}
			}
			if (networkCategory == null)
				valid = false;

			String notifCategory=null;
			for (String fcat: TNDao.m_notif_categories) {
				if (node.getCategory(fcat) != null ) {
					notifCategory = fcat;
					break;
				}
			}
			if (notifCategory == null)
				valid = false;
			
			String threshCategory = null;
			for (String tcat: TNDao.m_thresh_categories) {
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
			if (node.getAsset(TrentinoNetworkTab.ADDRESS1) != null)
				address1 = node.getAsset(TrentinoNetworkTab.ADDRESS1).getValue();
			else
				valid = false;
			
			if (hasInvalidDnsBind9Label(nodelabel))
				valid = false;
			if (hostname != null && hasUnSupportedDnsDomain(hostname,nodelabel,m_tnDao.getSubdomains()))
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

	public void createRequisition(String foreignSource) {
		logger.info("creating requisition: " + foreignSource );
		m_onmsDao.createRequisition(foreignSource);
	}

	public String getSnmpProfile(String ip) throws SQLException {
		SnmpInfo info = m_onmsDao.getSnmpInfo(ip);
		for (Entry<String, SnmpProfile> snmpprofileentry : m_tnDao.getSnmpProfiles().entrySet()) {
			if (info.getReadCommunity().equals(snmpprofileentry.getValue().getCommunity()) &&
				info.getVersion().equals(snmpprofileentry.getValue().getVersion()) &&	
				info.getTimeout().intValue() == Integer.parseInt(snmpprofileentry.getValue().getTimeout()))
				return snmpprofileentry.getKey();
		}
		return null;
	}

	public void addSecondaryInterface(String foreignSource,String foreignId,String ipaddress) {
		RequisitionInterface ipsecondary = new RequisitionInterface();
		ipsecondary.setIpAddr(ipaddress);
		ipsecondary.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
		ipsecondary.setDescr("Provided by Provision Dashboard");
		ipsecondary.putMonitoredService(new RequisitionMonitoredService("ICMP"));
		m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(ipsecondary.getIpAddr()));
		m_onmsDao.addRequisitionInterface(foreignSource, foreignId, ipsecondary);
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
		
		for ( RequisitionAsset asset : m_tnDao.getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getPrimary(), m_tnDao.getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(foreignSource, requisitionNode);
		logger.info("Adding policy for interface: " + node.getPrimary());
		m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(node.getPrimary()));
		
		m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
		m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		m_primaryipcollection.add(node.getPrimary());
		node.clear();
			
	}

	
	
	public void updateNode(String foreignSource, TrentinoNetworkNode node) throws ProvisionDashboardValidationException, SQLException {
		
		if (node.getUpdatemap().contains(TrentinoNetworkTab.SNMP_PROFILE)  && node.getPrimary() != null )
			m_onmsDao.setSnmpInfo(node.getPrimary(), m_tnDao.getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		
		MultivaluedMap< String, String> updatemap=new MultivaluedMapImpl();
		List<RequisitionAsset> assetsToPut = new ArrayList<RequisitionAsset>();
				
		if (node.getUpdatemap().contains(TrentinoNetworkTab.PARENT))
			updatemap.add("parent-foreign-id", m_nodeLabelForeignIdMap.get(node.getParent()));
		if (node.getUpdatemap().contains(TrentinoNetworkTab.HOST) || node.getUpdatemap().contains(TrentinoNetworkTab.VRF))
			updatemap.add("node-label", node.getNodeLabel());
		if (node.getUpdatemap().contains(TrentinoNetworkTab.CITY))
			updatemap.add("city", node.getCity());
		if (node.getUpdatemap().contains(TrentinoNetworkTab.ADDRESS1))
			 assetsToPut.add(new RequisitionAsset("address1", node.getAddress1()));
		if (node.getUpdatemap().contains(TrentinoNetworkTab.CITY) || node.getUpdatemap().contains(TrentinoNetworkTab.ADDRESS1))
			assetsToPut.add(new RequisitionAsset("description", node.getCity() + " - " + node.getAddress1()));
		
		if (!updatemap.isEmpty())
			m_onmsDao.updateRequisitionNode(foreignSource, node.getForeignId(), updatemap);

		for (String ip: node.getInterfToDel()) {
			m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(ip));
			m_onmsDao.deleteRequisitionInterface(foreignSource, node.getForeignId(), ip);
			m_primaryipcollection.remove(ip);
		}
		
		for (String ip: node.getInterfToAdd()) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setIpAddr(ip);
			iface.putMonitoredService(new RequisitionMonitoredService("ICMP"));
			iface.putMonitoredService(new RequisitionMonitoredService("SNMP"));
			iface.setDescr(node.getDescr());
			if (node.getPrimary().equals(ip)) {
				m_primaryipcollection.add(ip);
				iface.setSnmpPrimary(PrimaryType.PRIMARY);
			} else
				iface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			m_onmsDao.addRequisitionInterface(foreignSource, node.getForeignId(), iface);
			m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(ip));
		}
		
		for (RequisitionAsset asset: assetsToPut ) {
			m_onmsDao.addRequisitionAsset(foreignSource, node.getForeignId(), asset);
		}
		
		for (String category: node.getCategoriesToDel()) {
			m_onmsDao.deleteRequisitionCategory(foreignSource, node.getForeignId(), category);
		}
		
		for (String category: node.getCategoriesToAdd()) {
			m_onmsDao.addRequisitionCategory(foreignSource, node.getForeignId(), new RequisitionCategory(category));
		}
		
		if (node.getUpdatemap().contains(TrentinoNetworkTab.BACKUP_PROFILE) && node.getBackupProfile() != null ) {
			for ( RequisitionAsset asset : m_tnDao.getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
				m_onmsDao.addRequisitionAsset(foreignSource, node.getForeignId(), asset);;
			}
		}
		if (node.getUpdatemap().contains(TrentinoNetworkTab.HOST) || node.getUpdatemap().contains(TrentinoNetworkTab.VRF)) {
			m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		}
		
		node.clear();
	}
}
