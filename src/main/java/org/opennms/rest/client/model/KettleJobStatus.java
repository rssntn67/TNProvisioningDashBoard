package org.opennms.rest.client.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "jobstatus")
@Entity
public class KettleJobStatus {

    @XmlElement(name="jobname")
	String m_jobname;
    @XmlElement(name="id")
	String m_id;
    @XmlElement(name="status_desc")
	String m_statusDescr;
    @XmlElement(name="error_desc")
	String m_erroDescr;
    @XmlElement(name="logging_string")
	String m_loggingString;
    @XmlElement(name="first_log_line_nr")
	String m_firstLogLineNr;
    @XmlElement(name="last_log_line_nr")
	String m_lastLogLineNr;
	
    KettleJobResult m_result;

    
	public KettleJobStatus() {
	}


	public String getJobname() {
		return m_jobname;
	}


	public void setJobname(String jobname) {
		m_jobname = jobname;
	}


	public String getId() {
		return m_id;
	}


	public void setId(String id) {
		m_id = id;
	}


	public String getStatusDescr() {
		return m_statusDescr;
	}


	public void setStatusDescr(String statusDescr) {
		m_statusDescr = statusDescr;
	}


	public String getErroDescr() {
		return m_erroDescr;
	}


	public void setErroDescr(String erroDescr) {
		m_erroDescr = erroDescr;
	}


	public String getLoggingString() {
		return m_loggingString;
	}


	public void setLoggingString(String loggingString) {
		m_loggingString = loggingString;
	}


	public String getFirstLogLineNr() {
		return m_firstLogLineNr;
	}


	public void setFirstLogLineNr(String firstLogLineNr) {
		m_firstLogLineNr = firstLogLineNr;
	}


	public String getLastLogLineNr() {
		return m_lastLogLineNr;
	}


	public void setLastLogLineNr(String lastLogLineNr) {
		m_lastLogLineNr = lastLogLineNr;
	}


	public KettleJobResult getResult() {
		return m_result;
	}


	public void setResult(KettleJobResult result) {
		m_result = result;
	}

}
