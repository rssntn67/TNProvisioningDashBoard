package org.opennms.vaadin.provision.fast;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.dashboard.FastTab;
import org.opennms.vaadin.provision.model.FastServiceDevice;
//import org.opennms.vaadin.provision.model.FastServiceLink;





import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.Job.JobStatus;
import org.opennms.vaadin.provision.model.JobLogEntry;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.ClientResponse.Status;

public class FastIntegrationRunnable implements Runnable {

	private static final Logger logger = Logger.getLogger(FastIntegrationRunnable.class.getName());

	final private FastTab m_tab;
	Integer jobId;
	
	public FastIntegrationRunnable(FastTab tab) {
		m_tab = tab;
	}

	@Override
	public void run() {
		double current = 0.0;

		Job job = new Job();
		job.setUsername(m_tab.getService().getUser());
		job.setJobdescr("FAST synchronization");
		job.setJobstatus(JobStatus.RUNNING);
		job.setJobstart(new Date());

		jobId = m_tab.commitJob(job);
		job.setJobid(jobId);
		
		logger.info("run: loading fast service device table ");
		List<FastServiceDevice> devices = m_tab.getService().getTnDao().getFastServiceDeviceContainer().getFastServiceDevices();
		logger.info("run: finishing loading table ");
		int step = devices.size()  / 100;
		logger.info("run: step: " + step);
		logger.info("run: size: " + devices.size());
		int i = 0;
		
		try {
			for (FastServiceDevice device:devices) {
				process(device);
				i++;
				if (i == step) {
						current += 0.01;
						m_tab.setCaptionRunning(new Float(current));
						step = step + i;
				}
			}
			job.setJobend(new Date());
			job.setJobstatus(JobStatus.SUCCESS);
		} catch (Exception e) {
			job.setJobend(new Date());
			job.setJobstatus(JobStatus.FAILED);
		} 
		m_tab.commitJob(job);
		m_tab.setCaptionRunning(new Float(1.0));
		m_tab.setCaptionReady();
	}
			
	private void process(FastServiceDevice device) {
		logger.info("process: processing hostname: " + device.getHostname() + " ip: " + device.getIpaddr());
		JobLogEntry jloe = new JobLogEntry();
		jloe.setJobid(jobId);
		jloe.setDescription("FAST sync: processing entry");
		jloe.setHostname(device.getHostname());
		jloe.setIpaddr(device.getIpaddr());
		jloe.setOrderCode(device.getOrderCode());
		m_tab.log(jloe);
		if (device.getHostname() != null) {
			try {
				RequisitionNode rnode = m_tab.getService().getOnmsDao().getRequisitionNode("TrentinoNetwork",device.getHostname());
				JobLogEntry jle1 = new JobLogEntry();
				jle1.setJobid(jobId);
				jle1.setDescription("FAST sync: found on requisition TrentinoNetwork. nodelabel: " + rnode.getNodeLabel());
				jle1.setHostname(device.getHostname());
				jle1.setIpaddr(device.getIpaddr());
				jle1.setOrderCode(device.getOrderCode());
				m_tab.log(jloe);
			} catch (UniformInterfaceException e) {
				if (e.getResponse().getStatusInfo() == Status.NOT_FOUND) {
					JobLogEntry jle1 = new JobLogEntry();
					jle1.setJobid(jobId);
					jle1.setDescription("FAST sync: hostname not found on requisition TrentinoNetwork");
					jle1.setHostname(device.getHostname());
					jle1.setIpaddr(device.getIpaddr());
					jle1.setOrderCode(device.getOrderCode());
					m_tab.log(jloe);
					logger.info("process: not found on requisition hostname: " + device.getHostname() + " ip: " + device.getIpaddr());
				} else {
					e.printStackTrace();
				}
			}
		} else { 
			JobLogEntry jle1 = new JobLogEntry();
			jle1.setJobid(jobId);
			jle1.setDescription("FAST sync: null hostname");
			jle1.setHostname(device.getHostname());
			jle1.setIpaddr(device.getIpaddr());
			jle1.setOrderCode(device.getOrderCode());
			m_tab.log(jloe);
		}
		//List<FastServiceLink> links = m_tab.getService().getFastServiceLinks();
	}
}
