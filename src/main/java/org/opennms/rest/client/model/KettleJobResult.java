package org.opennms.rest.client.model;

import java.util.List;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result")
@Entity
public class KettleJobResult {

    @XmlElement(name="lines_input")
	String m_lines_input;
    @XmlElement(name="lines_output")
	String m_lines_output;
    @XmlElement(name="lines_read")
	String m_lines_read;
    @XmlElement(name="lines_written")
	String m_lines_written;
    @XmlElement(name="lines_updated")
	String m_lines_updated;
    @XmlElement(name="lines_rejected")
	String m_lines_rejected;
    @XmlElement(name="lines_deleted")
	String m_lines_deleted;
    @XmlElement(name="nr_errors")
	String m_nr_errors;
    @XmlElement(name="nr_files_retrieved")
	String m_nr_files_retrieved;
    @XmlElement(name="entry_nr")
	String m_entry_nr;
    @XmlElement(name="result")
	String m_result;
    @XmlElement(name="exit_status")
	String m_exit_status;
    @XmlElement(name="is_stopped")
	String m_is_stopped;
    @XmlElement(name="log_channel_id")
	String m_log_channel_id;
    @XmlElement(name="log_text")
	String m_log_text;
    @XmlElement(name="result-file")
	List<KettleJobResultFile> result_file;
    @XmlElement(name="result-rows")
	List<KettleJobResultFile> m_result_rows;
     
	public KettleJobResult() {
		// TODO Auto-generated constructor stub
	}

	public String getLines_input() {
		return m_lines_input;
	}

	public void setLines_input(String lines_input) {
		m_lines_input = lines_input;
	}

	public String getLines_output() {
		return m_lines_output;
	}

	public void setLines_output(String lines_output) {
		m_lines_output = lines_output;
	}

	public String getLines_read() {
		return m_lines_read;
	}

	public void setLines_read(String lines_read) {
		m_lines_read = lines_read;
	}

	public String getLines_written() {
		return m_lines_written;
	}

	public void setLines_written(String lines_written) {
		m_lines_written = lines_written;
	}

	public String getLines_updated() {
		return m_lines_updated;
	}

	public void setLines_updated(String lines_updated) {
		m_lines_updated = lines_updated;
	}

	public String getLines_rejected() {
		return m_lines_rejected;
	}

	public void setLines_rejected(String lines_rejected) {
		m_lines_rejected = lines_rejected;
	}

	public String getLines_deleted() {
		return m_lines_deleted;
	}

	public void setLines_deleted(String lines_deleted) {
		m_lines_deleted = lines_deleted;
	}

	public String getNr_errors() {
		return m_nr_errors;
	}

	public void setNr_errors(String nr_errors) {
		m_nr_errors = nr_errors;
	}

	public String getNr_files_retrieved() {
		return m_nr_files_retrieved;
	}

	public void setNr_files_retrieved(String nr_files_retrieved) {
		m_nr_files_retrieved = nr_files_retrieved;
	}

	public String getEntry_nr() {
		return m_entry_nr;
	}

	public void setEntry_nr(String entry_nr) {
		m_entry_nr = entry_nr;
	}

	public String getResult() {
		return m_result;
	}

	public void setResult(String result) {
		m_result = result;
	}

	public String getExit_status() {
		return m_exit_status;
	}

	public void setExit_status(String exit_status) {
		m_exit_status = exit_status;
	}

	public String getIs_stopped() {
		return m_is_stopped;
	}

	public void setIs_stopped(String is_stopped) {
		m_is_stopped = is_stopped;
	}

	public String getLog_channel_id() {
		return m_log_channel_id;
	}

	public void setLog_channel_id(String log_channel_id) {
		m_log_channel_id = log_channel_id;
	}

	public String getLog_text() {
		return m_log_text;
	}

	public void setLog_text(String log_text) {
		m_log_text = log_text;
	}

	public List<KettleJobResultFile> getResult_file() {
		return result_file;
	}

	public void setResult_file(List<KettleJobResultFile> result_file) {
		this.result_file = result_file;
	}

	public List<KettleJobResultFile> getResult_rows() {
		return m_result_rows;
	}

	public void setResult_rows(List<KettleJobResultFile> result_rows) {
		m_result_rows = result_rows;
	}
	

}
