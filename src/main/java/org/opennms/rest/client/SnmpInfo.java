package org.opennms.rest.client;


import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>SnmpInfo class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@XmlRootElement(name="snmp-info")
public class SnmpInfo {

    private String m_community;
    private String m_version;
    private int m_port;
    private int m_retries;
    private int m_timeout;
    
    /**
     * <p>Constructor for SnmpInfo.</p>
     */
    public SnmpInfo() {
        
    }

    /**
     * <p>getCommunity</p>
     *
     * @return the community
     */
    public String getCommunity() {
        return m_community;
    }

    /**
     * <p>setCommunity</p>
     *
     * @param community the community to set
     */
    public void setCommunity(String community) {
        m_community = community;
    }

    /**
     * <p>getVersion</p>
     *
     * @return the version
     */
    public String getVersion() {
        return m_version;
    }

    /**
     * <p>setVersion</p>
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        m_version = version;
    }

    /**
     * <p>getPort</p>
     *
     * @return the port
     */
    public int getPort() {
        return m_port;
    }

    /**
     * <p>setPort</p>
     *
     * @param port the port to set
     */
    public void setPort(int port) {
        m_port = port;
    }

    /**
     * <p>getRetries</p>
     *
     * @return the retries
     */
    public int getRetries() {
        return m_retries;
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries the retries to set
     */
    public void setRetries(int retries) {
        m_retries = retries;
    }

    /**
     * <p>getTimeout</p>
     *
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * <p>setTimeout</p>
     *
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }
    
    
}

