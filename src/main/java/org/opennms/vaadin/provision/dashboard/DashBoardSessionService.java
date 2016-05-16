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
import org.opennms.vaadin.provision.dao.JobLogDao;
import org.opennms.vaadin.provision.dao.OnmsDao;
import org.opennms.vaadin.provision.dao.SnmpProfileDao;
import org.opennms.vaadin.provision.dao.VrfDao;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.MediaGatewayNode;
import org.opennms.vaadin.provision.model.SistemiInformativiNode;
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

	public JobLogDao getJobLogContainer() {
		return m_service.getJobLogContainer();
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
	public BeanContainer<String, SistemiInformativiNode> getSIContainer() {
		BeanContainer<String, SistemiInformativiNode> requisitionContainer = new BeanContainer<String, SistemiInformativiNode>(SistemiInformativiNode.class);
		requisitionContainer.setBeanIdProperty(DashBoardUtils.LABEL);
		List<String> domains = m_service.getDnsDomainContainer().getDomains();
		List<String> subdomains = m_service.getDnsSubDomainContainer().getSubdomains();
		for (RequisitionNode node : m_onmsDao.getRequisition(DashBoardUtils.SI_REQU_NAME).getNodes()) {
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
			Map<String, Set<String>> serviceMap = new HashMap<String, Set<String>>();
			for (RequisitionInterface ip: node.getInterfaces()) {
				logger.info("parsing foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() == null)
					valid=false;
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} 
				for (RequisitionMonitoredService service: ip.getMonitoredServices()) {
					if (primary != null && ip.getIpAddr().equals(primary) && (service.getServiceName().equals("ICMP") || service.getServiceName().equals("SNMP")))
						continue;
					Set<String> services = serviceMap.get(ip.getIpAddr());
					if (services == null)
						services = new HashSet<String>();
					services.add(service.getServiceName());
					serviceMap.put(ip.getIpAddr(), services);
				}
			}

			if (primary == null)
				valid = false;
			else if (hasInvalidIp(primary))
				valid = false;

			String[] serverLevelCategory = null;
			for (String[] levels : DashBoardUtils.m_server_levels) {
				if (node.getCategory(levels[0]) != null && node.getCategory(levels[1]) != null) {
					serverLevelCategory = levels;
					break;
				}
			}
			if (serverLevelCategory == null)
				valid = false;

			String managedByCategory = null;
			for (String[] managedby: DashBoardUtils.m_server_managedby) {
				if (node.getCategory(managedby[0]) != null) {
					managedByCategory = managedby[0];
					break;
				}				
			}
			if (managedByCategory == null)
				valid = false;
			
			String notifCategory = null;
			for (String notif: DashBoardUtils.m_server_notif) {
				if (node.getCategory(notif) != null) {
					notifCategory = notif;
					break;
				}				
			}

			String optionalCategory = null;
			for (String option: DashBoardUtils.m_server_optional) {
				if (node.getCategory(option) != null) {
					optionalCategory = option;
					break;
				}				
			}

			String prodCategory = null;
			for (String prod: DashBoardUtils.m_server_prod) {
				if (node.getCategory(prod) != null) {
					prodCategory = prod;
					break;
				}				
			}

			String tnCategory = null;
			if (node.getCategory(DashBoardUtils.TN_REQU_NAME) == null) {
				logger.info("no TrentinoNetwork category on SI node: foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
				valid = false;
			} else {
				tnCategory = DashBoardUtils.TN_REQU_NAME;
			}
			
			String city = node.getCity();
			String building = node.getBuilding();
			
			String address1 = null;
			if (node.getAsset(DashBoardUtils.ADDRESS1) != null)
				address1 = node.getAsset(DashBoardUtils.ADDRESS1).getValue();

			String description = null;
			if (node.getAsset(DashBoardUtils.DESCRIPTION) != null)
				description = node.getAsset(DashBoardUtils.DESCRIPTION).getValue();

			String leaseExpires = null;
			if (node.getAsset(DashBoardUtils.LEASEEXPIRES) != null)
				leaseExpires = node.getAsset(DashBoardUtils.LEASEEXPIRES).getValue();

			String lease = null;
			if (node.getAsset(DashBoardUtils.LEASE) != null)
				lease = node.getAsset(DashBoardUtils.LEASE).getValue();
			
			String vendorPhone = null;
			if (node.getAsset(DashBoardUtils.VENDORPHONE) != null)
				vendorPhone = node.getAsset(DashBoardUtils.VENDORPHONE).getValue();

			String vendor = null;
			if (node.getAsset(DashBoardUtils.VENDOR) != null)
				vendor = node.getAsset(DashBoardUtils.VENDOR).getValue();

			String slot = null;
			if (node.getAsset(DashBoardUtils.SLOT) != null)
				slot = node.getAsset(DashBoardUtils.SLOT).getValue();

			String rack = null;
			if (node.getAsset(DashBoardUtils.RACK) != null)
				rack = node.getAsset(DashBoardUtils.RACK).getValue();

			String room = null;
			if (node.getAsset(DashBoardUtils.ROOM) != null)
				room = node.getAsset(DashBoardUtils.ROOM).getValue();

			String operatingSystem = null;
			if (node.getAsset(DashBoardUtils.OPERATINGSYSTEM) != null)
				operatingSystem = node.getAsset(DashBoardUtils.OPERATINGSYSTEM).getValue();

			String dateInstalled = null;
			if (node.getAsset(DashBoardUtils.DATEINSTALLED) != null)
				dateInstalled = node.getAsset(DashBoardUtils.DATEINSTALLED).getValue();

			String assetNumber = null;
			if (node.getAsset(DashBoardUtils.ASSETNUMBER) != null)
				assetNumber = node.getAsset(DashBoardUtils.ASSETNUMBER).getValue();

			String serialNumber = null;
			if (node.getAsset(DashBoardUtils.SERIALNUMBER) != null)
				serialNumber = node.getAsset(DashBoardUtils.SERIALNUMBER).getValue();

			String category = null;
			if (node.getAsset(DashBoardUtils.CATEGORY) != null)
				category = node.getAsset(DashBoardUtils.CATEGORY).getValue();

			String modelNumber = null;
			if (node.getAsset(DashBoardUtils.MODELNUMBER) != null)
				modelNumber = node.getAsset(DashBoardUtils.MODELNUMBER).getValue();

			String manufacturer = null;
			if (node.getAsset(DashBoardUtils.MANUFACTURER) != null)
				manufacturer = node.getAsset(DashBoardUtils.MANUFACTURER).getValue();

			if (hasInvalidDnsBind9Label(nodelabel))
				valid = false;
			if (hostname != null && hasUnSupportedDnsDomain(hostname,nodelabel,subdomains))
				valid = false;		

			SistemiInformativiNode sinode = new SistemiInformativiNode(serviceMap, descr, hostname, vrf, primary, 
					serverLevelCategory, managedByCategory, notifCategory, optionalCategory, 
					prodCategory, tnCategory, 
					city, address1, description, building, 
					leaseExpires, lease, vendorPhone, vendor, 
					slot, rack, room, 
					operatingSystem, dateInstalled, 
					assetNumber, serialNumber, category, 
					modelNumber, manufacturer, 
					foreignId, valid);
			requisitionContainer.addBean(sinode);
		}
		return requisitionContainer;
	}

	@SuppressWarnings("deprecation")
	public BeanContainer<String, TrentinoNetworkNode> getTNContainer() {
		BeanContainer<String, TrentinoNetworkNode> requisitionContainer = new BeanContainer<String, TrentinoNetworkNode>(TrentinoNetworkNode.class);
		
		Map<String, BackupProfile> backupprofilemap = m_service.getBackupProfileContainer().getBackupProfileMap();
		requisitionContainer.setBeanIdProperty(DashBoardUtils.LABEL);
		
		Collection<Vrf> vrfs = m_service.getVrfContainer().getVrfMap().values();
		List<String> domains = m_service.getDnsDomainContainer().getDomains();
		List<String> subdomains = m_service.getDnsSubDomainContainer().getSubdomains();
		
		Requisition req = m_onmsDao.getRequisition(DashBoardUtils.TN_REQU_NAME);
    	m_foreignIdNodeLabelMap.clear();
    	m_nodeLabelForeignIdMap.clear();
    	m_primaryipcollection.clear();
		
		for (RequisitionNode node : req.getNodes()) {
			m_foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
			for (RequisitionInterface ip: node.getInterfaces()) {
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					m_primaryipcollection.add(ip.getIpAddr());
				}
			}
		}
		
		for (RequisitionNode node : req.getNodes()) {
			if (node.getCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY) != null) {
				logger.info("skipping media gateway: foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
				continue;
			}
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
				logger.info("parsing foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() == null)
					valid=false;
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} else {
					logger.info("adding secondary: " + ip.getIpAddr() + " foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
					secondary.add(ip.getIpAddr());
				}
			}
			if (descr == null)
				descr = "Provisioned By TNPD";
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
			
			TrentinoNetworkNode tnnode = new TrentinoNetworkNode(
					descr, 
					hostname, 
					vrf, 
					primary, 
					parent, 
					networkCategory, 
					notifCategory, 
					threshCategory, 
					null, 
					DashBoardUtils.getBackupProfile(node, backupprofilemap), 
					city, 
					address1, 
					foreignId, 
					valid, 
					secondary.toArray(new String[secondary.size()]));
			requisitionContainer.addBean(tnnode);
		}
		return requisitionContainer;
	}

	@SuppressWarnings("deprecation")
	public BeanContainer<String, MediaGatewayNode> getMediaGatewayContainer() {
		BeanContainer<String, MediaGatewayNode> requisitionContainer = new BeanContainer<String, MediaGatewayNode>(MediaGatewayNode.class);
		
		Map<String, BackupProfile> backupprofilemap = m_service.getBackupProfileContainer().getBackupProfileMap();
		requisitionContainer.setBeanIdProperty(DashBoardUtils.LABEL);
		
		List<String> domains = m_service.getDnsDomainContainer().getDomains();
		List<String> subdomains = m_service.getDnsSubDomainContainer().getSubdomains();
		
		Requisition req = m_onmsDao.getRequisition(DashBoardUtils.TN_REQU_NAME);
    	m_foreignIdNodeLabelMap.clear();
    	m_nodeLabelForeignIdMap.clear();
    	m_primaryipcollection.clear();
		
		for (RequisitionNode node : req.getNodes()) {
			m_foreignIdNodeLabelMap.put(node.getForeignId(),node.getNodeLabel());
			m_nodeLabelForeignIdMap.put(node.getNodeLabel(),node.getForeignId());
			for (RequisitionInterface ip: node.getInterfaces()) {
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					m_primaryipcollection.add(ip.getIpAddr());
				}
			}
		}
		
		for (RequisitionNode node : req.getNodes()) {
			if (node.getCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY) == null) {
				continue;
			}
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
				logger.info("parsing foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel() + "ip address:" + ip.getIpAddr());
				if (ip.getSnmpPrimary() == null)
					valid=false;
				if (ip.getSnmpPrimary() != null && ip.getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					primary = ip.getIpAddr();
					descr = ip.getDescr();
				} else {
					logger.info("adding secondary: " + ip.getIpAddr() + " foreignid: " + node.getForeignId() + ", nodelabel: " + node.getNodeLabel());
					secondary.add(ip.getIpAddr());
				}
			}

			if (primary == null)
				valid = false;
			else if (hasInvalidIp(primary))
				valid = false;
				
			String networkCategory = null;
			if (node.getCategory(DashBoardUtils.MEDIAGATEWAY_NETWORK_CATEGORY) != null) {
				networkCategory = DashBoardUtils.MEDIAGATEWAY_NETWORK_CATEGORY;
			}
			if (networkCategory == null)
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
			
			MediaGatewayNode tnnode = new MediaGatewayNode(
					descr, 
					hostname, 
					vrf, 
					primary, 
					parent, 
					networkCategory, 
					null, 
					DashBoardUtils.getBackupProfile(node, backupprofilemap), 
					city, 
					address1, 
					foreignId, 
					valid);
			requisitionContainer.addBean(tnnode);
		}
		return requisitionContainer;	}

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

	public void deleteNode(SistemiInformativiNode sinode) {
		logger.info("Deleting SI node with foreignId: " + sinode.getForeignId() + " primary: " + sinode.getPrimary());
		m_onmsDao.deleteRequisitionNode(DashBoardUtils.SI_REQU_NAME, sinode.getForeignId());
	}
	
	public void deleteNode(TrentinoNetworkNode tnnode) {
		logger.info("Deleting TN node with foreignId: " + tnnode.getForeignId() + " primary: " + tnnode.getPrimary());
		if (tnnode.getPrimary() != null)
			m_onmsDao.deletePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyName(tnnode.getPrimary()));
		if (tnnode.getSecondary() != null) {
			for (String iface: tnnode.getSecondary())
				m_onmsDao.deletePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyName(iface));
		}
		m_onmsDao.deleteRequisitionNode(DashBoardUtils.TN_REQU_NAME, tnnode.getForeignId());
		String nodelabel = m_foreignIdNodeLabelMap.remove(tnnode.getForeignId());
		m_nodeLabelForeignIdMap.remove(nodelabel);
		m_primaryipcollection.remove(tnnode.getPrimary());
	}

	public void deleteNode(MediaGatewayNode tnnode) {
		logger.info("Deleting media gateway node with foreignId: " + tnnode.getForeignId() + " primary: " + tnnode.getPrimary());
		if (tnnode.getPrimary() != null)
			m_onmsDao.deletePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyName(tnnode.getPrimary()));
		m_onmsDao.deleteRequisitionNode(DashBoardUtils.TN_REQU_NAME, tnnode.getForeignId());
		String nodelabel = m_foreignIdNodeLabelMap.remove(tnnode.getForeignId());
		m_nodeLabelForeignIdMap.remove(nodelabel);
		m_primaryipcollection.remove(tnnode.getPrimary());
		RequisitionNode mediagateway = getMediaGateway();
		if (mediagateway == null)
			return;
		m_onmsDao.deleteRequisitionInterface(DashBoardUtils.SIVN_REQU_NAME, mediagateway.getForeignId(), tnnode.getPrimary());
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

	public RequisitionNode getMediaGateway() {
		for (RequisitionNode reqnode: m_onmsDao.getRequisition(DashBoardUtils.SIVN_REQU_NAME).getNodes()) {
			if (reqnode.getNodeLabel().equals("mediagateway")) {
				return reqnode;
			}
		}
		return null;
	}
	
	public void createMediaGateway() {
		RequisitionNode requisitionNode = new RequisitionNode();
		requisitionNode.setForeignId("mediagateway");
		requisitionNode.setNodeLabel("mediagateway");
		requisitionNode.putCategory(new RequisitionCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY));
		m_onmsDao.addRequisitionNode(DashBoardUtils.SIVN_REQU_NAME, requisitionNode);
	}

	public void addMediaGatewayNode(MediaGatewayNode node) throws SQLException {
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

		requisitionNode.putCategory(new RequisitionCategory(DashBoardUtils.MEDIAGATEWAY_CATEGORY));
		if (node.getNetworkCategory() != null) 
			requisitionNode.putCategory(new RequisitionCategory(node.getNetworkCategory()));

				
		if (node.getCity() != null && node.getAddress1() != null)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getCity() + " - " + node.getAddress1()));
		
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset("address1", node.getAddress1() ));
		
		for ( RequisitionAsset asset : m_service.getBackupProfileContainer().getBackupProfile(node.getBackupProfile()).getRequisitionAssets().getAssets()) {
			requisitionNode.putAsset(asset);
		}
		m_onmsDao.setSnmpInfo(node.getPrimary(), m_service.getSnmpProfileContainer().getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(DashBoardUtils.TN_REQU_NAME, requisitionNode);
		logger.info("Adding policy for interface: " + node.getPrimary());
		m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(node.getPrimary()));
		
		m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
		m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		m_primaryipcollection.add(node.getPrimary());
		node.clear();
		RequisitionNode mediagateway = getMediaGateway();
		if (mediagateway == null )
			return;
		RequisitionInterface mgiface = new RequisitionInterface();
		mgiface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
		mgiface.setIpAddr(node.getPrimary());
		mgiface.putMonitoredService(new RequisitionMonitoredService("PattonSIPCalls"));
		mgiface.setDescr(node.getDescr());
		
		m_onmsDao.addRequisitionInterface(DashBoardUtils.SIVN_REQU_NAME, mediagateway.getForeignId(),mgiface);
			
	}

	public void addSINode(SistemiInformativiNode node) {
		RequisitionNode requisitionNode = new RequisitionNode();
		
		requisitionNode.setForeignId(node.getForeignId());
		
		requisitionNode.setNodeLabel(node.getNodeLabel());
				
		requisitionNode.setCity(node.getCity());
		requisitionNode.setBuilding(node.getBuilding());
		
		RequisitionInterface iface = new RequisitionInterface();
		iface.setSnmpPrimary(PrimaryType.PRIMARY);
		iface.setIpAddr(node.getPrimary());
		iface.setDescr(node.getDescr());
		Set<String> primaryservices = node.getServiceMap().get(node.getPrimary());
		if (primaryservices == null ) {
			primaryservices = new HashSet<String>();
		} 
		primaryservices.add("ICMP");
		primaryservices.add("SNMP");
		for (String service: primaryservices)
			iface.putMonitoredService(new RequisitionMonitoredService(service));
		requisitionNode.putInterface(iface);

		for (String ip : node.getServiceMap().keySet()) {
			if (ip.equals(node.getPrimary()))
				continue;
			RequisitionInterface face = new RequisitionInterface();
			face.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			face.setIpAddr(ip);
			face.setDescr(node.getDescr());
			for (String service: node.getServiceMap().get(ip)) {
				face.putMonitoredService(new RequisitionMonitoredService(service));
			}
			requisitionNode.putInterface(face);
		}
		
		if (node.getServerLevelCategory() != null) {
			if (node.getServerLevelCategory()[0] != null)
				requisitionNode.putCategory(new RequisitionCategory(node.getServerLevelCategory()[0]));
			if (node.getServerLevelCategory()[1] != null)
				requisitionNode.putCategory(new RequisitionCategory(node.getServerLevelCategory()[1]));
		}
		
		requisitionNode.putCategory(new RequisitionCategory(DashBoardUtils.TN_REQU_NAME));
		
		if (node.getNotifCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getNotifCategory()));
		
		if (node.getProdCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getProdCategory()));

		if (node.getManagedByCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getManagedByCategory()));

		if (node.getOptionalCategory() != null)
			requisitionNode.putCategory(new RequisitionCategory(node.getOptionalCategory()));
		
		if (node.getAddress1()  != null)
			requisitionNode.putAsset(new RequisitionAsset("address1", node.getAddress1() ));

		if (node.getLeaseExpires() != null)
			requisitionNode.putAsset(new RequisitionAsset("leaseExpires", node.getLeaseExpires() ));

		if (node.getLease() != null)
			requisitionNode.putAsset(new RequisitionAsset("lease", node.getLease() ));

		if (node.getVendorPhone() != null)
			requisitionNode.putAsset(new RequisitionAsset("vendorPhone", node.getVendorPhone() ));

		if (node.getVendor() != null)
			requisitionNode.putAsset(new RequisitionAsset("vendor", node.getVendor() ));

		if (node.getSlot() != null)
			requisitionNode.putAsset(new RequisitionAsset("slot", node.getSlot() ));

		if (node.getRack() != null)
			requisitionNode.putAsset(new RequisitionAsset("rack", node.getRack() ));

		if (node.getRoom() != null)
			requisitionNode.putAsset(new RequisitionAsset("room", node.getRoom() ));

		if (node.getOperatingSystem() != null)
			requisitionNode.putAsset(new RequisitionAsset("operatingSystem", node.getOperatingSystem() ));

		if (node.getDateInstalled() != null)
			requisitionNode.putAsset(new RequisitionAsset("dateInstalled", node.getDateInstalled() ));

		if (node.getAssetNumber() != null)
			requisitionNode.putAsset(new RequisitionAsset("assetNumber", node.getAssetNumber() ));

		if (node.getSerialNumber() != null)
			requisitionNode.putAsset(new RequisitionAsset("serialNumber", node.getSerialNumber() ));

		if (node.getCategory() != null)
			requisitionNode.putAsset(new RequisitionAsset("category", node.getCategory() ));

		if (node.getModelNumber() != null)
			requisitionNode.putAsset(new RequisitionAsset("modelNumber", node.getModelNumber() ));

		if (node.getManufacturer() != null)
			requisitionNode.putAsset(new RequisitionAsset("manufacturer", node.getManufacturer() ));

		if (node.getDescription() != null)
			requisitionNode.putAsset(new RequisitionAsset("description", node.getDescription() ));

		m_onmsDao.setSnmpInfo(node.getPrimary(), m_service.getSnmpProfileContainer().getSnmpProfile(node.getSnmpProfile()).getSnmpInfo());
		logger.info("Adding node with foreignId: " + node.getForeignId() + " primary: " + node.getPrimary());
		m_onmsDao.addRequisitionNode(DashBoardUtils.SI_REQU_NAME, requisitionNode);
		
		node.clear();

	}
	public void updateSINode(SistemiInformativiNode node) {
		Map<String, String> update = new HashMap<String, String>();
		
		if (node.getUpdatemap().contains(DashBoardUtils.SNMP_PROFILE)
				&& node.getPrimary() != null)
			update.put(DashBoardUtils.SNMP_PROFILE, node.getSnmpProfile());
		if (node.getUpdatemap().contains(DashBoardUtils.HOST)
				|| node.getUpdatemap().contains(DashBoardUtils.VRF))
			update.put(DashBoardUtils.LABEL, node.getNodeLabel());
		if (node.getUpdatemap().contains(DashBoardUtils.CITY))
			update.put(DashBoardUtils.CITY, node.getCity());
		if (node.getUpdatemap().contains(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.ADDRESS1, node.getAddress1());
		if (node.getUpdatemap().contains(DashBoardUtils.BUILDING))
			update.put(DashBoardUtils.BUILDING_SCALAR, node.getBuilding());
		if (node.getUpdatemap().contains(DashBoardUtils.LEASEEXPIRES));
			update.put(DashBoardUtils.LEASEEXPIRES, node.getLeaseExpires());
		if (node.getUpdatemap().contains(DashBoardUtils.LEASE))
			update.put(DashBoardUtils.LEASE, node.getLease());
		if (node.getUpdatemap().contains(DashBoardUtils.VENDORPHONE))
			update.put(DashBoardUtils.VENDORPHONE, node.getVendorPhone());
		if (node.getUpdatemap().contains(DashBoardUtils.VENDOR))
			update.put(DashBoardUtils.VENDOR, node.getVendor());
		if (node.getUpdatemap().contains(DashBoardUtils.SLOT))
			update.put(DashBoardUtils.SLOT, node.getSlot());
		if (node.getUpdatemap().contains(DashBoardUtils.RACK))
			update.put(DashBoardUtils.RACK, node.getRack());
		if (node.getUpdatemap().contains(DashBoardUtils.ROOM))
			update.put(DashBoardUtils.ROOM, node.getRoom());
		if (node.getUpdatemap().contains(DashBoardUtils.OPERATINGSYSTEM))
			update.put(DashBoardUtils.OPERATINGSYSTEM, node.getOperatingSystem());
		if (node.getUpdatemap().contains(DashBoardUtils.DATEINSTALLED))
			update.put(DashBoardUtils.DATEINSTALLED, node.getDateInstalled());
		if (node.getUpdatemap().contains(DashBoardUtils.ASSETNUMBER))
			update.put(DashBoardUtils.ASSETNUMBER, node.getAssetNumber());
		if (node.getUpdatemap().contains(DashBoardUtils.SERIALNUMBER))
			update.put(DashBoardUtils.SERIALNUMBER, node.getSerialNumber());
		if (node.getUpdatemap().contains(DashBoardUtils.CATEGORY))
			update.put(DashBoardUtils.CATEGORY, node.getCategory());
		if (node.getUpdatemap().contains(DashBoardUtils.MODELNUMBER))
			update.put(DashBoardUtils.MODELNUMBER, node.getModelNumber());
		if (node.getUpdatemap().contains(DashBoardUtils.MANUFACTURER))
			update.put(DashBoardUtils.MANUFACTURER, node.getManufacturer());
		if (node.getUpdatemap().contains(DashBoardUtils.DESCRIPTION))
			update.put(DashBoardUtils.DESCRIPTION, node.getDescription());
		
		updateNode(DashBoardUtils.SI_REQU_NAME, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),node.getServiceToDel(),node.getServiceToAdd());
		node.clear();
		
	}

	public void updateMediaGatewayNode(MediaGatewayNode node) {
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
		updateNode(DashBoardUtils.TN_REQU_NAME, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),bck);
		node.clear();
		
		RequisitionNode mediagateway = getMediaGateway();
		if (mediagateway == null) {
			return;
		}
		for (String ipaddr : node.getInterfToDel()) 
			m_onmsDao.deleteRequisitionInterface(DashBoardUtils.SI_REQU_NAME, mediagateway.getForeignId(), ipaddr);
		for (String ipaddr : node.getInterfToAdd()) {
			RequisitionInterface mgiface = new RequisitionInterface();
			mgiface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			mgiface.setIpAddr(ipaddr);
			mgiface.putMonitoredService(new RequisitionMonitoredService("PattonSIPCalls"));
			mgiface.setDescr(node.getDescr());
			m_onmsDao.addRequisitionInterface(DashBoardUtils.SIVN_REQU_NAME, mediagateway.getForeignId(),mgiface);
		}		
	}

	public void addTNNode(TrentinoNetworkNode node) throws SQLException {
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
		m_onmsDao.addRequisitionNode(DashBoardUtils.TN_REQU_NAME, requisitionNode);
		logger.info("Adding policy for interface: " + node.getPrimary());
		m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(node.getPrimary()));
		
		m_foreignIdNodeLabelMap.put(node.getForeignId(), node.getNodeLabel());
		m_nodeLabelForeignIdMap.put(node.getNodeLabel(), node.getForeignId());
		m_primaryipcollection.add(node.getPrimary());
		node.clear();
			
	}

	public void updateTNNode(TrentinoNetworkNode node) {
		Map<String, String> update = new HashMap<String, String>();
		
		if (node.getUpdatemap().contains(DashBoardUtils.SNMP_PROFILE)
				&& node.getPrimary() != null)
			update.put(DashBoardUtils.SNMP_PROFILE, node.getSnmpProfile());
		if (node.getUpdatemap().contains(DashBoardUtils.PARENT)) {
			logger.info("Updating parent: " + node.getParent());
			update.put(DashBoardUtils.PARENT,
					m_nodeLabelForeignIdMap.get(node.getParent()));
		}
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
		updateNode(DashBoardUtils.TN_REQU_NAME, node.getForeignId(), node.getPrimary(),
				node.getDescr(), update, node.getInterfToDel(),
				node.getInterfToAdd(), node.getCategoriesToDel(),
				node.getCategoriesToAdd(),bck);
		node.clear();
	}

	public boolean updateNonFastNode(RequisitionNode rnode,Set<String> ipaddresses, String primary) {
		List<String> iptoAdd = new ArrayList<String>();
		List<String> ipToDel = new ArrayList<String>();
		for (String ip : ipaddresses) {
			if (rnode.getInterface(ip) == null)
				iptoAdd.add(ip);
		}
		for (RequisitionInterface riface: rnode.getInterfaces()) {
			if (riface.getDescr().contains("FAST") && !ipaddresses.contains(riface.getIpAddr()))
				ipToDel.add(riface.getIpAddr());
		}
		
		if (iptoAdd.isEmpty() && ipToDel.isEmpty())
			return false;
		updateNode(DashBoardUtils.TN_REQU_NAME,rnode.getForeignId(),primary,"provided by FAST",
				new HashMap<String, String>(),ipToDel,iptoAdd,new ArrayList<String>(),new ArrayList<String>(),null);
		return true;
	}
	
	public boolean updateFastNode(String nodelabel, FastServiceLink reflink, RequisitionNode rnode, FastServiceDevice refdevice, Vrf vrf, BackupProfile bck,Set<String> ipaddresses ) {
		Map<String,String> update = new HashMap<String, String>();
		if (!rnode.getNodeLabel().equals(nodelabel))
			update.put(DashBoardUtils.LABEL, nodelabel);

		if (refdevice.getCity() != null && !refdevice.getCity().equals(rnode.getCity()))
			update.put(DashBoardUtils.CITY, refdevice.getCity());
		else if (refdevice.getCity() == null && rnode.getCity() != null)
			update.put(DashBoardUtils.CITY, "");

		StringBuffer address1 = new StringBuffer();
		if (refdevice.getAddressDescr() != null)
			address1.append(refdevice.getAddressDescr());
		if (refdevice.getAddressName() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(refdevice.getAddressName());
		}
		if (refdevice.getAddressNumber() != null) {
			if (address1.length() > 0)
				address1.append(" ");
			address1.append(refdevice.getAddressNumber());
		}

		if (address1.length() > 0 && rnode.getAsset("address1") == null)
			update.put(DashBoardUtils.ADDRESS1, address1.toString());
		else if (address1.length() > 0 && !address1.toString().equals(rnode.getAsset("address1").getValue()))
			update.put(DashBoardUtils.ADDRESS1, address1.toString());
		else if (address1.length() == 0 && rnode.getAsset("address1") != null && rnode.getAsset("address1").getValue() != null && !"".equals(rnode.getAsset("address1").getValue()))
			update.put(DashBoardUtils.ADDRESS1, "");
		
		if (update.containsKey(DashBoardUtils.CITY) || update.containsKey(DashBoardUtils.ADDRESS1))
			update.put(DashBoardUtils.DESCRIPTION, refdevice.getCity() + " - " + address1.toString());
		
		if (reflink.getDeliveryCode() != null && rnode.getAsset("circuitId") == null) {
			update.put(DashBoardUtils.CIRCUITID, reflink.getDeliveryCode());
			update.put(DashBoardUtils.BUILDING_SCALAR, reflink.getDeliveryCode());
		} else if (reflink.getDeliveryCode() != null &&  !reflink.getDeliveryCode().equals(rnode.getAsset("circuitId").getValue())) {
			update.put(DashBoardUtils.CIRCUITID, reflink.getDeliveryCode());
			update.put(DashBoardUtils.BUILDING_SCALAR, reflink.getDeliveryCode());
		} else if (reflink.getDeliveryCode() == null && rnode.getAsset("circuitId") != null && rnode.getAsset("circuitId").getValue() != null && !"".equals(rnode.getAsset("circuitId").getValue()) ) {
			update.put(DashBoardUtils.CIRCUITID, "");
			update.put(DashBoardUtils.BUILDING_SCALAR, "");
		}					
		if (refdevice.getIstat() != null && reflink.getSiteCode() !=  null && rnode.getAsset("building") == null)
			update.put(DashBoardUtils.BUILDING, refdevice.getIstat()+"-"+reflink.getSiteCode());
		else if (refdevice.getIstat() != null && reflink.getSiteCode() !=  null 
				&& !rnode.getAsset("building").getValue().equals(refdevice.getIstat()+"-"+reflink.getSiteCode()))
			update.put(DashBoardUtils.BUILDING, refdevice.getIstat()+"-"+reflink.getSiteCode());
		else if ((refdevice.getIstat() == null || reflink.getSiteCode() ==  null) && rnode.getAsset("building") != null)
			update.put(DashBoardUtils.BUILDING, "");
		
		if (bck != null) 
			update.put(DashBoardUtils.BACKUP_PROFILE, bck.getName());

		List<String> categorytoAdd = new ArrayList<String>();
		List<String> categoryToDel = new ArrayList<String>();
		List<String> all = new ArrayList<String>();
		
		if (rnode.getCategory(vrf.getNetworklevel()) == null)
			categorytoAdd.add(vrf.getNetworklevel());
		if (rnode.getCategory(vrf.getName()) == null)
			categorytoAdd.add(vrf.getName());
		String notiyCategory = vrf.getNotifylevel();
		if (refdevice.getNotifyCategory() != null && !refdevice.getNotifyCategory().equals(DashBoardUtils.m_fast_default_notify))
			notiyCategory = refdevice.getNotifyCategory();
		if (rnode.getCategory(notiyCategory) == null)
			categorytoAdd.add(notiyCategory);
		if (rnode.getCategory(vrf.getThresholdlevel()) == null)
			categorytoAdd.add(vrf.getThresholdlevel());
		
		all.add(vrf.getNetworklevel());
		all.add(vrf.getName());
		all.add(notiyCategory);
		all.add(vrf.getThresholdlevel());
		
		for (RequisitionCategory category: rnode.getCategories()) {
			if (!all.contains(category.getName()))
				categoryToDel.add(category.getName());
		}
		
		List<String> iptoAdd = new ArrayList<String>();
		List<String> ipToDel = new ArrayList<String>();
		for (String ip : ipaddresses) {
			if (rnode.getInterface(ip) == null)
				iptoAdd.add(ip);
		}
		for (RequisitionInterface riface: rnode.getInterfaces()) {
			if (!ipaddresses.contains(riface.getIpAddr()))
				ipToDel.add(riface.getIpAddr());
		}
		if (update.isEmpty() && categorytoAdd.isEmpty() && categoryToDel.isEmpty() && iptoAdd.isEmpty() && ipToDel.isEmpty())
			return false;
		updateNode(DashBoardUtils.TN_REQU_NAME,rnode.getForeignId(),refdevice.getIpaddr(),"provided by FAST",update,ipToDel,iptoAdd,categoryToDel,categorytoAdd,bck);
		return true;
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

		if (update.containsKey(DashBoardUtils.PARENT)) {
			logger.info("UpdateMap: parent-node-id: " + update.get(DashBoardUtils.PARENT));
			updatemap.add("parent-foreign-id", update.get(DashBoardUtils.PARENT));
		}
		if (update.containsKey(DashBoardUtils.BUILDING_SCALAR))
			updatemap.add("building", update.get(DashBoardUtils.BUILDING_SCALAR));
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
		if (update.containsKey(DashBoardUtils.CIRCUITID))
			assetsToPut.add(new RequisitionAsset("circuitId", update
					.get(DashBoardUtils.CIRCUITID)));
		if (update.containsKey(DashBoardUtils.LEASEEXPIRES))
			assetsToPut.add(new RequisitionAsset("leaseExpires", update
					.get(DashBoardUtils.LEASEEXPIRES)));
		if (update.containsKey(DashBoardUtils.LEASE))
			assetsToPut.add(new RequisitionAsset("lease", update
					.get(DashBoardUtils.LEASE)));
		if (update.containsKey(DashBoardUtils.VENDORPHONE))
			assetsToPut.add(new RequisitionAsset("vendorPhone", update
					.get(DashBoardUtils.VENDORPHONE)));
		if (update.containsKey(DashBoardUtils.VENDOR))
			assetsToPut.add(new RequisitionAsset("vendor", update
					.get(DashBoardUtils.VENDOR)));
		if (update.containsKey(DashBoardUtils.SLOT))
			assetsToPut.add(new RequisitionAsset("slot", update
					.get(DashBoardUtils.SLOT)));
		if (update.containsKey(DashBoardUtils.RACK))
			assetsToPut.add(new RequisitionAsset("rack", update
					.get(DashBoardUtils.RACK)));
		if (update.containsKey(DashBoardUtils.ROOM))
			assetsToPut.add(new RequisitionAsset("room", update
					.get(DashBoardUtils.ROOM)));
		if (update.containsKey(DashBoardUtils.OPERATINGSYSTEM))
			assetsToPut.add(new RequisitionAsset("operatingSystem", update
					.get(DashBoardUtils.OPERATINGSYSTEM)));
		if (update.containsKey(DashBoardUtils.DATEINSTALLED))
			assetsToPut.add(new RequisitionAsset("dateInstalled", update
					.get(DashBoardUtils.DATEINSTALLED)));
		if (update.containsKey(DashBoardUtils.ASSETNUMBER))
			assetsToPut.add(new RequisitionAsset("assetNumber", update
					.get(DashBoardUtils.ASSETNUMBER)));
		if (update.containsKey(DashBoardUtils.SERIALNUMBER))
			assetsToPut.add(new RequisitionAsset("serialNumber", update
					.get(DashBoardUtils.SERIALNUMBER)));
		if (update.containsKey(DashBoardUtils.CATEGORY))
			assetsToPut.add(new RequisitionAsset("category", update
					.get(DashBoardUtils.CATEGORY)));
		if (update.containsKey(DashBoardUtils.MODELNUMBER))
			assetsToPut.add(new RequisitionAsset("modelNumber", update
					.get(DashBoardUtils.MODELNUMBER)));
		if (update.containsKey(DashBoardUtils.MANUFACTURER))
			assetsToPut.add(new RequisitionAsset("manufacturer", update
					.get(DashBoardUtils.MANUFACTURER)));

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

	public void updateNode(String foreignSource, String foreignId,
			String primary, String descr, Map<String, String> update,
			List<String> interfaceToDel,List<String> interfaceToAdd,
			List<String> categoriesToDel, List<String> categoriesToAdd, Map<String,Set<String>> serviceToDel, Map<String,Set<String>> serviceToAdd) {
		
		if (update.containsKey(DashBoardUtils.SNMP_PROFILE) && primary != null)
			m_onmsDao.setSnmpInfo(primary, m_service.getSnmpProfileContainer()
					.getSnmpProfile(update.get(DashBoardUtils.SNMP_PROFILE))
					.getSnmpInfo());

		MultivaluedMap<String, String> updatemap = new MultivaluedMapImpl();
		List<RequisitionAsset> assetsToPut = new ArrayList<RequisitionAsset>();

		if (update.containsKey(DashBoardUtils.PARENT)) {
			logger.info("UpdateMap: parent-node-id: " + update.get(DashBoardUtils.PARENT));
			updatemap.add("parent-foreign-id", update.get(DashBoardUtils.PARENT));
		}
		if (update.containsKey(DashBoardUtils.BUILDING_SCALAR))
			updatemap.add("building", update.get(DashBoardUtils.BUILDING_SCALAR));
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
		if (update.containsKey(DashBoardUtils.CIRCUITID))
			assetsToPut.add(new RequisitionAsset("circuitId", update
					.get(DashBoardUtils.CIRCUITID)));
		if (update.containsKey(DashBoardUtils.LEASEEXPIRES))
			assetsToPut.add(new RequisitionAsset("leaseExpires", update
					.get(DashBoardUtils.LEASEEXPIRES)));
		if (update.containsKey(DashBoardUtils.LEASE))
			assetsToPut.add(new RequisitionAsset("lease", update
					.get(DashBoardUtils.LEASE)));
		if (update.containsKey(DashBoardUtils.VENDORPHONE))
			assetsToPut.add(new RequisitionAsset("vendorPhone", update
					.get(DashBoardUtils.VENDORPHONE)));
		if (update.containsKey(DashBoardUtils.VENDOR))
			assetsToPut.add(new RequisitionAsset("vendor", update
					.get(DashBoardUtils.VENDOR)));
		if (update.containsKey(DashBoardUtils.SLOT))
			assetsToPut.add(new RequisitionAsset("slot", update
					.get(DashBoardUtils.SLOT)));
		if (update.containsKey(DashBoardUtils.RACK))
			assetsToPut.add(new RequisitionAsset("rack", update
					.get(DashBoardUtils.RACK)));
		if (update.containsKey(DashBoardUtils.ROOM))
			assetsToPut.add(new RequisitionAsset("room", update
					.get(DashBoardUtils.ROOM)));
		if (update.containsKey(DashBoardUtils.OPERATINGSYSTEM))
			assetsToPut.add(new RequisitionAsset("operatingSystem", update
					.get(DashBoardUtils.OPERATINGSYSTEM)));
		if (update.containsKey(DashBoardUtils.DATEINSTALLED))
			assetsToPut.add(new RequisitionAsset("dateInstalled", update
					.get(DashBoardUtils.DATEINSTALLED)));
		if (update.containsKey(DashBoardUtils.ASSETNUMBER))
			assetsToPut.add(new RequisitionAsset("assetNumber", update
					.get(DashBoardUtils.ASSETNUMBER)));
		if (update.containsKey(DashBoardUtils.SERIALNUMBER))
			assetsToPut.add(new RequisitionAsset("serialNumber", update
					.get(DashBoardUtils.SERIALNUMBER)));
		if (update.containsKey(DashBoardUtils.CATEGORY))
			assetsToPut.add(new RequisitionAsset("category", update
					.get(DashBoardUtils.CATEGORY)));
		if (update.containsKey(DashBoardUtils.MODELNUMBER))
			assetsToPut.add(new RequisitionAsset("modelNumber", update
					.get(DashBoardUtils.MODELNUMBER)));
		if (update.containsKey(DashBoardUtils.MANUFACTURER))
			assetsToPut.add(new RequisitionAsset("manufacturer", update
					.get(DashBoardUtils.MANUFACTURER)));

		if (!updatemap.isEmpty())
			m_onmsDao
					.updateRequisitionNode(foreignSource, foreignId, updatemap);

		for (String ip : interfaceToDel) {
			m_onmsDao.deleteRequisitionInterface(foreignSource, foreignId, ip);
		}

		for (String ip: serviceToDel.keySet()) {
			if (interfaceToDel.contains(ip))
				continue;
			for (String service: serviceToDel.get(ip)) {
				m_onmsDao.deleteRequisitionservice(foreignSource, foreignId, ip, service);
			}
		}
		
		for (String ip : interfaceToAdd) {
			RequisitionInterface iface = new RequisitionInterface();
			iface.setIpAddr(ip);
			iface.setDescr(descr);
			if (primary.equals(ip)) {
				iface.setSnmpPrimary(PrimaryType.PRIMARY);
			} else {
				iface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
			}
			for (String service: serviceToAdd.get(ip)) {
				iface.putMonitoredService(new RequisitionMonitoredService(service));
			}
			m_onmsDao.addRequisitionInterface(foreignSource, foreignId, iface);
		}

		for (String ip: serviceToAdd.keySet()) {
			if (interfaceToAdd.contains(ip))
				continue;
			for (String service: serviceToAdd.get(ip)) {
				m_onmsDao.addRequisitionservice(foreignSource, foreignId, ip, new RequisitionMonitoredService(service));
			}
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
	}
	
	public void addFastNode(String foreignId, FastServiceDevice node, FastServiceLink link, Vrf vrf, Set<String> secondary) {
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
		m_onmsDao.addRequisitionNode(DashBoardUtils.TN_REQU_NAME, requisitionNode);
		logger.info("Adding policy for interface: " + node.getIpaddr());
		m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(node.getIpaddr()));
		for (String ip: secondary) {
			logger.info("Adding policy for interface: " + ip);
			m_onmsDao.addOrReplacePolicy(DashBoardUtils.TN_REQU_NAME, DashBoardUtils.getPolicyWrapper(ip));			
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
