package org.opennms.rest.client.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webresult")
@Entity
public class KettleRunJob implements Serializable{

	public KettleRunJob() {}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5090384884167355302L;
    @XmlElement(name="result")
	String m_result;
    @XmlElement(name="message")
	String m_message;
    @XmlElement(name="id")
	String m_id;
    
	public String getResult() {
		return m_result;
	}
	
	public void setResult(String result) {
		m_result = result;
	}
	
	public String getMessage() {
		return m_message;
	}
	
	public void setMessage(String message) {
		m_message = message;
	}
	
	public String getId() {
		return m_id;
	}
	public void setId(String id) {
		m_id = id;
	}

}
