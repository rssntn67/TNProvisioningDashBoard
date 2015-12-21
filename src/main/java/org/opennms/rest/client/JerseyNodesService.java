package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCategoryCollection;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterfaceList;


public class JerseyNodesService extends JerseyAbstractService implements NodesService {

    private final static String NODES_PATH = "nodes";
    private final static String ASSET_PATH = "/assetRecord";
    private final static String IP_INTERFACES_PATH = "/ipinterfaces";
    private final static String SNMP_INTERFACES_PATH = "/snmpinterfaces";
    private final static String CATEGORIES_PATH = "/categories";
    private final static String SERVICES_PATH = "/services";

	private String buildNodePath(Integer id) {
		return NODES_PATH+"/"+id;
	}

	private String buildIpInterfacePath(Integer id) {
		return buildNodePath(id) + IP_INTERFACES_PATH;
	}

	private String buildIpInterfacePath(Integer id, String ipaddress) {
		return buildIpInterfacePath(id) + "/" + ipaddress;
	}

	private String buildServicePath(Integer id, String ipaddress) {
		return buildIpInterfacePath(id,ipaddress) + SERVICES_PATH;
	}

	private String buildServicePath(Integer id, String ipaddress,String service) {
		return buildServicePath(id,ipaddress) + "/" + service;
	}

	private String buildSnmpInterfacePath(Integer id) {
		return buildNodePath(id) + SNMP_INTERFACES_PATH;
	}

	private String buildSnmpInterfacePath(Integer id, Integer ifindex) {
		return buildSnmpInterfacePath(id) + "/" + ifindex;
	}

	private String buildAssetRecordPath(Integer id) {
		return buildNodePath(id) + ASSET_PATH;
	}

	private String buildCategoryPath(Integer id) {
		return buildNodePath(id) + CATEGORIES_PATH;
	}

	private String buildCategoryPath(Integer id, String categoryName) {
		return buildCategoryPath(id) + "/" + categoryName;
	}


    private JerseyClientImpl m_jerseyClient;
        
    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }

	@Override
    public OnmsNodeList getAll() {
    	MultivaluedMap<String, String> queryParams = setLimit(0);
        return getJerseyClient().get(OnmsNodeList.class, NODES_PATH,queryParams);                
    }
 
	@Override
    public OnmsNodeList find(MultivaluedMap<String, String> queryParams) {
        return getJerseyClient().get(OnmsNodeList.class, NODES_PATH,queryParams);                
    }

	@Override
    public OnmsNode get(Integer id) {
        return getJerseyClient().get(OnmsNode.class, buildNodePath(id));
    }
 

	@Override
	public OnmsNodeList getWithDefaultsQueryParams() {
        return getJerseyClient().get(OnmsNodeList.class, NODES_PATH);                
	}

	@Override
	public OnmsIpInterfaceList getIpInterfaces(Integer id) {
		return getJerseyClient().get(OnmsIpInterfaceList.class, buildIpInterfacePath(id));
	}

	@Override
	public OnmsIpInterface getIpInterface(Integer id, String ipaddress) {
		return getJerseyClient().get(OnmsIpInterface.class, buildIpInterfacePath(id,ipaddress));
	}

	@Override
	public OnmsSnmpInterfaceList getSnmpInterfaces(Integer id) {
		return getJerseyClient().get(OnmsSnmpInterfaceList.class, buildSnmpInterfacePath(id));
	}

	@Override
	public OnmsSnmpInterface getSnmpInterface(Integer id, Integer ifindex) {
		return getJerseyClient().get(OnmsSnmpInterface.class, buildSnmpInterfacePath(id,ifindex));
	}

	@Override
	public OnmsAssetRecord getAssetRecord(Integer id) {
		return getJerseyClient().get(OnmsAssetRecord.class, buildAssetRecordPath(id));
	}

	@Override
	public OnmsCategoryCollection getCategories(Integer id) {
		return getJerseyClient().get(OnmsCategoryCollection.class, buildCategoryPath(id));
	}

	@Override
	public OnmsCategory getCategory(Integer id, String category) {
		return getJerseyClient().get(OnmsCategory.class, buildCategoryPath(id,category));
	}

	@Override
	public OnmsMonitoredServiceList getMonitoredServices(Integer id,
			String ipaddress) {
		return getJerseyClient().get(OnmsMonitoredServiceList.class, buildServicePath(id, ipaddress));
	}

	@Override
	public OnmsMonitoredService getMonitoredService(Integer id,
			String ipaddress, String service) {
		return getJerseyClient().get(OnmsMonitoredService.class, buildServicePath(id, ipaddress,service));
	}
}
