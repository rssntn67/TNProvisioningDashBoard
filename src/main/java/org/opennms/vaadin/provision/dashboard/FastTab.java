package org.opennms.vaadin.provision.dashboard;



import java.sql.SQLException;
import java.util.List;

import org.opennms.vaadin.provision.dao.JobDao;
import org.opennms.vaadin.provision.fast.FastIntegrationRunnable;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.JobLogEntry;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.util.BeanItemContainer;
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
import com.vaadin.ui.VerticalLayout;

@Title("TNPD - Fast Integration")
@Theme("runo")
public class FastTab extends DashboardTab implements ClickListener {

	private Panel m_panel  = new Panel("Fast Integration - Status: Ready");
    private Button m_fast = new Button("Start Fast Integration");
    final ProgressBar m_progress = new ProgressBar();

    private JobDao m_jobcontainer;
	private BeanItemContainer<JobLogEntry> m_logcontainer = new BeanItemContainer<JobLogEntry>(JobLogEntry.class);
    private boolean m_loaded = false;

    private FastIntegrationRunnable runnable; 
    
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
		m_logTable.setContainerDataSource(m_logcontainer);	       
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
		if (event.getButton() == m_fast) 
			runfast();
	}
	
	private void runfast() {
        m_logTable.setVisible(true);
        m_progress.setEnabled(true);
//        m_progress.setIndeterminate(true);
   		runnable = new FastIntegrationRunnable(this);
		Thread thread = new Thread(runnable);
		thread.start();		
		Notification.show("Fast Integration - Status: Started", Type.HUMANIZED_MESSAGE);	
	}
	
	public void setCaptionReady() {
			m_fast.setEnabled(true);
			m_panel.setCaption("Fast Integration - Status: Ready");
			m_progress.setEnabled(false);			
	}
	
	public void setCaptionRunning(Float fl) {
		m_fast.setEnabled(false);
		m_panel.setCaption("Fast Integration - Status: Running");
		m_progress.setValue(fl);
	}
		
	public int commitJob(Job job) {
		if (job.getJobid() == null)
			m_jobcontainer.add(job);
		else
			m_jobcontainer.save(job.getJobid(), job);
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
		m_logcontainer.addBean(jLogE);
	}
	
	public void log(List<JobLogEntry> jLogEcoll) {
		m_logcontainer.addAll(jLogEcoll);
	}

}
