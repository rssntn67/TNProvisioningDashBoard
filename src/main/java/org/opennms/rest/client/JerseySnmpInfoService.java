package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.annotations.GenerationTime;
import org.opennms.web.svclayer.model.SnmpInfo;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JerseySnmpInfoService extends JerseyAbstractService implements SnmpInfoService {

    private final static String SNMP_REST_PATH = "snmpConfig/";

    private JerseyClientImpl m_jerseyClient;
        
    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }

    private String getPath(String ip) {
    	return SNMP_REST_PATH+ip;
    }

    @Override
	public SnmpInfo get(String ip) {
		return getJerseyClient().get(SnmpInfo.class, getPath(ip));
	}

	@Override
	public void set(String ip, SnmpInfo snmpInfo) {
		getJerseyClient().put(getMap(snmpInfo), getPath(ip));
	}
	
	private MultivaluedMap<String,String> getMap(SnmpInfo snmpinfo) {
		MultivaluedMap< String, String> form = new MultivaluedMapImpl();
		if (snmpinfo.getReadCommunity() != null) {
		    form.add("community", snmpinfo.getReadCommunity());
		}
		boolean version1=false;
		if (snmpinfo.getVersion() != null ) {
			form.add("version", snmpinfo.getVersion());
			version1 = snmpinfo.getVersion().equals("v1");
		}
		if (snmpinfo.getTimeout() != null && snmpinfo.getTimeout() > 0)
			form.add("timeout", Integer.toString(snmpinfo.getTimeout()));
		if (snmpinfo.getPort() != null && snmpinfo.getPort() > 0 ) 
			form.add("port", Integer.toString(snmpinfo.getPort()));
		if (snmpinfo.getRetries() != null && snmpinfo.getRetries() > 0) 
			form.add("retries", Integer.toString(snmpinfo.getRetries()));
                if (!version1 && snmpinfo.getMaxVarsPerPdu() != null && snmpinfo.getMaxVarsPerPdu() > 0) {
                    form.add("maxVarsPerPdu", Integer.toString(snmpinfo.getMaxVarsPerPdu()));
                    
                }
		return form;
	}


}
