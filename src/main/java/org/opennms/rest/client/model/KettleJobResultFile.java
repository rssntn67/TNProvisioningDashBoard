package org.opennms.rest.client.model;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "result-file")
@Entity
public class KettleJobResultFile {

    @XmlElement(name="file")
	String m_file;
    @XmlElement(name="parentorigin")
	String m_parentorigin;
    @XmlElement(name="origin")
	String m_origin;
    @XmlElement(name="comment")
	String m_comment;
    @XmlElement(name="timestamp")
	String m_timestamp;
    
	public KettleJobResultFile() {
	}

	public String getFile() {
		return m_file;
	}

	public void setFile(String file) {
		m_file = file;
	}

	public String getParentorigin() {
		return m_parentorigin;
	}

	public void setParentorigin(String parentorigin) {
		m_parentorigin = parentorigin;
	}

	public String getOrigin() {
		return m_origin;
	}

	public void setOrigin(String origin) {
		m_origin = origin;
	}

	public String getComment() {
		return m_comment;
	}

	public void setComment(String comment) {
		m_comment = comment;
	}

	public String getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(String timestamp) {
		m_timestamp = timestamp;
	}

	
}
