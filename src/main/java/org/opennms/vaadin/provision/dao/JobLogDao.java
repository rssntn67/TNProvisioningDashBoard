package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.vaadin.provision.model.JobLogEntry;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class JobLogDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -963057531610916225L;

	public JobLogDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(JobLogEntry joblog) {
		save((RowId)addItem(),joblog);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(RowId id, JobLogEntry joblog) {
		getContainerProperty(id, "jobid").setValue(joblog.getJobid());
		getContainerProperty(id, "hostname").setValue(joblog.getHostname());
		getContainerProperty(id, "ipaddr").setValue(joblog.getIpaddr());
		getContainerProperty(id, "order_code").setValue(joblog.getOrderCode());
		getContainerProperty(id, "description").setValue(joblog.getDescription());
		getContainerProperty(id, "note").setValue(joblog.getNote());
	}
	
	public synchronized void saveOrUpdate(RowId id, JobLogEntry joblog) {
		if (id == null)
			save((RowId)addItem(),joblog);
		else
			save(id, joblog);
	}
	
	public synchronized List<JobLogEntry> getJoblogs(int jobid) {
		addContainerFilter(new Compare.Equal("jobid", jobid));
    	List<JobLogEntry> joblogs = new ArrayList<JobLogEntry>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			JobLogEntry joblog = new JobLogEntry();
			Item joblogRow = getItem(i.next());
			joblog.setJoblogid((Integer)joblogRow.getItemProperty("joblogid").getValue());
			joblog.setJobid((Integer)joblogRow.getItemProperty("jobid").getValue());
			joblog.setHostname((String)joblogRow.getItemProperty("hostname").getValue());
			joblog.setIpaddr((String)joblogRow.getItemProperty("ipaddr").getValue());
			joblog.setOrderCode((String)joblogRow.getItemProperty("order_code").getValue());
			joblog.setDescription((String)joblogRow.getItemProperty("description").getValue());
			joblog.setNote((String)joblogRow.getItemProperty("note").getValue());
			joblogs.add(joblog);
		}
    	removeAllContainerFilters();
    	return joblogs;
    	
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Property<Integer> getLastJobId() {
		return getContainerProperty(lastItemId(), "jobid");
	}

}
