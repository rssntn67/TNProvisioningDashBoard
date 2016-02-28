package org.opennms.vaadin.provision.dashboard;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.util.Log;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.JobLogEntry;
import org.opennms.vaadin.provision.model.Job.JobStatus;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Title("TNPD - Fast Integration")
@Theme("runo")
public class FastTab extends DashboardTab implements ClickListener {

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());

	private Panel m_panel  = new Panel("Fast Integration - Status: Ready");
    private Button m_fast = new Button("Start Fast Integration");
    final ProgressBar m_progress = new ProgressBar();

    private JobDao m_jobcontainer;
    private boolean m_loaded = false;
    
    private Table m_jobTable =  new Table();
    private Table m_logTable =  new Table();
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public FastTab(DashBoardSessionService service) {
		super(service);
	}

	@Override
	public void load() {
		if (m_loaded) 
			return;
		m_jobcontainer = getService().getJobContainer();
		m_jobTable.setContainerDataSource(m_jobcontainer);
		m_jobTable.setVisibleColumns(new Object[] {"jobid", "username", "jobdescr","jobstatus","jobstart","jobend"});
		m_panel.setContent(getFastBox());
		setCompositionRoot(m_panel);
		m_fast.addClickListener(this);
    	m_loaded = true;
	}

	private Component getFastBox() {
	   	VerticalLayout layout = new VerticalLayout();
    	layout.setMargin(true);
        layout.addComponent(m_fast);
        layout.addComponent(m_progress);
	    m_progress.setEnabled(false);
	    m_progress.setVisible(false);
        
        HorizontalLayout tablelayout = new HorizontalLayout();
        layout.addComponent(m_jobTable);
        layout.addComponent(m_logTable);
        layout.addComponent(tablelayout);
        m_logTable.setVisible(false);
        return layout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_fast) {
			m_logTable.setVisible(false);
			m_logTable.setContainerDataSource(new BeanItemContainer<JobLogEntry>(JobLogEntry.class) );
	        UI.getCurrent().setPollInterval(2000);
	        	        
	        FastIntegrationRunnable runnable = new FastIntegrationRunnable();
	        Thread thread = new Thread(runnable);
	        thread.start();		
	        
	        m_panel.setCaption("Fast Integration - Status: Running");
	        Notification.show("Fast Integration - Status: Started", Type.HUMANIZED_MESSAGE);
		}
	}
			
	class FastIntegrationRunnable implements Runnable {

		volatile double current = 0.0;
	    volatile Job job = new Job();
		volatile BeanItemContainer<JobLogEntry> m_logcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);
		Map<String,String> m_ipForeignIdMap = new HashMap<String, String>();

		Map<String, List<FastServiceLink>>     m_fastServiceLinkMap = new HashMap<String, List<FastServiceLink>>();
		Map<String, List<FastServiceDevice>> m_fastServiceDeviceMap = new HashMap<String, List<FastServiceDevice>>();
		Map<String,RequisitionNode>        m_onmsRequisitionNodeMap = new HashMap<String, RequisitionNode>();
		
		Set<String> m_duplicatedForeignId  = new HashSet<String>();
		Set<String> m_duplicatedIpaddr     = new HashSet<String>();
		Set<String> m_fastRequisitionNodes = new HashSet<String>();

		public void startJob() {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					Integer jobId = commitJob(job);
					logger.info ("created job with id: " + jobId);
					job.setJobid(jobId);
			        m_fast.setEnabled(false);
			        m_progress.setIndeterminate(true);
			        m_progress.setVisible(true);
			        m_progress.setEnabled(true);
			        
					m_logTable.setContainerDataSource(m_logcontainer);
					m_logTable.setVisible(true);
				}
			});
		}
		
		private void endJob() {
			UI.getCurrent().access(new Runnable() {

				@Override
				public void run() {

					m_progress.setValue(new Float(0.0));
					m_progress.setEnabled(false);
					m_progress.setVisible(false);

					commitJob(job);
					m_fast.setEnabled(true);
					m_panel.setCaption("Fast Integration - Status: Ready");

					// Stop polling
					UI.getCurrent().setPollInterval(-1);
				}
			});

		}
		
		private void endNotStartedJob() {
			UI.getCurrent().access(new Runnable() {

				@Override
				public void run() {

					m_progress.setValue(new Float(0.0));
					m_progress.setEnabled(false);
					m_progress.setVisible(false);

					m_fast.setEnabled(true);
					m_panel.setCaption("Fast Integration - Status: Ready");

					Notification.show("Cannot start job", "Ask Administrator", Type.ERROR_MESSAGE);
					// Stop polling
					UI.getCurrent().setPollInterval(-1);
				}
			});

		}

		@Override
		public void run() {
			job.setUsername(getService().getUser());
			job.setJobdescr("Fast sync: ");
			job.setJobstatus(JobStatus.RUNNING);
			job.setJobstart(new Date());
			try {
				logger.info("run:creating job in jobs table");
				startJob();
				logger.info("run: created job in jobs table");
			} catch (final Exception e) {
				logger.log(Level.SEVERE,"Cannot create job", e);
				endNotStartedJob();
				return;
			}

			try {
				logger.info("run: loading table fastservicedevice");
				checkfastdevices(getService().getFastServiceDeviceContainer().getFastServiceDevices());
				logger.info("run: loaded table fastservicedevice");
				
				logger.info("run: loading table fastservicelink");
				checkfastlinks(getService().getFastServiceLinkContainer().getFastServiceLinks());
				logger.info("run: loaded table fastservicelink");
				
				logger.info("run: loading requisition: " + DashBoardUtils.TN);
				checkRequisition(getService().getOnmsDao().getRequisition(DashBoardUtils.TN));
				logger.info("run: loaded requisition: " + DashBoardUtils.TN);

				logger.info("run: sync Fast devices with Requisition");
				sync();
				logger.info("run: sync Fast devices with Requisition");
			} catch (final ProvisionDashboardException e) {
				logger.log(Level.WARNING,"Failed syncing Fast devices with Requisition", e);
				job.setJobstatus(JobStatus.FAILED);
				job.setJobdescr("FAST sync: Failed syncing Fast devices with Requisition. Error: " + e.getMessage());				
			} catch (final Exception e) {
				logger.log(Level.WARNING,"Failed init check fast integration", e);
				job.setJobstatus(JobStatus.FAILED);
				job.setJobdescr("FAST sync: Failed init check Fast. Error: " + e.getMessage());
			}

			job.setJobstatus(JobStatus.SUCCESS);
			job.setJobdescr("FAST sync: Done");
			job.setJobend(new Date());

			try {
				logger.info("ending job in jobs table");
				endJob();
				logger.info("run: ended job in jobs table");
			} catch (final Exception e) {
				logger.log(Level.WARNING,"Cannot end job in job table", e);
				endNotStartedJob();
			}

		}

		private void sync() throws ProvisionDashboardException {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					m_progress.setIndeterminate(false);
					m_progress.setValue(new Float(current));
				}
			});				
			
			try {
				deleteRequisitionNode();
			} catch (final UniformInterfaceException e) {
				throw new ProvisionDashboardException(e.getResponse());
			}
			
			try {
				updateRequisitionNode();
			} catch (final UniformInterfaceException e) {
				throw new ProvisionDashboardException(e.getResponse());
			}
		}
		
		private void deleteRequisitionNode() {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			for (String reqfastdevice: m_fastRequisitionNodes) {
				if (m_fastServiceDeviceMap.containsKey(reqfastdevice))
					continue;
				boolean delete = true;
				for (String hostname: m_fastServiceDeviceMap.keySet()) {
					if (m_onmsRequisitionNodeMap.get(reqfastdevice).getNodeLabel().
							startsWith(hostname)) {
						delete = false;
						break;
					}
				}
				if ( delete) {
					RequisitionNode delnode = m_onmsRequisitionNodeMap.remove(reqfastdevice);
					getService().deleteNode(DashBoardUtils.TN, delnode);
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(reqfastdevice);
					jloe.setIpaddr("NA");
					jloe.setOrderCode("NA");
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: node deleted");
					jloe.setNote(getNote(delnode));
					logger.info("delete node" + getNote(delnode));
					logs.add(jloe);
					current += 0.01;
					UI.getCurrent().access(new Runnable() {
						@Override
						public void run() {
							m_progress.setValue(new Float(current));
						}
					});
				}
			}
		}
		
		private void updateRequisitionNode() {
			int i = 0;
			int step = m_fastServiceDeviceMap.size()  / 100;
			logger.info("run: step: " + step);
			logger.info("run: size: " + m_fastServiceDeviceMap.size());
			int barrier = step;

			for (String hostname: m_fastServiceDeviceMap.keySet()) {
				String foreignId = getForeignId(hostname);
				
				if (foreignId == null)
					process(hostname, m_fastServiceDeviceMap.get(hostname), null);
				else if (duplicatedForeignId(hostname,foreignId))
					continue;
				else
					process(hostname,m_fastServiceDeviceMap.get(hostname),m_onmsRequisitionNodeMap.get(foreignId));

				i++;
				if (i == barrier) {
					if (current < 0.99)
						current += 0.01;
					UI.getCurrent().access(new Runnable() {
							
						@Override
						public void run() {
							m_progress.setValue(new Float(current));
						}
					});
					barrier += step;
				}
			}
			
		}
		
		private boolean duplicatedForeignId(String foreignid,String hostname) {
			if (m_duplicatedForeignId.contains(foreignid)) {
				for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: skipping service device. Cause: duplicated foreignid in requisition");
				jloe.setNote(getNote(device));
				logger.info("skipping service device. Cause: duplicated foreignid in requisition" + getNote(device));
				UI.getCurrent().access(new Runnable() {
					
					@Override
					public void run() {
							log(jloe);
					}
				});


				}
			}
			return false;
				
		}
		
		private String getForeignId(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			String foreignId = null;
			if (m_onmsRequisitionNodeMap.containsKey(hostname))
				return hostname;
			List<String> duplicatedip = new ArrayList<String>();
			
			for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
				if (m_duplicatedIpaddr.contains(device.getIpaddr())) {
					duplicatedip.add(device.getIpaddr());
				}
			}
				if (duplicatedip.isEmpty()) {
					List<String> hostnames = new ArrayList<String>();
					for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
						if (m_ipForeignIdMap.containsKey(device.getIpaddr()))
							hostnames.add(m_ipForeignIdMap.get(device.getIpaddr()));
					}
					if (hostnames.isEmpty()) {
						process(hostname, m_fastServiceDeviceMap.get(hostname), null);
					} else if ( hostnames.size() == 1 ) {
						process(hostnames.get(0), m_fastServiceDeviceMap.get(hostname), m_onmsRequisitionNodeMap.get(hostnames.get(0)));
					} else {
						for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
							final JobLogEntry jloe = new JobLogEntry();
							jloe.setHostname(device.getHostname());
							jloe.setIpaddr(device.getIpaddr());
							jloe.setOrderCode(device.getOrderCode());
							jloe.setJobid(job.getJobid());
							jloe.setDescription("FAST sync: skipping service device. Cause: duplicated ipaddr in requisition");
							jloe.setNote(getNote(device) + " Duplicated ips: " + duplicatedip);
							logger.info("skipping service device. Cause: duplicated ipaddr in requisition " + getNote(device) + " Duplicated ips: " + duplicatedip);
							logs.add(jloe);
						}															
					}
				} else {
					for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: skipping service device. Cause: duplicated ipaddr in requisition");
						jloe.setNote(getNote(device) + " Duplicated ips: " + duplicatedip);
						logger.info("skipping service device. Cause: duplicated ipaddr in requisition " + getNote(device) + " Duplicated ips: " + duplicatedip);
						logs.add(jloe);
					}							
				}
			}
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					for (JobLogEntry log: logs) {
						log(log);
					}
				}
			});
			
		}
		
		private void checkRequisition(Requisition requisition) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			for (RequisitionNode rnode: requisition.getNodes()) {
				if (m_onmsRequisitionNodeMap.containsKey(rnode.getForeignId())) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(rnode.getForeignId());
					jloe.setIpaddr("NA");
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: Duplicated Foreign Id");
					jloe.setNote(getNote(rnode));
					logs.add(jloe);
					m_duplicatedForeignId.add(rnode.getForeignId());
					logger.info("Duplicated Foreign Id: " + getNote(rnode));
				} else {
					m_onmsRequisitionNodeMap.put(rnode.getForeignId(), rnode);
				}
			}
			
			for (String dupforeignid: m_duplicatedForeignId)
				m_onmsRequisitionNodeMap.remove(dupforeignid);

			for (RequisitionNode rnode: requisition.getNodes()) {
				for (RequisitionInterface riface: rnode.getInterfaces()) {
					if (riface.getIpAddr() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr("NA");
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: Null ip");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("Null ip: " + getNote(rnode) + getNote(riface));
						continue;
					} else if (DashBoardUtils.hasInvalidIp(riface.getIpAddr())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr("NA");
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: Invalid ip");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("Invalid ip: " + getNote(rnode) + getNote(riface));
						continue;
					} else if (riface.getSnmpPrimary() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr(riface.getIpAddr());
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: No Snmp Primary");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("no Snmp Primary: " + getNote(rnode) + getNote(riface));
						continue;
					}
					/* move this to another runnable
					boolean hasIcmp = false;
					boolean hasSnmp = false;
					for (RequisitionMonitoredService rservice: riface.getMonitoredService()) {
						if (rservice.getServiceName().equals("ICMP"))
							hasIcmp = true;
						if (rservice.getServiceName().equals("SNMP"))
							hasSnmp = true;
					}
					if (!hasIcmp) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname("not applicable");
						jloe.setIpaddr("not applicable");
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: No ICMP Service");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("no ICMP Service: " + getNote(rnode) + getNote(riface));												
					}
					if (!hasSnmp) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname("not applicable");
						jloe.setIpaddr("not applicable");
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: No SNMP Service");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("no SNMP Service: " + getNote(rnode) + getNote(riface));												
					}
					*/
					if (m_ipForeignIdMap.containsKey(riface.getIpAddr())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname("not applicable");
						jloe.setIpaddr("not applicable");
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: Duplicated Ip Address");
						jloe.setNote("ForeignIds found: " + m_ipForeignIdMap.get(riface.getIpAddr()) + getNote(rnode) + getNote(riface));
						logs.add(jloe);
						m_duplicatedIpaddr.add(riface.getIpAddr());
						logger.info("ForeignIds found: " + m_ipForeignIdMap.get(riface.getIpAddr()) + getNote(rnode) + getNote(riface));						
					} else {
						m_ipForeignIdMap.put(riface.getIpAddr(), rnode.getForeignId());
					}
					if (riface.getDescr() != null && riface.getDescr().contains("FAST"))
						m_fastRequisitionNodes.add(rnode.getForeignId());
				}
			}
			for (String dupipaddr: m_duplicatedIpaddr)
				m_ipForeignIdMap.remove(dupipaddr);

			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					for (JobLogEntry log: logs) {
						log(log);
					}
				}
			});
		}
  		
		
		private void checkfastdevices(List<FastServiceDevice> devices) {
			Map<String,Set<String>> iptohostnamemap = new HashMap<String, Set<String>>();
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			for (FastServiceDevice device:devices) {
				
				if (device.getHostname() != null && device.getIpaddr() != null ) {
					if (DashBoardUtils.hasInvalidIp(device.getIpaddr())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: skipping service device. Cause: invalid ip");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: invalid ip. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else {
						if (!m_fastServiceDeviceMap.containsKey(device.getHostname().toLowerCase())) {
							m_fastServiceDeviceMap.put(device.getHostname().toLowerCase(), new ArrayList<FastServiceDevice>());
						}
						if (!iptohostnamemap.containsKey(device.getIpaddr())) {
							iptohostnamemap.put(device.getIpaddr(), new HashSet<String>());
						}
						m_fastServiceDeviceMap.get(device.getHostname().toLowerCase()).add(device);
						iptohostnamemap.get(device.getIpaddr()).add(device.getHostname());
						logger.info("Adding service device. hostname:  " + device.getHostname() + " ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					}
				} else if (device.getHostname() == null && device.getIpaddr() == null ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname("null");
					jloe.setIpaddr("null");
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null hostname and null ip address");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null hostname and null ip address. order_code: " +  device.getOrderCode() + " " +getNote(device));
				} else if (device.getHostname() != null && device.getIpaddr() == null) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(device.getHostname());
					jloe.setIpaddr("null");
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null ip address");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null ip address. hostname: " + device.getHostname() + " order_code: " +  device.getOrderCode() + " " + getNote(device));
				} else if (device.getHostname() == null && device.getIpaddr() != null ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname("null");
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null hostname");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null hostname. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
				} 	
			}
			
			for (String ipaddr: iptohostnamemap.keySet()) {
				if (iptohostnamemap.get(ipaddr).size() > 1) {
					for (String hostname: iptohostnamemap.get(ipaddr)) {
						for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
							if (device.getIpaddr().equals(ipaddr)) {
							final JobLogEntry jloe = new JobLogEntry();
							jloe.setHostname(hostname);
							jloe.setIpaddr(ipaddr);
							jloe.setOrderCode(device.getOrderCode());
							jloe.setJobid(job.getJobid());
							jloe.setDescription("FAST sync: Same ip found on different hostnames: " + iptohostnamemap.get(ipaddr));
							jloe.setNote(getNote(device));
							logs.add(jloe);
							logger.info("Same ip found on different hostnames: " + iptohostnamemap.get(ipaddr) + " hostname:  " + device.getHostname() + " ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
							}
						}
					}
				}
			}
			
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					for (JobLogEntry log: logs) {
						log(log);
					}
				}
			});
		}
		
		private void checkfastlinks(List<FastServiceLink> links) {
			for (FastServiceLink link:links) {
				if (!m_fastServiceLinkMap.containsKey(link.getOrderCode()))
					m_fastServiceLinkMap.put(link.getOrderCode(), new ArrayList<FastServiceLink>());
				m_fastServiceLinkMap.get(link.getOrderCode()).add(link);
			}
		}

		private String getNote(RequisitionNode rnode) {
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
		
		private String getNote(RequisitionInterface riface) {
			StringBuffer deviceNote=new StringBuffer("Notes:");
			if (riface.getIpAddr() != null) {
				deviceNote.append(" ipaddr: ");
				deviceNote.append(riface.getIpAddr());
			}

			if (riface.getDescr() != null) {
				deviceNote.append(" description: ");
				deviceNote.append(riface.getDescr());
			}
			
			if (riface.getSnmpPrimary() != null) {
				deviceNote.append(" snmpPrimary: ");
				deviceNote.append(riface.getSnmpPrimary().getCharCode());
				
			}
			return deviceNote.toString();
		
		}

		private String getNote(FastServiceDevice device) {
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

		private void process(String hostname, List<FastServiceDevice> devices, RequisitionNode rnode) {
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (FastServiceDevice device : devices) {
				logger.info("process: processing hostname: " + hostname
						+ " ip: " + device.getIpaddr() +  " order code: " + device.getOrderCode() + getNote(device));
				if (device.isNotmonitoring())
					continue;
			}
			
			if (rnode != null) {
				logger.info("process: found requisition node: "
						//+ rnodes.getForeignId() + " nodelabel: "
						//+ rnodes.getNodeLabel()
						);
			} else {
				logger.info("process: not found on requisition hostname: "
						+ hostname);
			}
/*
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(hostname);
			jloe.setIpaddr(device.getIpaddr());
			jloe.setOrderCode(device.getOrderCode());
			UI.getCurrent().access(new Runnable() {

				@Override
				public void run() {
					log(jloe);
				}
			});
*/

		}
		
		public int commitJob(Job job) {
			if (job.getJobid() == null)
				m_jobcontainer.add(job);
			else
				m_jobcontainer.save(new RowId(new Object[]{job.getJobid()}), job);
			try {
				m_jobcontainer.commit();;
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (java.lang.IllegalStateException e) {
				e.printStackTrace();
			}
			
			return m_jobcontainer.getLastJobId().getValue();

		}
		
		public void log(JobLogEntry jLogE) {
			jLogE.setJobid(job.getJobid());
			m_logcontainer.addBean(jLogE);
		}

	}

}
