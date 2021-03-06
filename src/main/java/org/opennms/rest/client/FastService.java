package org.opennms.rest.client;

import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;
import org.opennms.rest.client.model.FastAssetAttributes;
import org.opennms.rest.client.model.FastOrder;

public interface FastService {
    
    FastAsset[] getAssets();
    FastAsset getAssetById(Long id);
    FastAsset[] getAssetsWithAttributes();
    FastAssetAttributes getAssetAttributesById(Long id);
    FastAsset[] getAssetsByMeta(Meta meta);
    FastAsset getAssetWithAttributesById(Long id);
    FastOrder[] getOrders(); 
    FastOrder getOrderById(Long id);
}
