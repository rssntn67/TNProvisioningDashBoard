package org.opennms.vaadin.provision.model;

import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.vaadin.data.Property;

public class BackupProfile {
	final String name;
	final String username;
	final String password;
	final String enable;
	final String connection;
	final String autoenable;

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEnable() {
		return enable;
	}

	public String getConnection() {
		return connection;
	}

	public String getAutoenable() {
		return autoenable;
	}

	public BackupProfile(Property<String> name,Property<String> username,Property<String> password,Property<String> enable,Property<String> connection,Property<String> autoenable) {
		this.name=name.getValue();
		this.username = username.getValue();
		this.password = password.getValue();
		this.enable   = enable.getValue();
		this.connection = connection.getValue();
		if (autoenable != null && autoenable.getValue() != null)
			this.autoenable = autoenable.getValue();
		else
			this.autoenable = "";
		
	}
	
	public RequisitionNode getRequisitionAssets() {
		RequisitionNode template = new RequisitionNode();
		template.putAsset(new RequisitionAsset("username", getUsername()));
		template.putAsset(new RequisitionAsset("password", getPassword()));
		template.putAsset(new RequisitionAsset("enable",getEnable()));
		template.putAsset(new RequisitionAsset("connection", getConnection()));
		template.putAsset(new RequisitionAsset("autoenable", getAutoenable()));
		return template;
	}
}
