package org.opennms.rest.client;


import javax.ws.rs.core.MultivaluedMap;

import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;
import org.opennms.rest.client.model.FastAssetAttributes;
import org.opennms.rest.client.model.FastOrder;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JerseyFastService implements FastService {

    private JerseyClientImpl m_jerseyClient;
    
    private final String ORDERS_PATH="orders/";

    private final String ASSETS_PATH="assets/";

    private final String FILTER_PATH="filter_by_attrs";

    private final String ATTRS_PATH="/attrs";

    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }

    @Override
    public FastAsset[] getAssets() {
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH);
    }

    @Override
    public FastAsset getAssetById(Long id) {
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH+id)[0];
    }

    @Override
    public FastAsset[] getAssetsWithAttributes() {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("load_asset_attrs", "true");
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH,queryParams);
    }

    @Override
    public FastAsset getAssetWithAttributesById(Long id) {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("load_asset_attrs", "true");
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH+id,queryParams)[0];
    }

    @Override
    public FastAssetAttributes getAssetAttributesById(Long id) {
        return m_jerseyClient.getJson(FastAssetAttributes.class, ASSETS_PATH+id+ATTRS_PATH);
    }

    @Override
    public FastAsset[] getAssetsByMeta(Meta meta) {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("meta", meta.name());
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH+FILTER_PATH,queryParams);
    }

    @Override
    public FastOrder[] getOrders() {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("load_assets", "false");
        queryParams.add("load_asset_attrs", "false");
        return m_jerseyClient.getJson(FastOrder[].class, ORDERS_PATH);
    }

    @Override
    public FastOrder getOrderById(Long id) {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("load_assets", "false");
        queryParams.add("load_asset_attrs", "false");
        queryParams.add("sql_filter", "order_id%3D"+id);
        FastOrder[] orders = m_jerseyClient.getJson(FastOrder[].class, ORDERS_PATH,queryParams);
        if (orders.length == 1) {
            return orders[0];
        }
        return null;
    }
    
}
