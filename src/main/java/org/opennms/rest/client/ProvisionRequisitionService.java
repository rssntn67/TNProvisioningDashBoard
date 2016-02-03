package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNodeCollection;

public interface ProvisionRequisitionService extends RestFilterService{


    public RequisitionCollection getAll();

    public Integer count();

    public RequisitionCollection getAllDeployed();

    public int countDeployed();

    public Requisition get(String name);    

    public RequisitionNodeCollection getNodes(String name);
    
    public RequisitionNode getNode(String foreignId, String name);
    
    public RequisitionInterfaceCollection getInterfaces(String foreignId, String name);
    
    public RequisitionInterface getInterface(String foreignId, String name, String inet);

    public RequisitionMonitoredServiceCollection getServices(String foreignId, String name, String inet);
    
    public RequisitionMonitoredService getService(String foreignId, String name, String inet, String service);
    
    public RequisitionCategoryCollection getCategories(String foreignId, String name);
    
    public RequisitionCategory getCategory(String foreignId, String name,String category);
    
    public RequisitionAssetCollection getAssets(String foreignId, String name);
    
    public RequisitionAsset getAsset(String foreignId, String name,String assetfieldname);
    
    public Requisition add(Requisition requisition);
    
    public RequisitionNode add(String foreignSource, RequisitionNode rnode);
    
    public RequisitionInterface add(String foreignSource, String foreignid, RequisitionInterface rinterface);
    
    public RequisitionMonitoredService add(String foreignSource, String foreignid, String inet, RequisitionMonitoredService rservice);
    
    public RequisitionCategory add(String foreignSource, String foreignid, RequisitionCategory rcategory);
    
    public RequisitionAsset add(String foreignSource, String foreignid, RequisitionAsset rasset);
    
    public void sync(String foreignSource);
    
    public void syncWithOutScanning(String foreignSource);
    
    public void delete(String foreignSource);
    
    public void deleteDeployed(String foreignSource);
    
    public void deleteNode(String foreignSource, String foreignid);
    
    public void deleteInterface(String foreignSource, String foreignid, String inet);
    
    public void deleteService(String foreignSource, String foreignid, String inet, String service);
    
    public void deleteCategory(String foreignSource, String foreignid, String category);
    
    public void deleteAsset(String foreignSource, String foreignid, RequisitionAsset assetfieldname);
    
    public void update(String foreignSource, String foreignid, MultivaluedMap<String, String> map);
}
