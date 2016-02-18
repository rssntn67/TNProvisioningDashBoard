package org.opennms.vaadin.provision.model;

import java.util.Date;

public class Job {

	public enum JobStatus { RUNNING, FAILED, SUCCESS };
	private Integer m_jobid;
	private String m_username;
	private String m_jobdescr;
	private JobStatus m_jobstatus;
	private Date m_jobstart;
	private Date m_jobend;

	public Integer getJobid() {
		return m_jobid;
	}
	public void setJobid(int jobid) {
		m_jobid = jobid;
	}
	public String getUsername() {
		return m_username;
	}
	public void setUsername(String username) {
		m_username = username;
	}
	public String getJobdescr() {
		return m_jobdescr;
	}
	public void setJobdescr(String jobdescr) {
		m_jobdescr = jobdescr;
	}
	public JobStatus getJobstatus() {
		return m_jobstatus;
	}
	public void setJobstatus(JobStatus jobstatus) {
		m_jobstatus = jobstatus;
	}
	public Date getJobstart() {
		return m_jobstart;
	}
	public void setJobstart(Date jobstart) {
		m_jobstart = jobstart;
	}
	public Date getJobend() {
		return m_jobend;
	}
	public void setJobend(Date jobend) {
		m_jobend = jobend;
	}
	
	
}
