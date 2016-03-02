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

import org.opennms.netmgt.model.PrimaryType;
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
import org.opennms.vaadin.provision.model.Vrf;

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

	abstract class FastTabAbstractRunnable {
		volatile double current = 0.0;
	    volatile Job job = new Job();
		volatile BeanItemContainer<JobLogEntry> m_logcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);

		public void startJob() {
			Integer jobId = commitJob(job);
			logger.info ("created job with id: " + jobId);
			job.setJobid(jobId);
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
			        m_fast.setEnabled(false);
			        m_progress.setIndeterminate(true);
			        m_progress.setVisible(true);
			        m_progress.setEnabled(true);
			        
					m_logTable.setContainerDataSource(m_logcontainer);
					m_logTable.setVisible(true);
				}
			});
		}
		
		public void endJob() {
			commitJob(job);
			UI.getCurrent().access(new Runnable() {

				@Override
				public void run() {

					m_progress.setValue(new Float(0.0));
					m_progress.setEnabled(false);
					m_progress.setVisible(false);

					m_fast.setEnabled(true);
					m_panel.setCaption("Fast Integration - Status: Ready");

					// Stop polling
					UI.getCurrent().setPollInterval(-1);
				}
			});

		}
		
		public void endNotStartedJob() {
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

		public String getNote(RequisitionNode rnode) {
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
		
		@SuppressWarnings("deprecation")
		public String getNote(RequisitionInterface riface) {
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

	}

	class FastIntegrationRunnable  extends FastTabAbstractRunnable implements Runnable{

		Map<String, FastServiceLink>           m_fastServiceLinkMap = new HashMap<String, FastServiceLink>();
		
		Map<String, List<FastServiceDevice>> m_fastServiceDeviceMap = new HashMap<String, List<FastServiceDevice>>();
		Map<String,String>                      m_fastIpHostnameMap = new HashMap<String,String>();
		Map<String, Set<String>>     m_fastDuplicatedIpHostnamesMap = new HashMap<String, Set<String>>();

		Map<String,RequisitionNode>        m_onmsRequisitionNodeMap = new HashMap<String, RequisitionNode>();
		Map<String,String>                     m_onmsIpForeignIdMap = new HashMap<String, String>();
		Map<String, Set<String>>     m_onmsDuplicatedIpForeignIdMap = new HashMap<String, Set<String>>();

		Set<String> m_onmsDuplicatedForeignId  = new HashSet<String>();
		
		Map<String,Vrf> m_vrf;


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
				logger.info("run: loading table vrf");
				m_vrf = getService().getVrfContainer().getVrfMap();
				logger.info("run: loaded table vrf");

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
				job.setJobstatus(JobStatus.SUCCESS);
				job.setJobdescr("FAST sync: Done");
			} catch (final ProvisionDashboardException e) {
				logger.log(Level.WARNING,"Failed syncing Fast devices with Requisition", e);
				job.setJobstatus(JobStatus.FAILED);
				job.setJobdescr("FAST sync: Failed syncing Fast devices with Requisition. Error: " + e.getMessage());				
			} catch (final Exception e) {
				logger.log(Level.WARNING,"Failed init check fast integration", e);
				job.setJobstatus(JobStatus.FAILED);
				job.setJobdescr("FAST sync: Failed init check Fast. Error: " + e.getMessage());
			}

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

			int i = 0;
			int step = (4*m_fastServiceDeviceMap.size() + m_onmsRequisitionNodeMap.size()) / 100;
			logger.info("run: step: " + step);
			logger.info("run: size: " + m_fastServiceDeviceMap.size());
			int barrier = step;

			try {

				for (String hostname: m_fastServiceDeviceMap.keySet()) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (!checkduplicatedhostname(hostname) && !checkduplicatedipaddress(hostname))
						process(hostname);
					i=i+4;
					if (i >= barrier) {
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

				for (String foreignId: m_onmsRequisitionNodeMap.keySet()) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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

					
					if (isDeviceInFast(foreignId))
						continue;
					RequisitionNode rnode = m_onmsRequisitionNodeMap.get(foreignId);
					if (isDeletedFastDevice(rnode)) {
						deleteNode(foreignId, rnode);
						continue;
					}
					for (RequisitionInterface riface: rnode.getInterfaces()) {
						if (riface.getDescr().contains("FAST") && !m_fastIpHostnameMap.containsKey(riface.getIpAddr()) && !m_fastDuplicatedIpHostnamesMap.containsKey(riface.getIpAddr())) {
							deleteInterface(foreignId, rnode, riface.getIpAddr());
						}
					}
				}

			} catch (final UniformInterfaceException e) {
				throw new ProvisionDashboardException(e.getResponse());
			} 
			
		}
		
						
		private void checkRequisition(Requisition requisition) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			for (RequisitionNode rnode: requisition.getNodes()) {
				if (m_onmsRequisitionNodeMap.containsKey(rnode.getForeignId())) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(rnode.getForeignId());
					jloe.setIpaddr("NA");
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: Duplicated Foreign Id in Requisition");
					jloe.setNote(getNote(rnode));
					logs.add(jloe);
					m_onmsDuplicatedForeignId.add(rnode.getForeignId());
					logger.info("Duplicated Foreign Id: " + getNote(rnode));
				} else {
					m_onmsRequisitionNodeMap.put(rnode.getForeignId(), rnode);
				}
			}
			
			for (String dupforeignid: m_onmsDuplicatedForeignId) {
				RequisitionNode rnode = m_onmsRequisitionNodeMap.remove(dupforeignid);
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(rnode.getForeignId());
				jloe.setIpaddr("NA");
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: Duplicated Foreign Id in Requisition");
				jloe.setNote(getNote(rnode));
				logs.add(jloe);
			}

			for (RequisitionNode rnode: requisition.getNodes()) {
				for (RequisitionInterface riface: rnode.getInterfaces()) {
					if (riface.getIpAddr() == null) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr("NA");
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: Null Ip Address in Requisition");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("Null ip: " + getNote(rnode) + getNote(riface));
						continue;
					} else if (DashBoardUtils.hasInvalidIp(riface.getIpAddr())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr(riface.getIpAddr());
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: Invalid Ip Address in Requisition");
						jloe.setNote(getNote(rnode) + getNote(riface));
						logs.add(jloe);
						logger.info("Invalid ip: " + getNote(rnode) + getNote(riface));
						continue;
					}
					if (m_onmsIpForeignIdMap.containsKey(riface.getIpAddr())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(rnode.getForeignId());
						jloe.setIpaddr(riface.getIpAddr());
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: Duplicated Ip Address in Requisition");
						jloe.setNote("ForeignIds found: " + m_onmsIpForeignIdMap.get(riface.getIpAddr()) + getNote(rnode) + getNote(riface));
						logs.add(jloe);
						if (!m_onmsDuplicatedIpForeignIdMap.containsKey(riface.getIpAddr()))
							m_onmsDuplicatedIpForeignIdMap.put(riface.getIpAddr(), new HashSet<String>());
						m_onmsDuplicatedIpForeignIdMap.get(riface.getIpAddr()).add(rnode.getForeignId());
						logger.info("duplicated Address found: " + m_onmsIpForeignIdMap.get(riface.getIpAddr()) + getNote(rnode) + getNote(riface));						
					} else {
						m_onmsIpForeignIdMap.put(riface.getIpAddr(), rnode.getForeignId());
					}
				}
			}
			for (String dupipaddr: m_onmsDuplicatedIpForeignIdMap.keySet()) {
				String foreignId = m_onmsIpForeignIdMap.remove(dupipaddr);
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(foreignId);
				jloe.setIpaddr(dupipaddr);
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: Duplicated Ip Address in Requisition");
				jloe.setNote("ForeignIds found: " + foreignId);
				logs.add(jloe);
				m_onmsDuplicatedIpForeignIdMap.get(dupipaddr).add(foreignId);
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
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Ip Address");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: invalid ip. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else if (DashBoardUtils.hasInvalidDnsBind9Label(device.getHostname())) {
						final JobLogEntry jloe = new JobLogEntry();
						jloe.setHostname(device.getHostname());
						jloe.setIpaddr(device.getIpaddr());
						jloe.setOrderCode(device.getOrderCode());
						jloe.setJobid(job.getJobid());
						jloe.setDescription("FAST sync: skipping FAST device. Cause: Invalid Hostname");
						jloe.setNote(getNote(device));
						logs.add(jloe);
						logger.info("Skipping service device. Cause: invalid hostname. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
					} else {
						logger.info("Adding service device. hostname:  " + device.getHostname() + " ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
						if (!m_fastServiceDeviceMap.containsKey(device.getHostname().toLowerCase())) {
							m_fastServiceDeviceMap.put(device.getHostname().toLowerCase(), new ArrayList<FastServiceDevice>());
						}
						m_fastServiceDeviceMap.get(device.getHostname().toLowerCase()).add(device);
						
						if (m_fastIpHostnameMap.containsKey(device.getIpaddr()) && !m_fastIpHostnameMap.get(device.getIpaddr()).equals(device.getHostname().toLowerCase())) {
								logger.info("not adding to ip map. duplicated ip: " + device.getIpaddr() + " hostname:" + device.getHostname().toLowerCase());
								if (!m_fastDuplicatedIpHostnamesMap.containsKey(device.getIpaddr()))
									m_fastDuplicatedIpHostnamesMap.put(device.getIpaddr(), new HashSet<String>());
								m_fastDuplicatedIpHostnamesMap.get(device.getIpaddr()).add(device.getHostname().toLowerCase());
						} else {
							m_fastIpHostnameMap.put(device.getIpaddr(), device.getHostname().toLowerCase());
							logger.info("Adding to ip map. hostname:  " + device.getHostname() + " ipaddr: " + device.getIpaddr());
						}						
					}
				} else if (device.getHostname() == null && device.getIpaddr() == null ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname("NA");
					jloe.setIpaddr("NA");
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null hostname and null ip address");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null hostname and null ip address. order_code: " +  device.getOrderCode() + " " +getNote(device));
				} else if (device.getHostname() != null && device.getIpaddr() == null) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(device.getHostname());
					jloe.setIpaddr("NA");
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null ip address");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null ip address. hostname: " + device.getHostname() + " order_code: " +  device.getOrderCode() + " " + getNote(device));
				} else if (device.getHostname() == null && device.getIpaddr() != null ) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname("NA");
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: null hostname");
					jloe.setNote(getNote(device));
					logs.add(jloe);
					logger.info("Skipping service device. Cause: null hostname. ipaddr: " + device.getIpaddr() + " order_code: " +  device.getOrderCode() + " " +getNote(device));
				} 	
			}
			
			for (String ipaddr: m_fastDuplicatedIpHostnamesMap.keySet()) {
				String hostname = m_fastIpHostnameMap.remove(ipaddr);
				logger.info("cleaning ip map: duplicated ip: " + ipaddr + " hostname:" + hostname);
				m_fastDuplicatedIpHostnamesMap.get(ipaddr).add(hostname);
			}

			for (String ipaddr: m_fastDuplicatedIpHostnamesMap.keySet()) {
				Set<String> hostnames =m_fastDuplicatedIpHostnamesMap.get(ipaddr); 
				logger.info("ipaddr: " + ipaddr + " duplicated hostnames: " + hostnames);
				for (String hostname: hostnames) {
					for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
						if (device.getIpaddr().equals(ipaddr)) {
							final JobLogEntry jloe = new JobLogEntry();
							jloe.setHostname(hostname);
							jloe.setIpaddr(ipaddr);
							jloe.setOrderCode(device.getOrderCode());
							jloe.setJobid(job.getJobid());
							jloe.setDescription("FAST sync: Same ip found on different hostnames");
							jloe.setNote("hostnames:" + hostnames + getNote(device));
							logs.add(jloe);
							logger.info("ip address found on different hostnames: " + hostnames +  getNote(device));
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
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			final Set<String> duplicatedEntry = new HashSet<String>();
			for (FastServiceLink link:links) {
				if (link.getOrderCode() == null) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(link.getDeliveryDeviceClientSide());
					jloe.setIpaddr("NA");
					jloe.setOrderCode("NA");
					jloe.setJobid(job.getJobid());
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
					jloe.setJobid(job.getJobid());
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
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service link. Cause: invalid Vrf");
					jloe.setNote(getNote(link));
					logs.add(jloe);
					logger.info("skipping service link. Cause: invalid Vrf: "  +  getNote(link));
					continue;
				}

				if (m_fastServiceLinkMap.containsKey(link.getOrderCode())) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(link.getDeliveryDeviceClientSide());
					jloe.setIpaddr("NA");
					jloe.setOrderCode(link.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service link. Cause: duplicated order_code");
					jloe.setNote(getNote(link));
					logs.add(jloe);
					logger.info("skipping service link. Cause: duplicated order_code: "  +  getNote(link));
					duplicatedEntry.add(link.getOrderCode());
					continue;
					
				}
				m_fastServiceLinkMap.put(link.getOrderCode(), link);
			}
			for (String duplioc : duplicatedEntry ) {
				FastServiceLink link = m_fastServiceLinkMap.remove(duplioc);
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(link.getDeliveryDeviceClientSide());
				jloe.setIpaddr("NA");
				jloe.setOrderCode(link.getOrderCode());
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: skipping service link. Cause: duplicated order_code");
				jloe.setNote(getNote(link));
				logs.add(jloe);
				logger.info("skipping service link. Cause: duplicated order_code: "  +  getNote(link));
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


		private boolean checkduplicatedhostname(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			if (m_onmsDuplicatedForeignId.contains(hostname)) {
				for (FastServiceDevice device : m_fastServiceDeviceMap
						.get(hostname)) {
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(hostname);
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: duplicated foreign id in requisition");
					jloe.setNote("Hostname correspond to a duplicated foreignid: " + hostname + getNote(device));
					logger.info("skipping service device. Cause: duplicated foreignid in requisition: " + hostname + getNote(device));
					logs.add(jloe);
				}
				UI.getCurrent().access(new Runnable() {
					
					@Override
					public void run() {
						for (JobLogEntry log: logs) {
							log(log);
						}
					}
				});
				
				return true;
			} 
			return false;
		}
		
		private boolean checkduplicatedipaddress(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			boolean duplicated = false;
			for (FastServiceDevice device : m_fastServiceDeviceMap
					.get(hostname)) {
				String ipaddr = device.getIpaddr();
				if (m_onmsDuplicatedIpForeignIdMap.containsKey(ipaddr)) {
					duplicated = true;
					final JobLogEntry jloe = new JobLogEntry();
					jloe.setHostname(device.getHostname());
					jloe.setIpaddr(device.getIpaddr());
					jloe.setOrderCode(device.getOrderCode());
					jloe.setJobid(job.getJobid());
					jloe.setDescription("FAST sync: skipping service device. Cause: duplicated ipaddr in requisition");
					jloe.setNote(getNote(device) + " Duplicated ips: " + ipaddr);
					logger.info("skipping service device. Cause: duplicated ipaddr in requisition " + getNote(device) + " Duplicated ips: " + ipaddr);
					logs.add(jloe);
				}
			}
			if (duplicated) {
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					for (JobLogEntry log: logs) {
						log(log);
					}
				}
			});
			}
			return duplicated;
		}
		
		private void mismatch(String hostname, Set<String> foreignIds) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();

			for (FastServiceDevice device : m_fastServiceDeviceMap
					.get(hostname)) {
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: skipping service device. Cause: Mismatch hostname/foreignIds");
				jloe.setNote("hostname/foreignIds" + hostname + "/"+ foreignIds + getNote(device)  );
				logger.info("skipping service device. Cause: Mismatch hostname/foreignIds" + hostname + "/"
						+ foreignIds);
				logs.add(jloe);
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

		private void process(String hostname) {

			Set<String> foreignIds = new HashSet<String>();
			for (FastServiceDevice device : m_fastServiceDeviceMap
					.get(hostname)) {
				String ipaddr = device.getIpaddr();
				if (m_onmsIpForeignIdMap.containsKey(ipaddr)) {
					foreignIds.add( m_onmsIpForeignIdMap.get(ipaddr));
				} else if (m_onmsRequisitionNodeMap.containsKey(hostname)) {
					foreignIds.add(hostname);
				} else {
					for (RequisitionNode rnode : m_onmsRequisitionNodeMap
							.values()) {
						if (rnode.getNodeLabel().startsWith(hostname)) {
							foreignIds.add(rnode.getForeignId());
							break;
						}
					}
				}
				if (foreignIds.size() == 0) {
					add(hostname);
				} else if (foreignIds.size() == 1) {
					update(hostname, foreignIds.iterator().next());
				} else {
					mismatch(hostname, foreignIds);
				}
			}
		}
		
		private void norefdevice(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();

			for (FastServiceDevice device : m_fastServiceDeviceMap
					.get(hostname)) {
				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: skipping add service device. Cause: No Valid ref device for order_code");
				jloe.setNote(getNote(device));
				logger.info("skipping service add device. Cause: No Valid ref device for order_code " + getNote(device));
				logs.add(jloe);
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
		private void add(String hostname) {
			final List<JobLogEntry> logs = new ArrayList<JobLogEntry>();
			Set<String> secondary = new HashSet<String>(); 
			FastServiceDevice refdevice = null;
			FastServiceLink reflink = null;
			for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
				secondary.add(device.getIpaddr());
				if (!m_fastServiceLinkMap.containsKey(device.getOrderCode()))
					continue;
				if (refdevice == null ) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());
				} else 	if (device.isMaster() && !refdevice.isMaster()) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());
				} else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());						
				} else if (device.getIpAddrLan() != null && device.getIpAddrLan() == null) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());						
				}

				final JobLogEntry jloe = new JobLogEntry();
				jloe.setHostname(device.getHostname());
				jloe.setIpaddr(device.getIpaddr());
				jloe.setOrderCode(device.getOrderCode());
				jloe.setJobid(job.getJobid());
				jloe.setDescription("FAST sync: added service device.");
				jloe.setNote(getNote(device));
				logs.add(jloe);

			}

			if (refdevice == null) {
				norefdevice(hostname);
				return;
			}

			secondary.remove(refdevice.getIpaddr());
			getService().addNode(DashBoardUtils.TN, hostname,refdevice,reflink,m_vrf.get(reflink.getVrf()),secondary);
			
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					for (JobLogEntry log: logs) {
						log(log);
					}
				}
			});

		}
				
		@SuppressWarnings("deprecation")
		private void update(String hostname, String foreingId) {
			RequisitionNode rnode = m_onmsRequisitionNodeMap.get(foreingId);
			Set<String> secondary = new HashSet<String>(); 
			FastServiceDevice refdevice = null;
			FastServiceLink reflink = null;
			for (FastServiceDevice device: m_fastServiceDeviceMap.get(hostname)) {
				secondary.add(device.getIpaddr());
				if (!m_fastServiceLinkMap.containsKey(device.getOrderCode()))
					continue;
				if (refdevice == null ) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());
				} else if (rnode.getInterface(device.getIpaddr()) != null && rnode.getInterface(device.getIpaddr()).getSnmpPrimary().equals(PrimaryType.PRIMARY)) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());
				} else 	if (device.isMaster() && !refdevice.isMaster()) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());
				} else if (device.isSaveconfig() && !refdevice.isSaveconfig()) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());						
				} else if (device.getIpAddrLan() != null && device.getIpAddrLan() == null) {
					refdevice= device;
					reflink= m_fastServiceLinkMap.get(device.getOrderCode());						
				}


			}

			if (refdevice == null) {
				norefdevice(hostname);
				return;
			}

			secondary.remove(refdevice.getIpaddr());
			//FIXME
			//getService().updateNode(DashBoardUtils.TN, hostname,refdevice,reflink,m_vrf.get(reflink.getVrf()),secondary,rnode);
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(refdevice.getHostname());
			jloe.setIpaddr(refdevice.getIpaddr());
			jloe.setOrderCode(refdevice.getOrderCode());
			jloe.setJobid(job.getJobid());
			jloe.setDescription("FAST sync: updated service device.");
			jloe.setNote(getNote(refdevice)+ getNote(reflink));
			
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					log(jloe);
				}
			});

		}

		private boolean isDeviceInFast(String foreignId) {
			if (m_fastServiceDeviceMap.containsKey(foreignId))
				return true;
			for (String hostname: m_fastServiceDeviceMap.keySet()) {
				if (m_onmsRequisitionNodeMap.get(foreignId).getNodeLabel().
						startsWith(hostname)) {
					return true;
				}
			}
			return false;
		}
		
		private boolean isDeletedFastDevice(RequisitionNode rnode) {
			if (rnode.getCategory(DashBoardUtils.m_network_levels[0]) != null)
				return false;
			if (rnode.getCategory(DashBoardUtils.m_network_levels[1]) != null)
				return false;
			if (rnode.getCategory(DashBoardUtils.m_network_levels[2]) != null) {
				for (RequisitionInterface riface: rnode.getInterfaces()) {
					if (riface.getDescr().contains("FAST"))
						continue;
					return false;
				}
			}				
			return true;
		}
		
		private void deleteNode(String foreignId, RequisitionNode rnode) {
			getService().deleteNode(DashBoardUtils.TN, rnode);
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(foreignId);
			jloe.setIpaddr("NA");
			jloe.setOrderCode("NA");
			jloe.setJobid(job.getJobid());
			jloe.setDescription("FAST sync: node deleted");
			jloe.setNote(getNote(rnode));
			logger.info("delete node" + getNote(rnode));
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
						log(jloe);
				}
			});
			
		}
		
		private void deleteInterface(String foreignId, RequisitionNode rnode, String ipaddr) {
			getService().deleteInterface(DashBoardUtils.TN, foreignId, ipaddr);
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(foreignId);
			jloe.setIpaddr(ipaddr);
			jloe.setOrderCode("NA");
			jloe.setJobid(job.getJobid());
			jloe.setDescription("FAST sync: interface deleted");
			jloe.setNote(getNote(rnode));
			logger.info("delete interface node" + getNote(rnode));
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
						log(jloe);
				}
			});
			
		}
 		
	}

}
