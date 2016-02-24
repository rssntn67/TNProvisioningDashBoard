package org.opennms.vaadin.provision.dashboard;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.model.FastServiceDevice;
import org.opennms.vaadin.provision.model.FastServiceLink;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.JobLogEntry;
import org.opennms.vaadin.provision.model.Job.JobStatus;

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
	        
	        m_progress.setIndeterminate(true);
	        m_progress.setVisible(true);
	        m_progress.setEnabled(true);
	        
	        m_fast.setEnabled(false);
	        
	        FastIntegrationRunnable runnable = new FastIntegrationRunnable();
	        Thread thread = new Thread(runnable);
	        thread.start();		
	        
	        m_panel.setCaption("Fast Integration - Status: Running");
	        Notification.show("Fast Integration - Status: Started", Type.HUMANIZED_MESSAGE);
		}
	}
			
	class FastIntegrationRunnable implements Runnable {

		volatile double current = 0.0;
	    volatile Job job;
		volatile BeanItemContainer<JobLogEntry> m_logcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);

		@Override
		public void run() {
			job = new Job();
			job.setUsername(getService().getUser());
			job.setJobdescr("Fast sync");
			job.setJobstatus(JobStatus.RUNNING);
			job.setJobstart(new Date());


			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					Integer jobId = commitJob(job);
					logger.info ("runfast: created job with id: " + jobId);
					job.setJobid(jobId);
				}
			});
					
			List<FastServiceDevice> devices = new ArrayList<FastServiceDevice>();
			Requisition tn;
			List<FastServiceLink> links; 
			try {
				logger.info("run: loading table fastservicedevice");
				devices = getService().getFastServiceDeviceContainer().getFastServiceDevices();
				logger.info("run: loaded table fastservicedevice");
				
				logger.info("run: loading table fastservicelink");
				links = getService().getFastServiceLinkContainer().getFastServiceLinks();
				logger.info("run: loaded table fastservicelink");
				
				logger.info("run: loading requisition: " + DashBoardUtils.TN);
				tn = getService().getOnmsDao().getRequisition(DashBoardUtils.TN);
				logger.info("run: loaded requisition: " + DashBoardUtils.TN);
				
				UI.getCurrent().access(new Runnable() {
					@Override
					public void run() {
						m_progress.setIndeterminate(false);
						m_progress.setValue(new Float(current));
					}
				});

				// first operation remove from devices the devices with hostname null 
				// group devices by entries
				Map<String,RequisitionNode> rnmap = new HashMap<String, RequisitionNode>();
				for (RequisitionNode rnode: tn.getNodes()) {
					rnmap.put(rnode.getForeignId(), rnode);
				}
				
				Map<String, List<FastServiceDevice>> fldhostmap = new HashMap<String, List<FastServiceDevice>>();
				Map<String, List<FastServiceDevice>> fldipmap   = new HashMap<String, List<FastServiceDevice>>();
				List<FastServiceDevice> devicewithouthostname = new ArrayList<FastServiceDevice>();
				List<FastServiceDevice> devicewithoutipaddr = new ArrayList<FastServiceDevice>();
				
				int i = 0;
				int step = devices.size()  / 100;
				logger.info("run: step: " + step);
				logger.info("run: size: " + devices.size());
				int barrier = step;
				for (FastServiceDevice device:devices) {
					process(device, rnmap);
					i++;
					if (i == barrier) {
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

				UI.getCurrent().access(new Runnable() {
					
					@Override
					public void run() {
						runned(JobStatus.SUCCESS, "FAST sync: Done");
					}
				});
			} catch (final Exception e) {
				logger.log(Level.WARNING,"Failed running fast integration", e);
				UI.getCurrent().access(new Runnable() {
					@Override
					public void run() {
						runned(JobStatus.FAILED, "FAST sync: Failed: " + e.getMessage());
					}
				});
			}
		}
				
		private void runned(JobStatus status, String jobdescr) {
			m_progress.setValue(new Float(0.0));
	        m_progress.setEnabled(false);
	        m_progress.setVisible(false);
	        
	        job.setJobstatus(status);
			job.setJobend(new Date());
			job.setJobdescr(jobdescr);
			commitJob(job);
	                
	        
			m_logTable.setContainerDataSource(m_logcontainer);
			m_logTable.setVisible(true);

	        m_fast.setEnabled(true);
	        m_panel.setCaption("Fast Integration - Status: Ready");
	        
	        // Stop polling
	        UI.getCurrent().setPollInterval(-1);

		}

		private void process(FastServiceDevice device, Map<String,RequisitionNode> rnmap) {
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			logger.info("process: processing hostname: " + device.getHostname() + " ip: " + device.getIpaddr());
			final JobLogEntry jloe = new JobLogEntry();
			jloe.setHostname(device.getHostname());
			jloe.setIpaddr(device.getIpaddr());
			jloe.setOrderCode(device.getOrderCode());
			if (device.getHostname() != null) {
				if (rnmap.containsKey(device.getHostname())) {
					//RequisitionNode rnode = rnmap.get(device.getHostname());
					jloe.setDescription("FAST sync: label found on requisition TrentinoNetwork.");
//					logger.info("process: found requisition node: " + rnode.getForeignId() + " nodelabel: " + rnode.getNodeLabel());
				} else {
					jloe.setDescription("FAST sync: hostname not found on requisition TrentinoNetwork");
//					logger.info("process: not found on requisition hostname: " + device.getHostname() + " ip: " + device.getIpaddr());
				}
			} else { 
				jloe.setDescription("FAST sync: null hostname");
//				logger.info("process: hostname is null for order code: " + device.getOrderCode() + " ip: " + device.getIpaddr());
			}
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					log(jloe);
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

	}

}
