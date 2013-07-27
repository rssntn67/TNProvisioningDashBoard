package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.rest.client.model.OnmsAssetRecord;
import org.opennms.rest.client.model.OnmsCategory;
import org.opennms.rest.client.model.OnmsCategoryCollection;
import org.opennms.rest.client.model.OnmsIpInterface;
import org.opennms.rest.client.model.OnmsIpInterfaceList;
import org.opennms.rest.client.model.OnmsMonitoredService;
import org.opennms.rest.client.model.OnmsMonitoredServiceList;
import org.opennms.rest.client.model.OnmsNode;
import org.opennms.rest.client.model.OnmsNodeList;
import org.opennms.rest.client.model.OnmsSnmpInterface;
import org.opennms.rest.client.model.OnmsSnmpInterfaceList;


public interface NodesService extends RestFilterService{


    public OnmsNodeList getAll();

    public OnmsNodeList getWithDefaultsQueryParams();

    public OnmsNodeList find(MultivaluedMap<String, String> queryParams);

    public OnmsNode get(Integer id);
    
    public OnmsIpInterfaceList getIpInterfaces(Integer id);

    public OnmsIpInterface getIpInterface(Integer id, String ipaddress);

    public OnmsSnmpInterfaceList  getSnmpInterfaces(Integer id);
    
    public OnmsSnmpInterface getSnmpInterface(Integer id,Integer ifindex);

    public OnmsAssetRecord getAssetRecord(Integer id);
    
    public OnmsCategoryCollection getCategories(Integer id);
    
    public OnmsCategory getCategory(Integer id, String category);
    
    public OnmsMonitoredServiceList getMonitoredServices(Integer id, String ipaddress);
    
    public OnmsMonitoredService getMonitoredService(Integer id, String ipaddress,String serviceName);

}
