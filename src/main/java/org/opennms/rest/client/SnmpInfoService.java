package org.opennms.rest.client;

import org.opennms.rest.client.snmpinfo.SnmpInfo;

public interface SnmpInfoService extends RestFilterService{


    public SnmpInfo get(String ip);
    
    public void set(String ip, SnmpInfo snmpInfo);

}
