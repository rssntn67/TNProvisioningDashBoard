package org.opennms.rest.client;

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

    public int count();

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
    
    public void add(Requisition requisition);
    
    public void addOrReplace(String name, RequisitionNode rnode);
    
    public void addOrReplace(String name, String foreignid, RequisitionInterface rinterface);
    
    public void addOrReplace(String name, String foreignid, String inet, RequisitionMonitoredService rservice);
    
    public void addOrReplace(String name, String foreignid, RequisitionCategory rcategory);
    
    public void addOrReplace(String name, String foreignid, RequisitionAsset rasset);
    
    public void sync(String name);
    
    public void syncWithOutScanning(String name);

    public void update(Requisition requisition);
    
    public void update(String name, RequisitionNode rnode);
    
    public void update(String name, String foreignid, RequisitionInterface rinterface);
    
    public void deleteRequisition(String name);
    
    public void deleteDeployedRequisition(String name);
    
    public void deleteRequisitionNode(String name, String foreignid);
    
    public void deleteRequisitionInterface(String name, String foreignid, String inet);
    
    public void deleteRequisitionService(String name, String foreignid, String inet,String service);
    
    public void deleteRequisitionCategory(String name, String foreignid, String category);
    
    public void deleteRequisitionAsset(String name, String foreignid, String assetfieldname);
       
}
