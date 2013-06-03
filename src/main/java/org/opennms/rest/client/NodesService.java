package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;

public interface NodesService extends RestFilterService{


    public OnmsNodeList getAll();

    public OnmsNodeList getWithDefaultsQueryParams();

    public OnmsNodeList find(MultivaluedMap<String, String> queryParams);

    public OnmsNode get(Integer id);
    
    

}
