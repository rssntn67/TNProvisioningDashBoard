package org.opennms.vaadin.provision.dashboard;



import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.dao.JobLogDao;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.JobLogEntry;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Title("TNPD - Fast Integration")
@Theme("runo")
public class FastTab extends DashboardTab {

	private class JobLogFilter implements Filter {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1156479792519150329L;
		private String noodle;
		
		public JobLogFilter(Object obj) {
			if (obj != null)
				noodle = (String) obj;
		}
		
		@Override
		public boolean passesFilter(Object itemId, Item item)
				throws UnsupportedOperationException {
			@SuppressWarnings("unchecked")
			JobLogEntry logentry=((BeanItem<JobLogEntry>)item).getBean();
			if (logentry.getDescription() != null && logentry.getDescription().contains(noodle))
				return true;
			if (logentry.getHostname() != null && logentry.getHostname().contains(noodle))
				return true;
			if (logentry.getIpaddr()!= null && logentry.getIpaddr().contains(noodle))
				return true;
			if (logentry.getOrderCode() != null && logentry.getOrderCode().contains(noodle))
				return true;
			if (logentry.getNote() != null && logentry.getNote().contains(noodle))
				return true;
			return false;
		}

		@Override
		public boolean appliesToProperty(Object propertyId) {
			return true;
		}
		
	}

	private static final Logger logger = Logger.getLogger(DashboardTab.class.getName());

	private Panel m_panel  = new Panel();
    private Button m_fast = new Button("Start Fast Integration");
	private Button m_syncfast  = new Button("Sync Fast");

    final ProgressBar m_progress = new ProgressBar();

    private JobDao m_jobdao;
    private JobLogDao m_joblogdao;
    private boolean m_loaded = false;
	private TextField m_searchField       = new TextField("Search Job Logs Text");
	private FastTabRunnable m_runnable;
    private Table m_jobTable =  new Table();
    private Table m_logTable =  new Table();
	private Label m_loginfo = new Label();
	
	private Item m_selectLog=null;
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	@Override
	public Map<String, Collection<BasicNode>> getUpdatesMap() {
		Map<String,Collection<BasicNode>> updatesmap = new HashMap<String, Collection<BasicNode>>();
		if (m_runnable != null && !m_runnable.getUpdates().isEmpty())
			updatesmap.put(DashBoardUtils.TN_REQU_NAME, m_runnable.getUpdates());
		return updatesmap;
	}

	@Override
	public void resetUpdateMap() {
		if (m_runnable != null )
			m_runnable.resetUpdateMap();
	}
	
