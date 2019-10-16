package org.opennms.vaadin.provision.dao;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyFastService;
import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;

public class FastApiDao {

	private JerseyClientImpl m_jerseyClient;
    
	private JerseyFastService m_fastService;

	public FastApiDao() {
    	    m_fastService = new JerseyFastService();
	}
	
	public JerseyClientImpl getJerseyClient() {
		return m_jerseyClient;
	}

	public void setJerseyClient(JerseyClientImpl jerseyClient) {
		m_jerseyClient = jerseyClient;
	    m_fastService.setJerseyClient(m_jerseyClient);
	}
	

	public List<FastAsset> getAssetsByMeta(Meta meta) {
	     return new ArrayList<>(Arrays.asList(m_fastService.getAssetsByMeta(meta)));
	}
}
