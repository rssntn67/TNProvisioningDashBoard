package org.opennms.rest.client.snmpinfo;

import org.opennms.rest.client.RestFilterService;

public interface SnmpInfoService extends RestFilterService{


    public SnmpInfo get(String ip);
    
    public void set(String ip, SnmpInfo snmpInfo);

}
