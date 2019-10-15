package org.opennms.rest.client;


import javax.ws.rs.core.MultivaluedMap;

import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;
import org.opennms.rest.client.model.FastAssetAttributes;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JerseyFastService implements FastService {

    private JerseyClientImpl m_jerseyClient;
    
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
    public FastAsset getAssetById(Integer id) {
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH+id)[0];
    }

    @Override
    public FastAsset[] getAssetsWithAttributes() {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("load_asset_attrs", "true");
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH,queryParams);
    }

    @Override
    public FastAsset getAssetWithAttributesById(Integer id) {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("load_asset_attrs", "true");
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH+id,queryParams)[0];
    }

    @Override
    public FastAssetAttributes getFastAssetAttributesByIt(Integer id) {
        return m_jerseyClient.getJson(FastAssetAttributes.class, ASSETS_PATH+id+ATTRS_PATH);
    }

    @Override
    public FastAsset[] getAssetsByMeta(Meta meta) {
        MultivaluedMap< String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("meta", meta.name());
        return m_jerseyClient.getJson(FastAsset[].class, ASSETS_PATH+FILTER_PATH,queryParams);
    }
    
}
