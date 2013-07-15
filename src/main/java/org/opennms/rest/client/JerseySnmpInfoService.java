package org.opennms.rest.client;

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
		getJerseyClient().put(SnmpInfo.class, snmpInfo, getPath(ip));
	}


}
