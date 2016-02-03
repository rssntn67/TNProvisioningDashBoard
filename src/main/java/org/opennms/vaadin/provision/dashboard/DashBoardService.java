package org.opennms.vaadin.provision.dashboard;

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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
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
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.TrentinoNetworkRequisitionNode;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.opennms.rest.client.JerseyClientImpl;

import com.vaadin.data.util.BeanContainer;

public class DashBoardService implements Serializable {
    /**
	 * 
	 */

	private static final long serialVersionUID = 508580392774265535L;
	private final static Logger logger = Logger.getLogger(DashBoardService.class.getName());	
			
//	private String m_username;
//	private String m_url;
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
		m_tnDao.createPool("org.postgresql.Driver", m_config.getDbUrl(), m_config.getDbUsername(), m_config.getDbPassword());
		logger.info("connected to database: " + m_config.getDbUrl());
	}

	public BeanContainer<String, TrentinoNetworkRequisitionNode> getRequisitionContainer(String label, String foreignSource,Map<String, BackupProfile> backupprofilemap) {
		BeanContainer<String, TrentinoNetworkRequisitionNode> requisitionContainer = new BeanContainer<String, TrentinoNetworkRequisitionNode>(TrentinoNetworkRequisitionNode.class);
		m_foreignIdNodeLabelMap = new HashMap<String, String>();
		m_nodeLabelForeignIdMap = new HashMap<String, String>();
		m_primaryipcollection = new ArrayList<String>();
		requisitionContainer.setBeanIdProperty(label);
		logger.info("getting requisition: " + foreignSource );
		Requisition req = m_onmsDao.getRequisition(foreignSource);
		for (RequisitionNode node : req.getNodes()) {
			m_foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
		}
		for (RequisitionNode node : req.getNodes()) {
			TrentinoNetworkRequisitionNode tnnode = new TrentinoNetworkRequisitionNode(node,m_foreignIdNodeLabelMap.get(node.getParentForeignId()));
			tnnode.setBackupProfile(DashBoardUtils.getBackupProfile(node, backupprofilemap));
			requisitionContainer.addBean(tnnode);
			m_primaryipcollection.add(tnnode.getPrimary());
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
				info.getTimeout() == snmpprofileentry.getValue().getTimeout().intValue())
				return snmpprofileentry.getKey();
		}
		return null;
	}

	public void setSnmpInfo(String ip, String snmpProfile) throws SQLException {
		m_onmsDao.setSnmpInfo(ip, m_tnDao.getSnmpProfile(snmpProfile).getSnmpInfo());
	}

	public void update(String foreignSource, String foreignId, MultivaluedMap<String, String> map) {
		m_onmsDao.updateRequisitionNode(foreignSource, foreignId, map);
	}
		

	public void add(String foreignSource, String foreignid, RequisitionAsset asset) {
		m_onmsDao.addRequisitionAsset(foreignSource, foreignid, asset);
	}

	public void add(String foreignSource,String foreignid, RequisitionCategory category) {
		m_onmsDao.addRequisitionCategory(foreignSource, foreignid, category);
	}

	public void add(String foreignSource,String foreignid, RequisitionInterface riface) {
		m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(riface.getIpAddr()));
		m_onmsDao.addRequisitionInterface(foreignSource, foreignid, riface);
	}

	public void add(String foreignSource,RequisitionNode node, String primary) {
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + primary);
		m_onmsDao.addRequisitionNode(foreignSource, node);
		for (RequisitionInterface riface: node.getInterfaces()) {
			logger.info("Adding policy for interface: " + riface.getIpAddr());
			m_onmsDao.addOrReplacePolicy(foreignSource, DashBoardUtils.getPolicyWrapper(riface.getIpAddr()));
		}
		m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
		m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		m_primaryipcollection.add(primary);
		
	}
	
	public void deleteNode(String foreignSource,TrentinoNetworkRequisitionNode tnnode) {
		logger.info("Deleting node with foreignId: " + tnnode.getForeignId() + " primary: " + tnnode.getPrimary());
		m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(tnnode.getPrimary()));
		for (RequisitionInterface iface: m_onmsDao.getRequisitionNode(foreignSource, tnnode.getForeignId()).getInterfaces())
			m_onmsDao.deletePolicy(foreignSource, DashBoardUtils.getPolicyName(iface.getIpAddr()));
		m_onmsDao.deleteRequisitionNode(foreignSource, tnnode.getForeignId());
		String nodelabel = m_foreignIdNodeLabelMap.remove(tnnode.getForeignId());
		m_nodeLabelForeignIdMap.remove(nodelabel);
		m_primaryipcollection.remove(tnnode.getPrimary());
	}

	public void deleteCategory(String foreignSource,String foreignId, String category) {
		m_onmsDao.deleteRequisitionCategory(foreignSource, foreignId, category);
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

	public RequisitionNode getRequisitionNode(TrentinoNetworkRequisitionNode node) throws SQLException {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		if (node.getForeignId() != null)
			requisitionNode.setForeignId(node.getForeignId());
		else 
			requisitionNode.setForeignId(node.getHostname());
		
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
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory()[0]));
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory()[1]));
		}
		
		if (node.getNotifCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifCategory()));
		if (node.getThreshCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getThreshCategory()));
		
		if (node.getCity() != null && node.getAddress1() != null)
			requisitionNode.putAsset(new RequisitionAsset(OnmsDao.DESCRIPTION, node.getCity() + " - " + node.getAddress1()));
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset(OnmsDao.ADDRESS, node.getAddress1() ));
		
		for ( RequisitionAsset backupProfileItem : m_tnDao.getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(backupProfileItem);
		}
		return requisitionNode;
	}

	
	
	public void save(TrentinoNetworkRequisitionNode node) throws ProvisionDashboardValidationException {
		InetAddressUtils.getInetAddress(node.getPrimary());
		/*
		if (updatenodeLabel && vrf != null && hostname != null)
			updateNodeLabel();
		
		if (updatesnmpprofile && snmpProfile != null && primary != null )
			m_service.setSnmpInfo(primary, snmpProfile);
		
		if (!update) {
			if (m_service.hasDuplicatedForeignId(hostname))
							throw new ProvisionDashboardValidationException("The foreign id exist: cannot duplicate foreignId: " + hostname);
			if (m_service.hasDuplicatedPrimary(primary))
				throw new ProvisionDashboardValidationException("The primary ip exist: cannot duplicate primary: " + primary);
			m_service.add(getRequisitionNode(),primary);
			valid = true;
			update=true;
		} else {
			if (!m_updatemap.isEmpty())
				m_service.update(foreignId, m_updatemap);
			for (RequisitionInterface riface: m_interfToDel) {
				m_service.deleteNode(foreignId, riface);
			}
			for (RequisitionInterface riface: m_interfToAdd) {
				m_service.add(foreignId, riface);
			}
			for (RequisitionAsset asset: m_assetsToPut) {
				m_service.add(foreignId, asset);
			}
			for (RequisitionCategory category: m_categoriesToDel) {
				m_service.deleteNode(foreignId, category);
			}
			for (RequisitionCategory category: m_categoriesToAdd) {
				m_service.add(foreignId, category);
			}
		}

		updatenodeLabel = false;
		updatesnmpprofile = false;
		*/
	}
}
