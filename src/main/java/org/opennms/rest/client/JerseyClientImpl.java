package org.opennms.rest.client;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class JerseyClientImpl {

    Client m_client;

    DefaultApacheHttpClientConfig m_config;
    
    WebResource m_webResource;
    
    public JerseyClientImpl(String url, String username, String password) {
    	
        m_config = new DefaultApacheHttpClientConfig();
        m_config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,Boolean.TRUE);
        m_config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
        m_config.getState().setCredentials(null, null,-1, username, password);
        m_client = ApacheHttpClient.create(m_config);
        m_webResource = m_client.resource(url);
    }
    
    public <T> T get(Class<T> clazz,String relativePath, MultivaluedMap<String, String> queryParams) {
        return m_webResource.path(relativePath).queryParams(queryParams).header("Accept", "application/xml").accept(MediaType.APPLICATION_XML_TYPE).get(new GenericType<T>(clazz));
    }

    public <T> T get(Class<T> clazz,String relativePath) {
        return m_webResource.path(relativePath).accept(MediaType.APPLICATION_XML_TYPE).get(clazz);
    }

    public String getXml(String relativePath) {
        return m_webResource.path(relativePath).accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
    }
    
    public String getXml(String relativePath,MultivaluedMap<String, String> queryParams) {
        return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
    }

    public String get(String relativePath) {
    	return m_webResource.path(relativePath).accept(MediaType.TEXT_PLAIN).get(String.class);
    }
    
    public String get(String relativePath,MultivaluedMap<String, String> queryParams) {
    	return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(String.class);
    }

    public <T> T post(Class<T> clazz,Object o, String relativePath) {
    	return m_webResource.path(relativePath).type(MediaType.APPLICATION_XML_TYPE).post(new GenericType<T>(clazz),o);
    }
    
    public <T> T put(Class<T> clazz,Object o, String relativePath) {
    	return m_webResource.path(relativePath).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).put(new GenericType<T>(clazz),o);
    }
    
    public void delete(String relativePath) {
    	m_webResource.path(relativePath).delete();
    }

}
