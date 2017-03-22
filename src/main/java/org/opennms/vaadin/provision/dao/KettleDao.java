package org.opennms.vaadin.provision.dao;

import org.opennms.rest.client.JerseyClientImpl;
import org.opennms.rest.client.JerseyKettleService;
import org.opennms.rest.client.model.KettleJobStatus;
import org.opennms.rest.client.model.KettleRunJob;

public class KettleDao {

    
	private JerseyKettleService m_kettleService;

	public KettleDao(JerseyClientImpl client) {
		m_kettleService = new JerseyKettleService();
	    m_kettleService.setJerseyClient(client);
	}

	public KettleRunJob runJob() {
		return m_kettleService.runJob();
	}
	
	public KettleJobStatus jobStatus(KettleRunJob job) {
		return m_kettleService.jobStatus(job);
	}
	
	public boolean isFinished(KettleJobStatus status) {
		return "Finished".equals(status.getStatusDescr());
	}

	public boolean isRunning(KettleJobStatus status) {
		return "Running".equals(status.getStatusDescr());
	}
	
	public boolean isCompleted(KettleJobStatus status) {
		if (status.getResult() == null )
			return false;
		if (!"Y".equals(status.getResult().getResult()))
			return false;
		if (status.getResult().getExitStatus() == null )
			return false;
		try {
			if ( 0 != Integer.valueOf(status.getResult().getExitStatus()).intValue() )
				return false;
		} catch (Exception e) {
			return false;
		}
		return "N".equals( status.getResult().getIsStopped());
	}
}
