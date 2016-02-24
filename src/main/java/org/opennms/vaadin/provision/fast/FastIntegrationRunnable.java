package org.opennms.vaadin.provision.fast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dashboard.FastTab;
import org.opennms.vaadin.provision.model.FastServiceDevice;
//import org.opennms.vaadin.provision.model.FastServiceLink;





import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.Job.JobStatus;
import org.opennms.vaadin.provision.model.JobLogEntry;

public class FastIntegrationRunnable implements Runnable {

	private static final Logger logger = Logger.getLogger(FastIntegrationRunnable.class.getName());

	final private FastTab m_tab;
	Integer jobId;
	
	public FastIntegrationRunnable(FastTab tab) {
		m_tab = tab;
	}

	@Override
	public void run() {
		
		m_tab.getUI().getSession().getLockInstance().lock();
		double current = 0.0;
		m_tab.setCaptionRunning(new Float(current));

		Job job = new Job();
		job.setUsername(m_tab.getService().getUser());
		job.setJobdescr("FAST synchronization");
		job.setJobstatus(JobStatus.RUNNING);
		job.setJobstart(new Date());

		jobId = m_tab.commitJob(job);
		job.setJobid(jobId);
		
		logger.info("run: loading fast service device table ");
		
		List<FastServiceDevice> devices = new ArrayList<FastServiceDevice>();
		Requisition tn;
		Map<String,RequisitionNode> rnmap = new HashMap<String, RequisitionNode>();
		//List<FastServiceLink> links = m_tab.getService().getFastServiceLinks();
		try {
			devices = m_tab.getService().getFastServiceDeviceContainer().getFastServiceDevices();
			logger.info("run: loaded table fastservicedevice");
			tn = m_tab.getService().getOnmsDao().getRequisition(DashBoardUtils.TN);
			logger.info("run: loaded requisition: " + DashBoardUtils.TN);
			int step = devices.size()  / 100;
			logger.info("run: step: " + step);
			logger.info("run: size: " + devices.size());
			
			for (RequisitionNode rnode: tn.getNodes()) {
				rnmap.put(rnode.getForeignId(), rnode);
			}
			
			int i = 0;
			for (FastServiceDevice device:devices) {
				process(device, rnmap);
				i++;
				if (i == step) {
						current += 0.01;
						m_tab.setCaptionRunning(new Float(current));
						step += step;
						logger.info("run: updating: " + step);
				}
			}
			job.setJobstatus(JobStatus.SUCCESS);
		} catch (Exception e) {
			job.setJobstatus(JobStatus.FAILED);
			job.setJobdescr(job.getJobdescr()+ ": " + e.getMessage());
		} finally {
			job.setJobend(new Date());
			m_tab.setCaptionReady();
			m_tab.commitJob(job);
			m_tab.getUI().getSession().getLockInstance().unlock();

		}
	}
			
	private void process(FastServiceDevice device, Map<String,RequisitionNode> rnmap) {
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		logger.info("process: processing hostname: " + device.getHostname() + " ip: " + device.getIpaddr());
		JobLogEntry jloe = new JobLogEntry();
		jloe.setJobid(jobId);
		jloe.setHostname(device.getHostname());
		jloe.setIpaddr(device.getIpaddr());
		jloe.setOrderCode(device.getOrderCode());
		if (device.getHostname() != null) {
			if (rnmap.containsKey(device.getHostname())) {
//				RequisitionNode rnode = rnmap.get(device.getHostname());
				jloe.setDescription("FAST sync: label found on requisition TrentinoNetwork.");
//				logger.info("process: found requisition node: " + rnode.getForeignId() + " nodelabel: " + rnode.getNodeLabel());
			} else {
				jloe.setDescription("FAST sync: hostname not found on requisition TrentinoNetwork");
//				logger.info("process: not found on requisition hostname: " + device.getHostname() + " ip: " + device.getIpaddr());
			}
		} else { 
			jloe.setDescription("FAST sync: null hostname");
//			logger.info("process: hostname is null for order code: " + device.getOrderCode() + " ip: " + device.getIpaddr());
		}
		m_tab.log(jloe);
	}
}
