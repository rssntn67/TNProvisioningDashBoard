package org.opennms.rest.client;

import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;
import org.opennms.rest.client.model.FastAssetAttributes;

public interface FastService {
    
    FastAsset[] getAssets();
    
    FastAsset getAssetById(Integer id);

    FastAsset[] getAssetsWithAttributes();
    
    FastAsset getAssetWithAttributesById(Integer id);
    
    FastAssetAttributes getFastAssetAttributesByIt(Integer id);
    
    FastAsset[] getAssetsByMeta(Meta meta);
    

}
