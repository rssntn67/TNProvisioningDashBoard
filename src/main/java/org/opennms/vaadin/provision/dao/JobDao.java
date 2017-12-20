package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;

import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.Job.JobStatus;

import com.vaadin.data.Property;
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
	
	@SuppressWarnings("unchecked")
	public synchronized void save(RowId id, Job job) {
		getContainerProperty(id, "jobdescr").setValue(job.getJobdescr());
		getContainerProperty(id, "jobstatus").setValue(job.getJobstatus().name());
		getContainerProperty(id, "jobstart").setValue(job.getJobstart());
		getContainerProperty(id, "jobend").setValue(job.getJobend());
		getContainerProperty(id, "username").setValue(job.getUsername());
	}
	
	public synchronized void saveOrUpdate(RowId id, Job job) {
		if (id == null)
			save((RowId)addItem(),job);
		else
			save(id, job);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Property<Integer> getLastJobId() {
		return getContainerProperty(lastItemId(), "jobid");
	}

	public synchronized boolean isFastRunning() {
		if (lastItemId() == null ) {
			return false;
		}
		return 
			JobStatus.RUNNING == JobStatus.valueOf(
					getItem(
							lastItemId()).
							getItemProperty("jobstatus").
							getValue().
							toString().toUpperCase());
	}

}
