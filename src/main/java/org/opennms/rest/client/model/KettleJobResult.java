package org.opennms.rest.client.model;

import java.util.List;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
@Entity
public class KettleJobResult {

    @XmlElement(name="lines_input")
	String m_linesInput;
    @XmlElement(name="lines_output")
	String m_linesOutput;
    @XmlElement(name="lines_read")
	String m_linesRead;
    @XmlElement(name="lines_written")
	String m_linesWritten;
    @XmlElement(name="lines_updated")
	String m_linesUpdated;
    @XmlElement(name="lines_rejected")
	String m_linesRejected;
    @XmlElement(name="lines_deleted")
	String m_linesDeleted;
    @XmlElement(name="nr_errors")
	String m_nrErrors;
    @XmlElement(name="nr_files_retrieved")
	String m_nr_files_retrieved;
    @XmlElement(name="entry_nr")
	String m_entryNr;
    @XmlElement(name="result")
	String m_result;
    @XmlElement(name="exit_status")
	String m_exitStatus;
    @XmlElement(name="is_stopped")
	String m_isStopped;
    @XmlElement(name="log_channel_id")
	String m_logChannelId;
    @XmlElement(name="log_text")
	String m_logText;
    @XmlElement(name="result-file")
	List<KettleJobResultFile> m_resultFile;
    @XmlElement(name="result-rows")
	String m_resultRows;
     
	public KettleJobResult() {
	}

	public String getLinesInput() {
		return m_linesInput;
	}

	public void setLinesInput(String linesInput) {
		m_linesInput = linesInput;
	}

	public String getLinesOutput() {
		return m_linesOutput;
	}

	public void setLinesOutput(String linesOutput) {
		m_linesOutput = linesOutput;
	}

	public String getLinesRead() {
		return m_linesRead;
	}

	public void setLinesRead(String linesRead) {
		m_linesRead = linesRead;
	}

	public String getLinesWritten() {
		return m_linesWritten;
	}

	public void setLinesWritten(String linesWritten) {
		m_linesWritten = linesWritten;
	}

	public String getLinesUpdated() {
		return m_linesUpdated;
	}

	public void setLinesUpdated(String linesUpdated) {
		m_linesUpdated = linesUpdated;
	}

	public String getLinesRejected() {
		return m_linesRejected;
	}

	public void setLinesRejected(String linesRejected) {
		m_linesRejected = linesRejected;
	}

	public String getLinesDeleted() {
		return m_linesDeleted;
	}

	public void setLinesDeleted(String linesDeleted) {
		m_linesDeleted = linesDeleted;
	}

	public String getNrErrors() {
		return m_nrErrors;
	}

	public void setNrErrors(String nrErrors) {
		m_nrErrors = nrErrors;
	}

	public String getNr_files_retrieved() {
		return m_nr_files_retrieved;
	}

	public void setNr_files_retrieved(String nr_files_retrieved) {
		m_nr_files_retrieved = nr_files_retrieved;
	}

	public String getEntryNr() {
		return m_entryNr;
	}

	public void setEntryNr(String entryNr) {
		m_entryNr = entryNr;
	}

	public String getResult() {
		return m_result;
	}

	public void setResult(String result) {
		m_result = result;
	}

	public String getExitStatus() {
		return m_exitStatus;
	}

	public void setExitStatus(String exitStatus) {
		m_exitStatus = exitStatus;
	}

	public String getIsStopped() {
		return m_isStopped;
	}

	public void setIsStopped(String isStopped) {
		m_isStopped = isStopped;
	}

	public String getLogChannelId() {
		return m_logChannelId;
	}

	public void setLogChannelId(String logChannelId) {
		m_logChannelId = logChannelId;
	}

	public String getLogText() {
		return m_logText;
	}

	public void setLogText(String logText) {
		m_logText = logText;
	}

	public List<KettleJobResultFile> getResultFile() {
		return m_resultFile;
	}

	public void setResultFile(List<KettleJobResultFile> resultFile) {
		m_resultFile = resultFile;
	}

	public String getResultRows() {
		return m_resultRows;
	}

	public void setResultRows(String resultRows) {
		m_resultRows = resultRows;
	}

	

}
