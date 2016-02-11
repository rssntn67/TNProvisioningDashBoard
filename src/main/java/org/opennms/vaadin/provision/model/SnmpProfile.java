package org.opennms.vaadin.provision.model;

import org.opennms.web.svclayer.model.SnmpInfo;

import com.vaadin.data.Property;

public class SnmpProfile {
	String name;
	String community;
	String version;
	String timeout;
	SnmpInfo info;
	
	public SnmpProfile() {
		
	}

	public SnmpProfile(Property<String> name,Property<String> community, Property<String> version, Property<String> timeout) {
		super();
		this.name=name.getValue();
		this.community = community.getValue();
		this.version = version.getValue();
		this.timeout = timeout.getValue();
		info = new SnmpInfo();
		info.setReadCommunity(this.community);
		info.setVersion(this.version);
		info.setTimeout(Integer.parseInt(timeout.getValue()));
		info.setPort(161);
		info.setRetries(1);
	}

	public String getName() {
		return name;
	}
	public String getCommunity() {
		return community;
	}

	public String getVersion() {
		return version;
	}

	public String getTimeout() {
		return timeout;
	}		
	
	public SnmpInfo getSnmpInfo() {
		return info;
	}

	public SnmpInfo getInfo() {
		return info;
	}

	public void setInfo(SnmpInfo info) {
		this.info = info;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
}

