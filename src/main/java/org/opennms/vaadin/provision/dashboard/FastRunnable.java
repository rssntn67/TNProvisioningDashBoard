package org.opennms.vaadin.provision.dashboard;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.rest.client.model.KettleJobStatus;
import org.opennms.rest.client.model.KettleRunJob;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicNode.OnmsState;
import org.opennms.vaadin.provision.model.BasicService;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.JobLogEntry;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;
import org.opennms.vaadin.provision.model.Job.JobStatus;
import org.opennms.vaadin.provision.model.SnmpProfile;
import org.opennms.vaadin.provision.model.Categoria;
import org.opennms.vaadin.provision.model.TrentinoNetworkNode;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.sqlcontainer.RowId;

public abstract class FastRunnable implements Runnable {

	Map<String, FastServiceLink>           m_fastOrderCodeServiceLinkMap = new HashMap<String, FastServiceLink>();
	
	Map<String, List<FastServiceDevice>>    m_fastHostnameServiceDeviceMap = new HashMap<String, List<FastServiceDevice>>();
	Map<String,Set<String>>                 m_fastIpHostnameMap            = new HashMap<String,Set<String>>();

	Map<String,TrentinoNetworkNode>        m_onmsForeignIdRequisitionNodeMap = new HashMap<String, TrentinoNetworkNode>();
	Set<String> m_onmsDuplicatedForeignId = new HashSet<String>();
	Set<String> m_onmsDuplicatedIpAddress = new HashSet<String>();

	List<BasicNode> m_updates=new ArrayList<BasicNode>();
	Map<String,Categoria> m_vrf;
	Map<String, BackupProfile> m_backup;
	Map<String, SnmpProfile> m_snmp;

    private DashBoardSessionService m_session;
	private static final Logger logger = Logger.getLogger(FastRunnable.class.getName());

    Job m_job;
	BeanItemContainer<JobLogEntry> m_logcontainer;
	boolean m_syncRequisition = false;

	public void syncRequisition() {
		m_syncRequisition=true;
	}
	
	public Collection<BasicNode> getUpdates() {
		return m_updates;
	}

	public void resetUpdateMap() {
		List<BasicNode> updates = new ArrayList<BasicNode>();
		for (BasicNode node: m_updates) {
			if (node.getOnmstate() == OnmsState.NONE)
				continue;
			updates.add(node);
		}
		m_updates = updates;
	}
	
