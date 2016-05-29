package org.opennms.vaadin.provision.model;

import org.opennms.web.svclayer.model.SnmpInfo;

import com.vaadin.data.Property;

public class SnmpProfile {
	String m_name;
	String m_community;
	String m_version;
	String m_timeout;
	String m_retries;
	String m_maxvarsperpdu;
	
	public SnmpProfile() {
		
	}

	public SnmpProfile(Property<String> name,
			Property<String> community, 
			Property<String> version, 
			Property<String> timeout, 
			Property<String> maxvarsperpdu, 
			Property<String> retries) {
		super();
		m_name=name.getValue();
		m_community = community.getValue();
		m_version = version.getValue();
		m_timeout = timeout.getValue();
		m_maxvarsperpdu = maxvarsperpdu.getValue();
		m_retries = retries.getValue();
	}

	public String getName() {
		return m_name;
	}
	public String getCommunity() {
		return m_community;
	}

	public String getVersion() {
		return m_version;
	}

	public String getTimeout() {
		return m_timeout;
	}		
	
	public SnmpInfo getSnmpInfo() {
		SnmpInfo info = new SnmpInfo();
		info.setReadCommunity(m_community);
		info.setVersion(m_version);
		info.setTimeout(Integer.parseInt(m_timeout));
		info.setPort(161);
		info.setRetries(Integer.parseInt(m_retries));
		info.setMaxVarsPerPdu(Integer.parseInt(m_maxvarsperpdu));
		return info;
	}


	public void setName(String name) {
		m_name = name;
	}

	public void setCommunity(String community) {
		m_community = community;
	}

	public void setVersion(String version) {
		m_version = version;
	}

	public void setTimeout(String timeout) {
		m_timeout = timeout;
	}

	public String getMaxvarsperpdu() {
		return m_maxvarsperpdu;
	}

	public void setMaxvarsperpdu(String maxvarsperpdu) {
		m_maxvarsperpdu = maxvarsperpdu;
	}

	public String getRetries() {
		return m_retries;
	}

	public void setRetries(String retries) {
		m_retries = retries;
	}
}

