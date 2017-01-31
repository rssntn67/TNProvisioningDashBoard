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
import org.opennms.rest.client.ProvisionRequisitionService;

import com.sun.jersey.core.util.MultivaluedMapImpl;


public class JerseyProvisionRequisitionService extends JerseyAbstractService implements
		ProvisionRequisitionService {

	private final static String REQUISITIONS_PATH = "requisitions";
	private final static String REQUISITIONS_COUNT_PATH = "requisitions/count";

	private final static String REQUISITIONS_DEPLOYED_PATH = "requisitions/deployed";
	private final static String REQUISITIONS_DEPLOYED_COUNT_PATH = "requisitions/deployed/count";

	private static final String REQUISION_NODE_PATH = "/nodes";
	private static final String REQUISION_INTERFACE_PATH = "/interfaces";
	private static final String REQUISION_SERVICE_PATH = "/services";
	private static final String REQUISION_CATEGORY_PATH = "/categories";
	private static final String REQUISION_ASSET_PATH = "/assets";
	
	private static final String IMPORT_PATH = "/import";
	
	
	private String buildRequisitionPath(String name) {
		return REQUISITIONS_PATH+"/"+name;
	}
	
	private String buildRequisitionDeployedPath(String name) {
		return REQUISITIONS_DEPLOYED_PATH+"/"+name;
	}
	
	private String buildImportPath(String name) {
		return buildRequisitionPath(name)+IMPORT_PATH;
	}

	private String buildNodesPath(String name) {
		return buildRequisitionPath(name)+REQUISION_NODE_PATH;
	}
	
	private String buildNodePath(String name, String foreignid) {
		return buildNodesPath(name)+"/"+foreignid;
	}

	private String buildCategoriesPath(String name, String foreignid) {
		return buildNodePath(name, foreignid)+REQUISION_CATEGORY_PATH;
	}
	
	private String buildCategoryPath(String name, String foreignid, String category) {
		return buildCategoriesPath(name, foreignid)+"/"+category;
	}

	private String buildAssetsPath(String name, String foreignid) {
		return buildNodePath(name, foreignid)+REQUISION_ASSET_PATH;
	}
	
	private String buildAssetPath(String name, String foreignid, String assetfieldname) {
		return buildAssetsPath(name, foreignid)+"/"+assetfieldname;
	}

	private String buildInterfacesPath(String name, String foreignid) {
		return buildNodePath(name, foreignid)+REQUISION_INTERFACE_PATH;
	}
	
	private String buildInterfacePath(String name, String foreignid, String inet) {
		return buildInterfacesPath(name, foreignid)+"/"+inet;
	}
	
	private String buildServicesPath(String name, String foreignid, String inet) {
		return buildInterfacePath(name, foreignid,inet)+REQUISION_SERVICE_PATH;
	}

	private String buildServicePath(String name, String foreignid, String inet, String service) {
		return buildServicesPath(name, foreignid, inet)+"/"+service;
	}

	@Override
	public RequisitionCollection getAll() {
		return getJerseyClient().get(RequisitionCollection.class, REQUISITIONS_PATH);
	}

	@Override
	public Integer count() {
		return Integer.decode(getJerseyClient().get(REQUISITIONS_COUNT_PATH));
		/*
		if (found == null)
			return -1;
		return Integer.getInteger(getJerseyClient().get(REQUISITIONS_COUNT_PATH));
		*/
	}

	@Override
	public RequisitionCollection getAllDeployed() {
		return getJerseyClient().get(RequisitionCollection.class, REQUISITIONS_DEPLOYED_PATH);
	}

	@Override
	public int countDeployed() {
		return Integer.getInteger(getJerseyClient().get(REQUISITIONS_DEPLOYED_COUNT_PATH));
	}

	@Override
	public Requisition get(String name) {
		return getJerseyClient().get(Requisition.class, buildRequisitionPath(name));
	}

	@Override
	public RequisitionNodeCollection getNodes(String name) {
		return getJerseyClient().get(RequisitionNodeCollection.class, buildNodesPath(name));
	}

	@Override
	public RequisitionNode getNode(String foreignid, String name) {
		return getJerseyClient().get(RequisitionNode.class, buildNodePath(name, foreignid));
	}

	@Override
	public RequisitionInterfaceCollection getInterfaces(String foreignid,
			String name) {
		return getJerseyClient().get(RequisitionInterfaceCollection.class, buildInterfacesPath(name, foreignid));
	}

	@Override
	public RequisitionInterface getInterface(String foreignid, String name,
			String inet) {
		return getJerseyClient().get(RequisitionInterface.class, buildInterfacePath(name, foreignid,inet));
	}

	@Override
	public RequisitionMonitoredServiceCollection getServices(String foreignid,
			String name, String inet) {
		return getJerseyClient().get(RequisitionMonitoredServiceCollection.class, buildServicesPath(name, foreignid, inet));
	}

	@Override
	public RequisitionMonitoredService getService(String foreignid,
			String name, String inet, String service) {
		return getJerseyClient().get(RequisitionMonitoredService.class, buildServicePath(name, foreignid, inet, service));
	}

	@Override
	public RequisitionCategoryCollection getCategories(String foreignid,
			String name) {
		return getJerseyClient().get(RequisitionCategoryCollection.class, buildCategoriesPath(name, foreignid));
	}

	@Override
	public RequisitionCategory getCategory(String foreignid, String name,
			String category) {
		return getJerseyClient().get(RequisitionCategory.class, buildCategoryPath(name, foreignid, category));
	}

	@Override
	public RequisitionAssetCollection getAssets(String foreignid, String name) {
		return getJerseyClient().get(RequisitionAssetCollection.class, buildAssetsPath(name, foreignid));
	}

	@Override
	public RequisitionAsset getAsset(String foreignid, String name,
			String assetfieldname) {
		return getJerseyClient().get(RequisitionAsset.class, buildAssetPath(name, foreignid, assetfieldname));
	}

	@Override
	public void add(Requisition requisition) {
		getJerseyClient().post(requisition, REQUISITIONS_PATH);
	}

	@Override
	public void add(String name, RequisitionNode rnode) {
		getJerseyClient().post(rnode, buildNodesPath(name));
	}

	@Override
	public void add(String name, String foreignid, RequisitionInterface rinterface) {
		getJerseyClient().post(rinterface, buildInterfacesPath(name,foreignid));
	}

	@Override
	public void add(String name, String foreignid, String inet, RequisitionMonitoredService rservice) {
		getJerseyClient().post(rservice, buildServicesPath(name,foreignid,inet));
	}

	@Override
	public void add(String name, String foreignid, RequisitionCategory rcategory) {
		getJerseyClient().post(rcategory, buildCategoriesPath(name,foreignid));

	}

	@Override
	public void add(String name, String foreignid, RequisitionAsset rasset) {
		getJerseyClient().post(rasset, buildAssetsPath(name,foreignid));
	}

	@Override
	public void sync(String name) {
		getJerseyClient().put(null, buildImportPath(name));
	}

	@Override
	public void syncRescanExistingFalse(String name) {
		MultivaluedMap<String, String> mvm = new MultivaluedMapImpl();
		mvm.add("rescanExisting", "false");
		getJerseyClient().put(mvm, buildImportPath(name));
	}

	@Override
	public void syncDbOnly(String name) {
		MultivaluedMap<String, String> mvm = new MultivaluedMapImpl();
		mvm.add("rescanExisting", "dbonly");
		getJerseyClient().put(mvm, buildImportPath(name));
	}

	@Override
	public void delete(String name) {
		getJerseyClient().delete(buildRequisitionPath(name));
	}

	@Override
	public void deleteDeployed(String name) {
		getJerseyClient().delete(buildRequisitionDeployedPath(name));
	}

	@Override
	public void deleteNode(String name, String foreignid) {
		getJerseyClient().delete(buildNodePath(name, foreignid));
	}

	@Override
	public void deleteInterface(String name, String foreignid,
			String inet) {
		getJerseyClient().delete(buildInterfacePath(name, foreignid, inet));

	}

	@Override
	public void deleteService(String name, String foreignid,
			String inet, String service) {
		getJerseyClient().delete(buildServicePath(name, foreignid, inet, service));
	}

	@Override
	public void deleteCategory(String name, String foreignid,
			String category) {
		getJerseyClient().delete(buildCategoryPath(name, foreignid, category));
	}

	@Override
	public void deleteAsset(String name, String foreignid,
			RequisitionAsset assetfieldname) {
		getJerseyClient().delete(buildAssetPath(name, foreignid, assetfieldname.getName()));
	}

	@Override
	public void update(String foreignSource, String foreignid,
			MultivaluedMap<String, String> map) {
		getJerseyClient().put(map, buildNodePath(foreignSource, foreignid));
	}
}
