package org.opennms.vaadin.provision.dao;


import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyNodesService;
import org.opennms.rest.client.JerseyProvisionForeignSourceService;
import org.opennms.rest.client.JerseyProvisionRequisitionService;
import org.opennms.rest.client.JerseySnmpInfoService;
import org.opennms.rest.client.model.OnmsIpInterfaceList;
import org.opennms.web.svclayer.model.SnmpInfo;

public class OnmsDao {

	private JerseyClientImpl m_jerseyClient;
    
	private JerseyProvisionRequisitionService m_provisionService;
	private JerseyProvisionForeignSourceService m_foreignSourceService;
	private JerseyNodesService m_nodeService;
	private JerseySnmpInfoService m_snmpInfoService;

	public OnmsDao() {
    	m_provisionService = new JerseyProvisionRequisitionService();
    	m_nodeService = new JerseyNodesService();
    	m_snmpInfoService = new JerseySnmpInfoService();
    	m_foreignSourceService = new JerseyProvisionForeignSourceService();
	}
	
	public JerseyClientImpl getJerseyClient() {
		return m_jerseyClient;
	}

	public void setJerseyClient(JerseyClientImpl jerseyClient) {
		m_jerseyClient = jerseyClient;
	    m_nodeService.setJerseyClient(m_jerseyClient);
		m_provisionService.setJerseyClient(m_jerseyClient);
	    m_snmpInfoService.setJerseyClient(m_jerseyClient);
	    m_foreignSourceService.setJerseyClient(m_jerseyClient);
	}
	
	public Requisition getRequisition(String foreignSource) {
		return m_provisionService.get(foreignSource);
	}

	public RequisitionNode getRequisitionNode(String foreignSource, String foreignId) {
		return m_provisionService.getNode(foreignId,foreignSource);
	}

	public void createRequisition(String foreignSource) {
		m_provisionService.add(new Requisition(foreignSource));
	}
	
	public SnmpInfo getSnmpInfo(String ip) {
		return m_snmpInfoService.get(ip);
	}
	
	public void setSnmpInfo(String ip,SnmpInfo info) {
		m_snmpInfoService.set(ip, info);
	}
	
	public void updateRequisitionNode(String foreignSource, String foreignId, MultivaluedMap<String, String> map) {
		m_provisionService.update(foreignSource, foreignId, map);
	}

	public void updateRequisitionInterface(String foreignSource, String foreignId, String ipaddress,MultivaluedMap<String, String> map) {
		m_provisionService.update(foreignSource, foreignId, ipaddress,map);
	}

	public void addRequisitionAsset(String foreignSource, String foreignid, RequisitionAsset asset) {
		m_provisionService.add(foreignSource, foreignid, asset);
	}

	public void addRequisitionCategory(String foreignSource,String foreignid, RequisitionCategory category) {
		m_provisionService.add(foreignSource, foreignid, category);
	}

	public void addRequisitionInterface(String foreignSource,String foreignid, RequisitionInterface riface) {
		m_provisionService.add(foreignSource, foreignid, riface);
	}

	public void addRequisitionservice(String foreignSource,String foreignid, String ip,RequisitionMonitoredService service) {
		m_provisionService.add(foreignSource, foreignid, ip,service);
	}

	public void addOrReplacePolicy(String foreignSource,PolicyWrapper policy) {
		m_foreignSourceService.addOrReplace(foreignSource, policy);
	}
	
	public void addRequisitionNode(String foreignSource, RequisitionNode node) {
		m_provisionService.add(foreignSource, node);
	}
	
	public void deleteRequisitionNode(String foreignSource, String foreignId) {
		m_provisionService.deleteNode(foreignSource, foreignId);	
	}

	public void deleteRequisitionInterface(String foreignSource, String foreignId, String ipaddr) {
		m_provisionService.deleteInterface(foreignSource, foreignId,ipaddr);	
	}

	public void deleteRequisitionservice(String foreignSource,String foreignid, String ip,String service) {
		m_provisionService.deleteService(foreignSource, foreignid, ip,service);
	}

	public void deleteRequisitionCategory(String foreignSource,String foreignId, String category) {
		m_provisionService.deleteCategory(foreignSource, foreignId, category);
	}
	
	public void deletePolicy(String foreignSource, String policyName) {
		m_foreignSourceService.deletePolicy(foreignSource, policyName);
	}
	
	public OnmsNodeList findNodes(MultivaluedMap<String, String> queryParams) {
		return m_nodeService.find(queryParams);
	}
	
	public OnmsIpInterfaceList getNodeIpInterfaces(Integer nodeId) {
		return m_nodeService.getIpInterfaces(nodeId);
	}
	
	public void sync(String foreignSource) {
		m_provisionService.sync(foreignSource);
	}
	
	public void syncRescanExistingFalse(String foreignSource) {
		m_provisionService.syncRescanExistingFalse(foreignSource);
	}

	public void syncDbOnly(String foreignSource) {
		m_provisionService.syncDbOnly(foreignSource);
	}

}
