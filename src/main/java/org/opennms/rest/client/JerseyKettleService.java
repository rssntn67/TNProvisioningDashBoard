package org.opennms.rest.client;


import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.rest.client.model.KettleJobStatus;
import org.opennms.rest.client.model.KettleRunJob;

import java.io.StringReader;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class JerseyKettleService implements KettleService {

    private JerseyClientImpl m_jerseyClient;
    
    public JerseyClientImpl getJerseyClient() {
        return m_jerseyClient;
    }

    public void setJerseyClient(JerseyClientImpl jerseyClient) {
        m_jerseyClient = jerseyClient;
    }
    
    public KettleRunJob runJob() {
    	MultivaluedMap<String, String> query = new MultivaluedMapImpl();
    	query.add("job", "/staging/fast/staging_fast");
    	try {
    		JAXBContext jaxbContext = JAXBContext.newInstance(KettleRunJob.class);
    		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    		StringReader reader = new StringReader(m_jerseyClient.get("runJob/", query));
    		KettleRunJob runjob = (KettleRunJob) unmarshaller.unmarshal(reader);
    		return runjob;
		} catch (JAXBException e) {
			e.printStackTrace();
		}    	
    	return null;
    }
    
    public KettleJobStatus jobStatus(KettleRunJob job) {
    	MultivaluedMap<String, String> query = new MultivaluedMapImpl();
    	query.add("name", "staging_fast");
    	query.add("id", job.getId());
    	query.add("xml", "Y");
    	return m_jerseyClient.get(KettleJobStatus.class,"jobStatus/",query);
    }

}
