package org.opennms.rest.client;

public interface SnmpInfoService extends RestFilterService{


    public SnmpInfo get(String ip);
    
    public void set(String ip, SnmpInfo snmpInfo);

}