package org.opennms.vaadin.provision.model;

import org.opennms.web.svclayer.model.SnmpInfo;

import com.vaadin.data.Property;

public class SnmpProfile {
	final String name;
	final String community;
	final String version;
	final Integer timeout;
	SnmpInfo info;
	
	public SnmpProfile(Property<String> name,Property<String> community, Property<String> version, Property<String> timeout) {
		super();
		this.name=name.getValue();
		this.community = community.getValue();
		this.version = version.getValue();
		this.timeout = Integer.parseInt(timeout.getValue());
		info = new SnmpInfo();
		info.setReadCommunity(this.community);
		info.setVersion(this.version);
		info.setTimeout(this.timeout);
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

	public Integer getTimeout() {
		return timeout;
	}		
	
	public SnmpInfo getSnmpInfo() {
		return info;
	}
}