	public FastTab() {
		super();
		
		VerticalLayout searchlayout = new VerticalLayout();
		
		m_searchField.setWidth("80%");
		
		searchlayout.addComponent(m_searchField);
		searchlayout.setWidth("100%");
		searchlayout.setMargin(true);

		m_searchField.setInputPrompt("Search Text");
		m_searchField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		m_searchField.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("unchecked")
			public void textChange(final TextChangeEvent event) {
				if (m_logTable.getContainerDataSource() == null)
					return;
				((BeanItemContainer<JobLogEntry>)m_logTable.getContainerDataSource()).removeAllContainerFilters();
				((BeanItemContainer<JobLogEntry>)m_logTable.getContainerDataSource()).addContainerFilter(
						new JobLogFilter(event.getText()));
			}
		});

	    m_jobTable.setSelectable(true);
	    m_jobTable.setImmediate(true);
	    m_jobTable.addItemClickListener(new ItemClickListener() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void itemClick(ItemClickEvent event) {
				if (getService().isFastRunning()) {
					return;
				}
				BeanItemContainer<JobLogEntry> joblogcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);
				Integer oldjobId = -1;
				if (m_selectLog != null) {
					oldjobId = (Integer)m_selectLog.getItemProperty("jobid").getValue();
				}
				Integer jobid = (Integer)event.getItem().getItemProperty("jobid").getValue();
				if (oldjobId.intValue() == jobid.intValue()) {
					jobid = -1;
					m_selectLog = null;
					logger.info ("un selected job with id: " + jobid);
					m_loginfo.setCaption("");
				} else {
					m_selectLog = event.getItem();
					m_loginfo.setCaption("Description for Job: " + "'"+jobid+"': " + event.getItem().getItemProperty("jobdescr").getValue());
					for (JobLogEntry jlog: m_joblogdao.getJoblogs(jobid))
						joblogcontainer.addBean(jlog);
					logger.info ("selected job with id: " + jobid);
				}
				m_logTable.setContainerDataSource(joblogcontainer);
				m_logTable.setSizeFull();
				m_logTable.setVisibleColumns(new Object[] {"hostname","ipaddr","orderCode","description","note"});
				m_logTable.setVisible(true);
			}
		});

        m_progress.setEnabled(false);
	    m_progress.setVisible(false);
        m_logTable.setVisible(false);
		m_fast.addClickListener(this);
		m_syncfast.addClickListener(this);

		getRightHead().addComponent(m_fast);
		getRightHead().setComponentAlignment(m_fast, Alignment.MIDDLE_LEFT);
		getRightHead().addComponent(m_syncfast);
		getRightHead().setComponentAlignment(m_syncfast, Alignment.MIDDLE_RIGHT);

		getLeft().addComponent(new Panel("Log Search",searchlayout));
		getLeft().addComponent(new Panel("Jobs",m_jobTable));

		getRight().addComponent(m_progress);
		getRight().addComponent(m_panel);
		VerticalLayout joblog = new VerticalLayout();
		joblog.addComponent(m_loginfo);
		joblog.addComponent(m_logTable);
		getRight().addComponent(new Panel("Logs",joblog));
		
	}
	

	@Override
	public String getName() {
		return "FastTab";
	}

	@Override
	public void load() {
		updateTabHead();

		if (getService().isFastRunning()) {
			m_fast.setEnabled(false);
			m_panel.setCaption("Fast Integration - Status: Running");
			
		} else {
			m_panel.setCaption("Fast Integration - Status: Ready");			
		}
		if (m_loaded) 
			return;
		m_jobdao = getService().getJobContainer();
		m_joblogdao = getService().getJobLogContainer();
		m_jobTable.setContainerDataSource(m_jobdao);
		m_jobTable.setVisibleColumns(new Object[] {"jobid", "username", "jobstatus","jobstart","jobend"});
    	m_loaded = true;
	}

	public boolean runFast() {
		m_searchField.setVisible(false);
		if (m_selectLog != null) {
			m_jobTable.unselect(m_selectLog.getItemProperty("jobid").getValue());
			m_selectLog = null;
		}
		m_jobTable.setSelectable(false);
		m_jobTable.setVisibleColumns(new Object[] {"jobid", "username", "jobstatus","jobstart","jobend"});
		m_logTable.setVisible(false);
        m_runnable = new FastTabRunnable(getService());
        m_loginfo.setCaption("");
		m_logTable.setContainerDataSource(m_runnable.getJobLogContainer());

        UI.getCurrent().setPollInterval(5000);
        Thread thread = new Thread(m_runnable);
        thread.start();		
		m_logTable.setVisible(true);
        
		logger.info ("Fast Integration - Status: Running");
        m_panel.setCaption("Fast Integration - Status: Running");
        return true;
		
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_fast) {
	        runFast();
		} else	if (event.getButton() == m_syncfast) {
			sync(DashBoardUtils.TN_REQU_NAME);
		} else {
			super.buttonClick(event);
		}
	}
		
	private class FastTabRunnable extends FastRunnable {

		public FastTabRunnable(DashBoardSessionService session) {
			super(session);
		}

		@Override
		public void updateProgress(Float progress) {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					m_progress.setIndeterminate(false);
					m_progress.setValue(progress);
				}
			});				
		}

		@Override
		public void log(List<JobLogEntry> logs) {
			UI.getCurrent().access(new Runnable() {
				
				@Override
				public void run() {
					for (JobLogEntry log: logs) {
						log(log);
					}
				}
			});
		}

		@Override
		public void beforeJob() {
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
			        m_fast.setEnabled(false);
			        m_progress.setIndeterminate(true);
			        m_progress.setVisible(true);
			        m_progress.setEnabled(true);
				}
			});
			UI.getCurrent().getSession().getLockInstance().lock();
			try {
				startJob();
			} finally {
				UI.getCurrent().getSession().getLockInstance().unlock();
				
			}
		}

		@Override
		public void afterJob() {
			UI.getCurrent().getSession().getLockInstance().lock();
			try {
				endJob();
			} finally {
				UI.getCurrent().getSession().getLockInstance().unlock();				
			}
			UI.getCurrent().access(new Runnable() {

				@Override
				public void run() {
					m_progress.setValue(new Float(0.0));
					m_progress.setEnabled(false);
					m_progress.setVisible(false);

					m_fast.setEnabled(true);
					m_jobTable.setSelectable(true);
					m_jobTable.setVisibleColumns(new Object[] {"jobid", "username", "jobstatus","jobstart","jobend"});
					m_panel.setCaption("Fast Integration - Status: Ready");
					m_searchField.setVisible(true);

					// Stop polling
					UI.getCurrent().setPollInterval(-1);
				}
			});			
		}
		
	}

}