	public FastRunnable(DashBoardSessionService session) {
		m_session = session;
		m_logcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);
	}
	
	public DashBoardSessionService getService() {
		return m_session;
	}
	public BeanItemContainer<JobLogEntry> getJobLogContainer() {
		return m_logcontainer;
	}
	
	public abstract void updateProgress(Float progress );
	public abstract void log(List<JobLogEntry> logs);

	public abstract void  beforeJob();
	public abstract void  afterJob();	

	private void commitJob(Job job) throws SQLException {
		if (job.getJobid() == null)
			getService().getJobContainer().add(job);
		else
			getService().getJobContainer().save(new RowId(new Object[]{job.getJobid()}), job);
		getService().getJobContainer().commit();
		
	}
 
	public synchronized boolean success() {
		return m_job.getJobstatus() == JobStatus.SUCCESS;		
	}
	
	public synchronized boolean running() {
		return m_job.getJobstatus() == JobStatus.RUNNING;		
	}
	
	public synchronized boolean failed() {
		return m_job.getJobstatus() == JobStatus.FAILED;		
	}
	
	public boolean startJob() {
		if (getService().getJobContainer().getLastJobId() !=  null) {
			int jobid = getService().getJobContainer().getLastJobId().getValue();
			logger.info ("found last job with id: " + jobid);
		}
        m_job = new Job();
		m_job.setUsername(getService().getUser());
		m_job.setJobdescr("FAST sync: started");
		m_job.setJobstatus(JobStatus.RUNNING);
		m_job.setJobstart(new Date());
		
		try {
			commitJob(m_job);
		} catch (SQLException e) {
			logger.log(Level.SEVERE,"failed creating job", e);
			m_job.setJobstatus(JobStatus.FAILED);
			m_job.setJobdescr("FAST sync: Failed commit JobTable");							
	        return false;
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "interrupted: ",  e);
			m_job.setJobstatus(JobStatus.FAILED);
			m_job.setJobdescr("FAST sync: Interrupted");							
	        return false;
		}
		int curjobid = getService().getJobContainer().getLastJobId().getValue();
		logger.info ("created job with id: " + curjobid);
		m_job.setJobid(curjobid);
		
		return true;
	}

	public boolean endJob() {
		m_job.setJobend(new Date());

		try {
			commitJob(m_job);
			logger.info("run: ended job in jobs table");
		} catch (final Exception e) {
			logger.log(Level.SEVERE,"Cannot end job in job table", e);
			return false;
		}
		
		for (JobLogEntry joblog: m_logcontainer.getItemIds())
			getService().getJobLogContainer().add(joblog);
		
		try {
			getService().getJobLogContainer().commit();
			logger.info("run: saving logs in joblog table");
		} catch (SQLException e) {
			logger.log(Level.SEVERE,"Exception saving logs ", e);
			return false;
		}
		
		try {
			getService().getIpSnmpProfileContainer().commit();
			logger.info("run: saving profiles in ipsnmpprofile table");
		} catch (SQLException e) {
			logger.log(Level.SEVERE,"Exception saving ipsnmpmap", e);
			return false;
		}
		
		if (m_syncRequisition) {
		try {
			getService().synctrue(DashBoardUtils.TN_REQU_NAME);
			logger.info("run: sync requisition" + DashBoardUtils.TN_REQU_NAME);
		} catch (Exception e){
			logger.log(Level.SEVERE,"cannot sync requisition ",e);
			return false;			
		}
		}
		return true;
	}

	public void log(JobLogEntry jLogE) {
		jLogE.setJobid(m_job.getJobid());
		m_logcontainer.addBean(jLogE);
	}

	public String getNote(TrentinoNetworkNode rnode) {
		StringBuffer deviceNote=new StringBuffer("Notes:");
		if (rnode.getForeignId() != null) {
			deviceNote.append(" ForeignId: ");
			deviceNote.append(rnode.getForeignId());
		}

		if (rnode.getNodeLabel() != null) {
			deviceNote.append(" NodeLabel: ");
			deviceNote.append(rnode.getNodeLabel());
		}
		
		return deviceNote.toString();
	}
		
	public String getNote(BasicInterface riface) {
		StringBuffer deviceNote=new StringBuffer("Notes:");
		if (riface.getIp() != null) {
			deviceNote.append(" ipaddr: ");
			deviceNote.append(riface.getIp());
		}

		if (riface.getDescr() != null) {
			deviceNote.append(" description: ");
			deviceNote.append(riface.getDescr());
		}
		
		if (riface.getOnmsprimary()!= null) {
			deviceNote.append(" snmpPrimary: ");
			deviceNote.append(riface.getOnmsprimary());
			
		}
		if (riface.getDescr() != null) {
			deviceNote.append(" descr: ");
			deviceNote.append(riface.getDescr());
			
		}
		return deviceNote.toString();
	
	}

	public String getNote(FastServiceDevice device) {
		StringBuffer deviceNote=new StringBuffer("Notes:");
		if (device.getDeviceType() != null) {
			deviceNote.append(" deviceType: ");
			deviceNote.append(device.getDeviceType());
		}
		if (device.getCity() != null) {
			deviceNote.append(" city: ");
			deviceNote.append(device.getCity());
		}
		if (device.getIpAddrLan() != null) {
			deviceNote.append(" ip lan");
			deviceNote.append(device.getIpAddrLan());
		}
		if (device.isNotmonitoring())
			deviceNote.append(" not_monitored");
		else
			deviceNote.append(" monitored");
		return deviceNote.toString();
	}


	public String getNote(FastServiceLink link) {
		StringBuffer deviceNote=new StringBuffer("Notes:");
		if (link.getVrf() != null) {
			deviceNote.append(" vrf: ");
			deviceNote.append(link.getVrf());
		}
		if (link.getDeliveryDeviceClientSide() != null) {
			deviceNote.append(" client device: ");
			deviceNote.append(link.getDeliveryDeviceClientSide());
		}
		if (link.getDeliveryDeviceNetworkSide() != null) {
			deviceNote.append(" network device: ");
			deviceNote.append(link.getDeliveryDeviceNetworkSide());
		}
		
		if (link.getDeliveryCode() != null) {
			deviceNote.append(" delivery Code: ");
			deviceNote.append(link.getDeliveryCode());
		}

		if (link.getOrderCode() != null) {
			deviceNote.append(" order Code: ");
			deviceNote.append(link.getOrderCode());
		}

		return deviceNote.toString();
	}

	private void runKettleJob() {
		try {
			logger.info("run: executing kettle remote procedure");
			KettleRunJob kjob = getService().getKettleDao().runJob();
	    	KettleJobStatus status = getService().getKettleDao().jobStatus(kjob);
			while (getService().getKettleDao().isRunning(status)) {
				Thread.sleep(1000);
				status = getService().getKettleDao().jobStatus(kjob);
			}
			if (!getService().getKettleDao().isFinished(status) || 
					!getService().getKettleDao().isCompleted(status)) {
				logger.log(Level.WARNING,"Failed Kettle runjob", status.getErroDescr());
				m_job.setJobstatus(JobStatus.FAILED);
				m_job.setJobdescr("FAST sync: Failed Kettle runJob. Error: " 
				+ status.getErroDescr());				
			}
		} catch (Exception e){
			logger.log(Level.WARNING,"Failed Kettle runjob", e);
			m_job.setJobstatus(JobStatus.FAILED);
			m_job.setJobdescr("FAST sync: Failed Kettle runJob. Error: " + e.getMessage());				
		}
	}
		
	private void check() {
		try {
			logger.info("run: loading table vrf");
			m_vrf = getService().getCatContainer().getCatMap();
			logger.info("run: loaded table vrf");

			logger.info("run: loading table backupprofile");
			m_backup= getService().getBackupProfileContainer().getBackupProfileMap();
			logger.info("run: loaded table backupprofile");

			logger.info("run: loading table snmpprofile");
			m_snmp= getService().getSnmpProfileContainer().getSnmpProfileMap();
			logger.info("run: loaded table snmpprofile");

			logger.info("run: loading table fastservicedevice");
			checkfastdevices(getService().getFastServiceDeviceContainer().getFastServiceDevices());
			logger.info("run: loaded table fastservicedevice");
			
			logger.info("run: loading table fastservicelink");
			checkfastlinks(getService().getFastServiceLinkContainer().getFastServiceLinks());
			logger.info("run: loaded table fastservicelink");
			
			logger.info("run: loading requisition: " + DashBoardUtils.TN_REQU_NAME);
			checkRequisition(getService().getTNContainer());
			logger.info("run: loaded requisition: " + DashBoardUtils.TN_REQU_NAME);

		} catch (final UniformInterfaceException e) {
			logger.log(Level.WARNING,"Failed syncing Fast devices with Requisition", e);
			m_job.setJobstatus(JobStatus.FAILED);
			m_job.setJobdescr("FAST check: Failed syncing Fast devices with Requisition. Error: " + e.getMessage());				
		} catch (final Exception e) {
			logger.log(Level.WARNING,"Failed init check fast integration", e);
			m_job.setJobstatus(JobStatus.FAILED);
			m_job.setJobdescr("FAST check: Failed init check Fast. Error: " + e.getMessage());
		}
		
	}

	@Override
	public void run() {
		beforeJob();
		
		if (running())
			runKettleJob();
		
		if (running())
			check();

		if (running()) {
			try {
				logger.info("run: sync Fast devices with Requisition");
				sync();
				logger.info("run: sync Fast devices with Requisition");
				m_job.setJobstatus(JobStatus.SUCCESS);
				m_job.setJobdescr("FAST sync: Done");
			} catch (final UniformInterfaceException e) {
				logger.log(Level.WARNING,"Failed syncing Fast devices with Requisition", e);
				m_job.setJobstatus(JobStatus.FAILED);
				m_job.setJobdescr("FAST sync: Failed syncing Fast devices with Requisition. Error: " + e.getMessage());				
			} catch (final Exception e) {
				logger.log(Level.SEVERE,"Failed syncing Fast", e);
				m_job.setJobstatus(JobStatus.FAILED);
				m_job.setJobdescr("FAST sync: Failed syncing Fast devices with Requisition. Error: " + e.getMessage());				
			}
		}
				
		afterJob();
	}

		private void sync() {
			double current = 0.0;
			updateProgress(new Float(current));
			
			int i = 0;
			int step = (m_fastHostnameServiceDeviceMap.size() + m_onmsForeignIdRequisitionNodeMap.size()) / 100;
			logger.info("run: step: " + step);
			logger.info("run: size: " + m_fastHostnameServiceDeviceMap.size());
			int barrier = step;

			try {

				for (String hostname: m_fastHostnameServiceDeviceMap.keySet()) {
					logger.info("FAST sync: parsing hostname: " + hostname);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					i++;
					if (i >= barrier) {
						if (current < 0.99)
							current += 0.01;
						updateProgress(new Float(current));
						barrier += step;
					}
					
					if (!checkduplicatedhostname(hostname) && !checkduplicatedipaddress(hostname)) {
						Set<String> foreignIds = new HashSet<String>();
						if (m_onmsForeignIdRequisitionNodeMap.containsKey(hostname)) {
							foreignIds.add(hostname);
						} 

						for (TrentinoNetworkNode rnode : m_onmsForeignIdRequisitionNodeMap
								.values()) {
							if (rnode.getNodeLabel().startsWith(hostname)) { 
								foreignIds.add(rnode.getForeignId());
							}
						}
						
						if (foreignIds.size() == 0) {
							add(hostname);
						} else if (foreignIds.size() == 1) {
							String foreignId = foreignIds.iterator().next();
							TrentinoNetworkNode rnode = m_onmsForeignIdRequisitionNodeMap.get(foreignId);
							if (isManagedByFast(rnode)) {
								updateFast(rnode,m_fastHostnameServiceDeviceMap.get(foreignId));
							} else { 
								updateNonFast(rnode,m_fastHostnameServiceDeviceMap.get(foreignId));
							}
						} else {
							mismatch(hostname, foreignIds);
						}
					}
				}

				for (String foreignId: m_onmsForeignIdRequisitionNodeMap.keySet()) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i++;
					if (i == barrier) {
						if (current < 0.99)
							current += 0.01;
						updateProgress(new Float(current));
						barrier += step;
					}

					if (isDeviceInFast(foreignId))
						continue;
					TrentinoNetworkNode rnode = m_onmsForeignIdRequisitionNodeMap.get(foreignId);
					if (isManagedByFast(rnode)) {
						delete(rnode);
						continue;
					}
					Set<BasicInterface> inttoDelete = new HashSet<BasicInterface>();
					for (BasicInterface riface: rnode.getServiceMap().keySet()) {
						if (riface.getDescr() == null) {
							logger.info("no Description on interface node" + getNote(rnode));
							continue;
						}
						if (riface.getDescr().contains("FAST") 
								&& !m_fastIpHostnameMap.containsKey(riface.getIp())) {
							inttoDelete.add(riface);
						}
					}
					if (inttoDelete.isEmpty())
						continue;
					for (BasicInterface bi: inttoDelete) {
						BasicService bs = new BasicService(bi);
						bs.setService("ICMP");
						rnode.delService(bs);
						BasicService bs1 = new BasicService(bi);
						bs1.setService("SNMP");
						rnode.delService(bs1);
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr(bi.getIp());
						jloe.setOrderCode("NA");
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: old referenced FAST interface deleted");
						jloe.setNote(getNote(rnode));
						logger.info("FAST sync: old referenced FAST delete interface node" + getNote(rnode));
						List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
						logs.add(jloe);
						log(logs);
					}
				}

			} catch (final UniformInterfaceException e) {
				throw e;
			} 
			
		}
		
						
		private void checkRequisition(BeanContainer<String, TrentinoNetworkNode> requisition) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			for (String id : requisition.getItemIds()) {
				TrentinoNetworkNode rnode = requisition.getItem(id).getBean();
				if (m_onmsForeignIdRequisitionNodeMap.containsKey(rnode.getForeignId())) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(rnode.getForeignId());
					jloe.setIpaddr("NA");
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: Duplicated Foreign Id in Requisition");
					jloe.setNote(getNote(rnode));
					logs.add(jloe);
					m_onmsDuplicatedForeignId.add(rnode.getForeignId());
					logger.info("Duplicated Foreign Id: " + getNote(rnode));
				} else {
					m_onmsForeignIdRequisitionNodeMap.put(rnode.getForeignId(), rnode);
				}
			}
			
			for (String dupforeignid: m_onmsDuplicatedForeignId) {
				TrentinoNetworkNode rnode = m_onmsForeignIdRequisitionNodeMap.remove(dupforeignid);
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(rnode.getForeignId());
				jloe.setIpaddr("NA");
				jloe.setJobid(m_job.getJobid());
				jloe.setDescription("FAST sync: Duplicated Foreign Id in Requisition");
				jloe.setNote(getNote(rnode));
				logs.add(jloe);
			}

			Map<String, Set<String>> onmsIpForeignIdMap = new HashMap<String, Set<String>>();
			for (String id: requisition.getItemIds()) {
				TrentinoNetworkNode rnode = requisition.getItem(id).getBean();
				for (BasicInterface riface: rnode.getServiceMap().keySet()) {
					if (riface.getIp() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr("NA");
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: Null Ip Address in Requisition");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("Null ip: " + getNote(rnode) + getNote(riface));
						continue;
					} else if (DashBoardUtils.hasInvalidIp(riface.getIp())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr(riface.getIp());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: Invalid Ip Address in Requisition");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("Invalid ip: " + getNote(rnode) + getNote(riface));
						continue;
					}
					if (!onmsIpForeignIdMap.containsKey(riface.getIp())) {
						onmsIpForeignIdMap.put(riface.getIp(), new HashSet<String>());
					onmsIpForeignIdMap.get(riface.getIp()).add(rnode.getForeignId());
					}
				}
			}

			for (String ipaddr: onmsIpForeignIdMap.keySet()) {
				if (onmsIpForeignIdMap.get(ipaddr).size() == 1)
					continue;
				m_onmsDuplicatedIpAddress.add(ipaddr);
				for (String foreignid: onmsIpForeignIdMap.get(ipaddr) ) {
					TrentinoNetworkNode duplicatedipnode = m_onmsForeignIdRequisitionNodeMap.remove(foreignid);
					if (duplicatedipnode == null)
						continue;
					BasicInterface duplicatedinterface = duplicatedipnode.getInterface(ipaddr);
					if (duplicatedinterface == null)
						continue;
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(foreignid);
					jloe.setIpaddr(ipaddr);
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: Duplicated Ip Address in Requisition");
					jloe.setNote(getNote(duplicatedipnode) + getNote(duplicatedinterface));
					logs.add(jloe);
				}

			}
			log(logs);
		}
  		
		
		private void checkfastdevices(List<FastServiceDevice> devices) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			for (FastServiceDevice device:devices) {
				if (device.isNotmonitoring()){ 
					logger.info("Skipping service device. Cause: not monitored. hostname: " + device.getHostname() + " ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					continue;
				}
				if (device.getHostname() != null && device.getIpaddr() != null ) {
					if (DashBoardUtils.hasInvalidIp(device.getIpaddr())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Ip Address");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: invalid ip. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (DashBoardUtils.hasInvalidDnsBind9Label(device.getHostname())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Hostname");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: invalid hostname. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (device.getNotifyCategory() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Null Notify Category");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: null notify category. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (!DashBoardUtils.isValidNotifyLevel(device.getNotifyCategory())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Notify Category: " + device.getNotifyCategory());
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: invalid notify category. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (device.getSnmpprofile() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Null Snmp Profile");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: null Snmp Profile. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (!m_snmp.containsKey(device.getSnmpprofile())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Snmp Profile: " + device.getSnmpprofile());
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: Invalid Snmp Profile. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (device.getBackupprofile() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Null Backup Profile");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: null Backup Profile. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (!m_backup.containsKey(device.getBackupprofile())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(m_job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Backup Profile: " + device.getBackupprofile());
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: Invalid Backup Profile. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else {
						logger.info("Adding service device. hostname:  " + device.getHostname() + " ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));

						if (!m_fastHostnameServiceDeviceMap.containsKey(device.getHostname().toLowerCase())) {
							m_fastHostnameServiceDeviceMap.put(device.getHostname().toLowerCase(), new ArrayList<FastServiceDevice>());
						}
						m_fastHostnameServiceDeviceMap.get(device.getHostname().toLowerCase()).add(device);
						
						if (!m_fastIpHostnameMap.containsKey(device.getIpaddr())) {
								m_fastIpHostnameMap.put(device.getIpaddr(), new HashSet<String>());
						}						
						m_fastIpHostnameMap.get(device.getIpaddr()).add(device.getHostname().toLowerCase());
						logger.info("Adding to ip map. hostname:  " + device.getHostname() + " ipaddr: " + device.getIpaddr());
					}
				} else if (device.getHostname() == null && device.getIpaddr() == null ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname("NA");
					jloe.setIpaddr("NA");
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null hostname and null ip address");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null hostname and null ip address. order_code: " +  device.getOrderCode() + " " +getNote(device));
				} else if (device.getHostname() != null && device.getIpaddr() == null) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(device.getHostname());
					jloe.setIpaddr("NA");
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null ip address");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null ip address. hostname: " + device.getHostname() + " order_code: " +  device.getOrderCode() + " " + getNote(device));
				} else if (device.getHostname() == null && device.getIpaddr() != null ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname("NA");
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null hostname");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null hostname. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
				} 	
			}
			
			for (String ipaddr: m_fastIpHostnameMap.keySet()) {
				if (m_fastIpHostnameMap.get(ipaddr).size() == 1)
					continue;
				for (String hostname: m_fastIpHostnameMap.get(ipaddr)) {
					List<FastServiceDevice> survived = new ArrayList<FastServiceDevice>();
					for (FastServiceDevice device: m_fastHostnameServiceDeviceMap.remove(hostname)) {
						if (device.getIpaddr().equals(ipaddr)) {
							final JobLogEntry jloe = new JobLogEntry();
							jloe.setHostname(hostname);
							jloe.setIpaddr(ipaddr);
							jloe.setOrderCode(device.getOrderCode());
							jloe.setJobid(m_job.getJobid());
							jloe.setDescription("FAST sync: Same ip found on different hostnames");
							jloe.setNote(getNote(device));
							logs.add(jloe);
							logger.info("ip address found on different hostnames: " +  getNote(device));
						} else {
							survived.add(device);
						}
					}
					if (!survived.isEmpty())
						m_fastHostnameServiceDeviceMap.put(hostname, survived);
					
				}
			}
			
			log(logs);
		}
		
		private void checkfastlinks(List<FastServiceLink> links) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			final Set<String> duplicatedEntry = new HashSet<String>();
			for (FastServiceLink link:links) {
				if (link.getOrderCode() == null) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(link.getDeliveryDeviceClientSide());
					jloe.setIpaddr("NA");
					jloe.setOrderCode("NA");
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service link. Cause: null order_code");
					jloe.setNote(getNote(link));
					logs.add(jloe);
					logger.info("skipping service link. Cause: null order_code: "  +  getNote(link));
					continue;
				} 
				if (link.getVrf() == null) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(link.getDeliveryDeviceClientSide());
					jloe.setIpaddr("NA");
					jloe.setOrderCode(link.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service link. Cause: null Vrf");
					jloe.setNote(getNote(link));
					logs.add(jloe);
					logger.info("skipping service link. Cause: null Vrf: "  +  getNote(link));
					continue;					
				}
				if (!m_vrf.containsKey(link.getVrf()) ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(link.getDeliveryDeviceClientSide());
					jloe.setIpaddr("NA");
					jloe.setOrderCode(link.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service link. Cause: invalid Vrf");
					jloe.setNote(getNote(link));
					logs.add(jloe);
					logger.info("skipping service link. Cause: invalid Vrf: "  +  getNote(link));
					continue;
				}

				if (m_fastOrderCodeServiceLinkMap.containsKey(link.getOrderCode())) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(link.getDeliveryDeviceClientSide());
					jloe.setIpaddr("NA");
					jloe.setOrderCode(link.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service link. Cause: duplicated order_code");
					jloe.setNote(getNote(link));
					logs.add(jloe);
					logger.info("skipping service link. Cause: duplicated order_code: "  +  getNote(link));
					duplicatedEntry.add(link.getOrderCode());
					continue;
					
				}
				m_fastOrderCodeServiceLinkMap.put(link.getOrderCode(), link);
			}
			for (String duplioc : duplicatedEntry ) {
				FastServiceLink link = m_fastOrderCodeServiceLinkMap.remove(duplioc);
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(link.getDeliveryDeviceClientSide());
				jloe.setIpaddr("NA");
				jloe.setOrderCode(link.getOrderCode());
				jloe.setJobid(m_job.getJobid());
				jloe.setDescription("FAST sync: skipping service link. Cause: duplicated order_code");
				jloe.setNote(getNote(link));
				logs.add(jloe);
				logger.info("skipping service link. Cause: duplicated order_code: "  +  getNote(link));
			}
			
			log(logs);
		}


		private boolean checkduplicatedhostname(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			if (m_onmsDuplicatedForeignId.contains(hostname)) {
				for (FastServiceDevice device : m_fastHostnameServiceDeviceMap
						.get(hostname)) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(hostname);
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: duplicated foreign id in requisition");
					jloe.setNote("Hostname correspond to a duplicated foreignid: " + hostname + getNote(device));
					logger.info("skipping service device. Cause: duplicated foreignid in requisition: " + hostname + getNote(device));
					logs.add(jloe);
				}
				log(logs);
				return true;
			} 
			return false;
		}
		
		private boolean checkduplicatedipaddress(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			boolean duplicated = false;
			for (FastServiceDevice device : m_fastHostnameServiceDeviceMap
					.get(hostname)) {
				if (m_onmsDuplicatedIpAddress.contains(device.getIpaddr())) {
					duplicated = true;
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(device.getHostname());
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: duplicated ipaddr in requisition");
					jloe.setNote(getNote(device) + " Duplicated ips: " + device.getIpaddr());
					logger.info("skipping service device. Cause: duplicated ipaddr in requisition " + getNote(device) + " Duplicated ips: " + device.getIpaddr());
					logs.add(jloe);
				}
			}
			if (duplicated) {
				log(logs);
			}
			return duplicated;
		}
		
		private void mismatch(String hostname, Set<String> foreignIds) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();

			for (FastServiceDevice device : m_fastHostnameServiceDeviceMap
					.get(hostname)) {
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(m_job.getJobid());
				jloe.setDescription("FAST sync: skipping service device. Cause: Mismatch hostname/foreignIds");
				jloe.setNote("hostname/foreignIds" + hostname + "/"+ foreignIds + getNote(device)  );
				logger.info("skipping service device. Cause: Mismatch hostname/foreignIds" + hostname + "/"
						+ foreignIds);
				logs.add(jloe);
			}

			log(logs);

		}
		
		private void norefdevice(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();

			for (FastServiceDevice device : m_fastHostnameServiceDeviceMap
					.get(hostname)) {
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(m_job.getJobid());
				jloe.setDescription("FAST sync: skipping add service device. Cause: No Valid ref device for order_code");
				jloe.setNote(getNote(device));
				logger.info("skipping service add device. Cause: No Valid ref device for order_code " + getNote(device));
				logs.add(jloe);
			}

			log(logs);

	
		}
				
		private void add(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			Set<String> secondary = new HashSet<String>(); 
			FastServiceDevice refdevice = null;
			for (FastServiceDevice device: m_fastHostnameServiceDeviceMap.get(hostname)) {
				secondary.add(device.getIpaddr());
				if (!m_fastOrderCodeServiceLinkMap.containsKey(device.getOrderCode()))
					continue;
				if (refdevice == null ) {
					refdevice= device;
				} else 	if (device.isMaster() && !refdevice.isMaster()) {
					refdevice= device;
				} else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
					refdevice= device;
				} else if (device.getIpAddrLan() != null && device.getIpAddrLan() == null) {
					refdevice= device;
				}

				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(m_job.getJobid());
				jloe.setDescription("FAST sync: added service device.");
				jloe.setNote(getNote(device));
				logs.add(jloe);
			}

			if (refdevice == null) {
				norefdevice(hostname);
				return;
			}
			
			FastServiceLink reflink= m_fastOrderCodeServiceLinkMap.get(refdevice.getOrderCode());						
			TrentinoNetworkNode rnode = new TrentinoNetworkNode(hostname, m_vrf.get(reflink.getVrf()), DashBoardUtils.TN_REQU_NAME);
			rnode.setHostname(hostname);
			rnode.setForeignId(hostname);
			rnode = getnode(refdevice, reflink, rnode, secondary);


			getService().add(rnode);
			m_updates.add(rnode);
			updateSnmp(refdevice);			
			log(logs);
		}
				
		private void updateNonFast(TrentinoNetworkNode rnode, List<FastServiceDevice> devices) {
			Set<String> fastipaddressonnode = new HashSet<String>();
			for (FastServiceDevice device: devices) {
				BasicInterface bi = rnode.getInterface(device.getIpaddr());
				if (bi == null) {
					bi = new BasicInterface();
					bi.setIp(device.getIpaddr());
					bi.setDescr(DashBoardUtils.DESCR_FAST);
					bi.setOnmsprimary(OnmsPrimary.N);
				}
				fastipaddressonnode.add(device.getIpaddr());
				BasicService bs = new BasicService(bi);
				bs.setService("ICMP");
				rnode.addService(bs);
			}

			Set<BasicInterface> fastiponnodetodelete = new HashSet<BasicInterface>();
			for (BasicInterface riface: rnode.getServiceMap().keySet()) {
				if (riface.getDescr().contains("FAST") && !fastipaddressonnode.contains(riface.getIp())) {
					fastiponnodetodelete.add(riface);
				}
			}
			List<JobLogEntry> logs = new ArrayList<JobLogEntry>();

			for (BasicInterface bi: fastiponnodetodelete) {
				BasicService bs = new BasicService(bi);
				bs.setService("ICMP");
				rnode.delService(bs);
				BasicService bs2 = new BasicService(bi);
				bs2.setService("SNMP");
				rnode.delService(bs2);
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(rnode.getForeignId());
				jloe.setIpaddr(bi.getIp());
				jloe.setOrderCode("NA");
				jloe.setJobid(m_job.getJobid());
				jloe.setDescription("FAST sync: updateNonFast: interface deleted");
				jloe.setNote(getNote(rnode));
				logger.info("updateNonFast: delete interface node" + getNote(rnode));
				logs.add(jloe);
			}
			
			if (rnode.getOnmstate() == OnmsState.NONE)
				return;
			getService().update(rnode);
			m_updates.add(rnode);

			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(rnode.getHostname());
			jloe.setIpaddr(rnode.getPrimary());
			jloe.setOrderCode("NA");
			jloe.setDescription("FAST sync: updateNonFast: updated Fast Ip.");
			jloe.setNote(getNote(rnode));
			
			logs.add(jloe);
			log(logs);

		}

		private TrentinoNetworkNode getnode(FastServiceDevice refdevice, FastServiceLink reflink, TrentinoNetworkNode rnode, Set<String> ipaddresses) {
			rnode.setVrf(m_vrf.get(reflink.getVrf()).getDnsdomain());
			rnode.setPrimary(refdevice.getIpaddr());
			rnode.setBackupProfile(refdevice.getBackupprofile());
			rnode.setSnmpProfile(refdevice.getSnmpprofile());
			rnode.setNetworkCategory(m_vrf.get(reflink.getVrf()));
			if (refdevice.getNotifyCategory().
					equals(DashBoardUtils.m_fast_default_notify)) {
				rnode.setNotifCategory(m_vrf.get(reflink.getVrf()).getNotifylevel());
			} else {
				rnode.setNotifCategory(refdevice.getNotifyCategory());
			}
			rnode.setThreshCategory(m_vrf.get(reflink.getVrf()).getThresholdlevel());
			rnode.setCity(refdevice.getCity());
			
			StringBuffer address1 = new StringBuffer();
			if (refdevice.getAddressDescr() != null)
				address1.append(refdevice.getAddressDescr());
			if (refdevice.getAddressName() != null) {
				if (address1.length() > 0)
					address1.append(" ");
				address1.append(refdevice.getAddressName());
			}
			if (refdevice.getAddressNumber() != null) {
				if (address1.length() > 0)
					address1.append(" ");
				address1.append(refdevice.getAddressNumber());
			}
			rnode.setAddress1(address1.toString());
			rnode.setCircuitId(reflink.getDeliveryCode());
			rnode.setBuilding(refdevice.getIstat()+"-"+reflink.getSiteCode());
			
			for (String ip : ipaddresses) {
				if (ip.equals(refdevice.getIpaddr()))
					continue;
				BasicInterface bi = rnode.getInterface(ip);
				if ( bi == null) {
					bi = new BasicInterface();
					bi.setIp(ip);
					bi.setDescr(DashBoardUtils.DESCR_FAST);
					bi.setOnmsprimary(OnmsPrimary.N);
				}
				BasicService bs = new BasicService(bi);
				bs.setService("ICMP");
				rnode.addService(bs);
			}
			Set<BasicInterface> intefacetodelonnode = new HashSet<BasicInterface>();
			for (BasicInterface riface: rnode.getServiceMap().keySet()) {
				if (!ipaddresses.contains(riface.getIp())) {
					intefacetodelonnode.add(riface);
				}
			}
			for (BasicInterface bi: intefacetodelonnode) {
				BasicService bs = new BasicService(bi);
				bs.setService("ICMP");
				rnode.delService(bs);
				BasicService bs2 = new BasicService(bi);
				bs2.setService("SNMP");
				rnode.delService(bs2);
			}
			return rnode;
		}
		
		private void updateFast(TrentinoNetworkNode rnode,List<FastServiceDevice> fastdevices) {
			Set<String> ipaddresses = new HashSet<String>(); 
			FastServiceDevice refdevice = null;
			for (FastServiceDevice device: fastdevices) {
				ipaddresses.add(device.getIpaddr());
				if (!m_fastOrderCodeServiceLinkMap.containsKey(device.getOrderCode()))
					continue;
				if (refdevice == null ) {
					refdevice= device;
				} else if (rnode.getInterface(device.getIpaddr()) != null && 
						rnode.getInterface(device.getIpaddr()).getOnmsprimary()
						== OnmsPrimary.P) {
					refdevice= device;
				} else 	if (device.isMaster() && !refdevice.isMaster()) {
					refdevice= device;
				} else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
					refdevice= device;
				} else if (device.getIpAddrLan() != null && device.getIpAddrLan() == null) {
					refdevice= device;
				}
			}

			if (refdevice == null) {
				norefdevice(rnode.getHostname());
				return;
			}
			rnode = getnode(refdevice, m_fastOrderCodeServiceLinkMap.get(refdevice.getOrderCode()),rnode, ipaddresses);

			if (rnode.getOnmstate() == OnmsState.NONE)
				return;
			
			getService().update(rnode);
			m_updates.add(rnode);
			updateSnmp(refdevice);
			
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(refdevice.getHostname());
			jloe.setIpaddr(refdevice.getIpaddr());
			jloe.setOrderCode(refdevice.getOrderCode());
			jloe.setJobid(m_job.getJobid());
			jloe.setDescription("FAST sync: updateFast: updated service device.");
			jloe.setNote(getNote(refdevice));
			logger.info("FAST sync: updateFast updated: " + getNote(refdevice));

			List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			logs.add(jloe);
			log(logs);
			

		}
		
		private boolean updateSnmp(FastServiceDevice refdevice)  {
			String snmpprofile = refdevice.getSnmpprofile();
			try {
				if ( getService().saveSnmpProfile(refdevice.getIpaddr(), snmpprofile) ) {
					logger.info("FAST sync: set snmp profile: " + snmpprofile);
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(refdevice.getHostname());
					jloe.setIpaddr(refdevice.getIpaddr());
					jloe.setOrderCode(refdevice.getOrderCode());
					jloe.setJobid(m_job.getJobid());
					jloe.setDescription("FAST sync: set snmp profile: " + snmpprofile);
					jloe.setNote(getNote(refdevice));
					
					List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
					logs.add(jloe);
					log(logs);
					return true;
				} 
			} catch (SQLException e) {
				logger.log(Level.WARNING,"FAST sync: cannot set snmp profile: " + snmpprofile,e);
			}
			
			return false;

		}

		private boolean isDeviceInFast(String foreignId) {
			if (m_fastHostnameServiceDeviceMap.containsKey(foreignId)) {
				return true;
			}
			for (String hostname: m_fastHostnameServiceDeviceMap.keySet()) {
				if (m_onmsForeignIdRequisitionNodeMap.get(foreignId).getNodeLabel().
						startsWith(hostname)) {
					return true;
				}
			}
			return false;
		}
		
		private boolean isManagedByFast(TrentinoNetworkNode rnode) {
			if (rnode.getNetworkCategory() == null) {
				logger.info("FAST sync: isManagedByFast is Not: " + rnode.getHostname());
				return false;
			}
			if (DashBoardUtils.m_network_levels[2].equals(rnode.getNetworkCategory().getNetworklevel())) {
				for (BasicInterface riface: rnode.getServiceMap().keySet()) {
					if (riface.getOnmsprimary() == null || !riface.getOnmsprimary().equals(OnmsPrimary.P)) {
						continue;
					}
					if (riface.getDescr() != null &&
							riface.getDescr().contains("FAST") ) {
						logger.info("FAST sync: isManagedByFast: " + rnode.getHostname());
						return true;
					}
				}
			}
			logger.info("FAST sync: isManagedByFast is Not: " + rnode.getHostname());
			return false;
		}
		
		private void delete(TrentinoNetworkNode rnode) {
			rnode.setDeleteState();
			m_updates.add(rnode);
			getService().delete(rnode);
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(rnode.getForeignId());
			jloe.setIpaddr("NA");
			jloe.setOrderCode("NA");
			jloe.setJobid(m_job.getJobid());
			jloe.setDescription("FAST sync: node deleted");
			jloe.setNote(getNote(rnode));
			logger.info("delete node" + getNote(rnode));
			List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			logs.add(jloe);
			log(logs);
			
		} 		
}


