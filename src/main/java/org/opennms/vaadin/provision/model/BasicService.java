package org.opennms.vaadin.provision.model;

import java.io.Serializable;

public class BasicService implements Serializable {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private String m_service;
	private BasicInterface m_basicInterface;

	public BasicService(BasicInterface bi) {
		m_basicInterface = bi;
	}

	public BasicInterface getInterface() {
		return m_basicInterface;
		
	}
	public String getService() {
		return m_service;
	}

	public void setService(String service) {
		m_service = service;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((m_service == null) ? 0 : m_service.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicService other = (BasicService) obj;
		if (m_service == null) {
			if (other.m_service != null)
				return false;
		} else if (!m_service.equals(other.m_service))
			return false;
		return true;
	}

	
	

}
