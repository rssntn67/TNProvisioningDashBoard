package org.opennms.vaadin.provision.model;

import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.vaadin.data.Property;

public class BackupProfile extends AbstractProfile{
	private String m_username;
	private String m_password;
	private String m_enable;
	private String m_connection;
	private String m_autoenable;
 
	public String getUsername() {
		return m_username;
	}

	public String getPassword() {
		return m_password;
	}

	public String getEnable() {
		return m_enable;
	}

	public String getConnection() {
		return m_connection;
	}

	public String getAutoenable() {
		return m_autoenable;
	}
	
	public BackupProfile() {
		super();
		
	}

	public BackupProfile(Property<String> name,Property<String> username,Property<String> password,Property<String> enable,Property<String> connection,Property<String> autoenable) {
		super(name);
		this.m_username = username.getValue();
		this.m_password = password.getValue();
		this.m_enable   = enable.getValue();
		this.m_connection = connection.getValue();
		if (autoenable != null && autoenable.getValue() != null)
			this.m_autoenable = autoenable.getValue();
		else
			this.m_autoenable = "";
		
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

	public void setUsername(String username) {
		m_username = username;
	}

	public void setPassword(String password) {
		m_password = password;
	}

	public void setEnable(String enable) {
		m_enable = enable;
	}

	public void setConnection(String connection) {
		m_connection = connection;
	}

	public void setAutoenable(String autoenable) {
		m_autoenable = autoenable;
	}
}
