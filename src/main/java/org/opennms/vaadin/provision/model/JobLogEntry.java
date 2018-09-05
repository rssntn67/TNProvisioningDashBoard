package org.opennms.vaadin.provision.model;

import java.io.Serializable;

public class JobLogEntry implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5344344886863404461L;
	private int m_joblogid;
	private int m_jobid;
	private String m_hostname;
	private String m_ipaddr;
	private String m_orderCode;
	private String m_description;
	private String m_note;

	public int getJoblogid() {
		return m_joblogid;
	}
	
	public void setJoblogid(int joblogid) {
		m_joblogid = joblogid;
	}
	public int getJobid() {
		return m_jobid;
	}
	public void setJobid(int jobid) {
		m_jobid = jobid;
	}
	public String getHostname() {
		return m_hostname;
	}
	public void setHostname(String hostname) {
		m_hostname = hostname;
	}
	public String getIpaddr() {
		return m_ipaddr;
	}
	public void setIpaddr(String ipaddr) {
		m_ipaddr = ipaddr;
	}
	public String getOrderCode() {
		return m_orderCode;
	}
	public void setOrderCode(String orderCode) {
		m_orderCode = orderCode;
	}
	public String getDescription() {
		return m_description;
	}
	public void setDescription(String description) {
		m_description = description;
	}
	public String getNote() {
		return m_note;
	}
	public void setNote(String note) {
		m_note = note;
	}

	
}
