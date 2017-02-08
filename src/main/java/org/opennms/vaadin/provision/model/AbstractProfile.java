package org.opennms.vaadin.provision.model;

import com.vaadin.data.Property;

public abstract class AbstractProfile {
	private String m_name;

	public String getName() {
		return m_name;
	}

	public AbstractProfile() {
		
	}

	public AbstractProfile(Property<String> name) {
		m_name=name.getValue();
	}
	
	public void setName(String name) {
		m_name = name;
	}

}
