package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;

import org.opennms.vaadin.provision.model.Job;

import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class JobDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -963057531610916225L;

	public JobDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(Job job) {
		save((RowId)addItem(),job);
	}
	
	public synchronized void save(RowId id, Job job) {
		getContainerProperty(id, "jobdescr").setValue(job.getJobdescr());
		getContainerProperty(id, "jobstatus").setValue(job.getJobstatus().name());
		getContainerProperty(id, "jobstart").setValue(job.getJobstart());
		getContainerProperty(id, "jobend").setValue(job.getJobend());
		getContainerProperty(id, "username").setValue(job.getUsername());
	}		
}
