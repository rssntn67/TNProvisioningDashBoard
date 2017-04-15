package org.opennms.vaadin.provision.model;

public class BasicService extends BasicInterface {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private String m_service;

	public BasicService(BasicInterface bi) {
		this.setOnmsprimary(bi.getOnmsprimary());
		this.setIp(bi.getIp());
		this.setDescr(bi.getDescr());
	}

	public BasicInterface getBasicInterface() {
		BasicInterface bi = new BasicInterface();
		bi.setOnmsprimary(getOnmsprimary());
		bi.setIp(getIp());
		bi.setDescr(getDescr());
		return bi;
		
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
