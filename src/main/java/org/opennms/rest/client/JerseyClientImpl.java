package org.opennms.rest.client;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class JerseyClientImpl {

	private static final Logger logger = Logger.getLogger(JerseyClientImpl.class.getName());

    Client m_client;

    DefaultApacheHttpClientConfig m_config;
    
    String m_url; 
    WebResource m_webResource;
    
    public JerseyClientImpl(String url, String username, String password) {
    	
        m_config = new DefaultApacheHttpClientConfig();
        m_config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,Boolean.TRUE);
        m_config.getProperties().put(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION, Boolean.TRUE);
        m_config.getProperties().put(ApacheHttpClientConfig.FEATURE_DISABLE_XML_SECURITY, Boolean.TRUE);
        m_config.getState().setCredentials(null, null,-1, username, password);
        m_client = ApacheHttpClient.create(m_config);
        m_url=url;
        m_webResource = m_client.resource(url);
    }
    
    public void destroy() {
    	m_client.destroy();
    }

    public <T> T get(Class<T> clazz,String relativePath, MultivaluedMap<String, String> queryParams) {
    	try {
    		return m_webResource.path(relativePath).queryParams(queryParams).header("Accept", "application/xml").accept(MediaType.APPLICATION_XML_TYPE).get(new GenericType<T>(clazz));
    	} catch (UniformInterfaceException uie) {
    		logger.warning("GET: + "+ relativePath + "error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("GET: + "+ relativePath + "error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }

    public <T> T get(Class<T> clazz,String relativePath) {
    	try {
    		return m_webResource.path(relativePath).accept(MediaType.APPLICATION_XML_TYPE).get(clazz);
    	} catch (UniformInterfaceException uie) {
    		logger.warning("GET: + "+ relativePath + "error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("GET: + "+ relativePath + "error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }
    
    public String getXml(String relativePath) {
    	try {
    		return m_webResource.path(relativePath).accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
    	} catch (UniformInterfaceException uie) {
    		logger.warning("GET: + "+ relativePath + "error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("GET: + "+ relativePath + "error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }
    
    public String getXml(String relativePath,MultivaluedMap<String, String> queryParams) {
    	try {
    		return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.APPLICATION_XML_TYPE).get(String.class);
    	} catch (UniformInterfaceException uie) {
    		logger.warning("GET: + "+ relativePath + "error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("GET: + "+ relativePath + "error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }

    public String get(String relativePath) {
    	try {
    		return m_webResource.path(relativePath).accept(MediaType.TEXT_PLAIN).get(String.class);
    	} catch (UniformInterfaceException uie) {
    		logger.warning("GET: + "+ relativePath + "error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("GET: + "+ relativePath + "error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }
    
    public String get(String relativePath,MultivaluedMap<String, String> queryParams) {
    	try{
    		return m_webResource.path(relativePath).queryParams(queryParams).accept(MediaType.TEXT_PLAIN).get(String.class);
    	} catch (UniformInterfaceException uie) {
    		logger.warning("GET: + "+ relativePath + " error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("GET: + "+ relativePath + " error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }

    public void post(Object o, String relativePath) {
    	try {
    		ClientResponse cr = m_webResource.path(relativePath).type(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class,o);
    		logger.info("POST: + "+ relativePath + " response: " + cr.getStatusInfo());
    	} catch (UniformInterfaceException uie) {
    		logger.warning("POST: + "+ relativePath + " error: " + uie.getLocalizedMessage());
    		throw uie;
    	} catch (ClientHandlerException che) {
    		logger.warning("POST: + "+ relativePath + " error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }
    
    public void put(MultivaluedMap<String,String> mvm, String relativePath) {
       	try {
       		ClientResponse cr = m_webResource.queryParams(mvm).path(relativePath).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).put(ClientResponse.class);
    		logger.info("PUT: + "+ relativePath + " response: " + cr.getStatusInfo());
    	} catch (UniformInterfaceException uie) {
    		logger.warning("PUT: + "+ relativePath + " error: " + uie.getLocalizedMessage());
    		throw uie;
    	} catch (ClientHandlerException che) {
    		logger.warning("PUT: + "+ relativePath + " error: " + che.getLocalizedMessage());
    		throw che;
    	}
    }
    
    public void delete(String relativePath) {
    	try {
       		ClientResponse cr = m_webResource.path(relativePath).delete(ClientResponse.class);
    		logger.info("DELETE: + "+ relativePath + " response: " + cr.getStatusInfo());
    	} catch (UniformInterfaceException uie) {
    		logger.warning("DELETE: + "+ relativePath + " error: " + uie.getLocalizedMessage());
    		throw uie;
     	} catch (ClientHandlerException che) {
    		logger.warning("DELETE: + "+ relativePath + " error: " + che.getLocalizedMessage());
    		throw che;
    	}

    }

}
