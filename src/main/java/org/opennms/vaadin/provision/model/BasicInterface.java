package org.opennms.vaadin.provision.model;

import java.io.Serializable;

public class BasicInterface implements Serializable {
		
	public enum OnmsPrimary {
		N,
		P,
		S;
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3824402168422477329L;
	
	private String m_ip;
	private String m_descr;
	private OnmsPrimary m_onmsprimary;
	
	public String getIp() {
		return m_ip;
	}
	public void setIp(String ip) {
		m_ip = ip;
	}
	public String getDescr() {
		return m_descr;
	}
	public void setDescr(String descr) {
		m_descr = descr;
	}
	public OnmsPrimary getOnmsprimary() {
		return m_onmsprimary;
	}
	public void setOnmsprimary(OnmsPrimary onmsprimary) {
		m_onmsprimary = onmsprimary;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_ip == null) ? 0 : m_ip.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicInterface other = (BasicInterface) obj;
		if (m_ip == null) {
			if (other.m_ip != null)
				return false;
		} else if (!m_ip.equals(other.m_ip))
			return false;
		return true;
	}	
	
	@Override
	public String toString() {
		return m_ip+"/"+m_descr+"/"+m_onmsprimary;
	}

}
