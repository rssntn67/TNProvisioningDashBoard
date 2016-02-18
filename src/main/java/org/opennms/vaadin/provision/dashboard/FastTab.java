package org.opennms.vaadin.provision.dashboard;



import java.sql.SQLException;

import org.opennms.vaadin.provision.fast.FastIntegrationRunnable;
import org.opennms.vaadin.provision.model.Job;
import org.opennms.vaadin.provision.model.JobContainer;
import org.opennms.vaadin.provision.model.JobLogEntry;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Title("TNPD - Fast Integration")
@Theme("runo")
public class FastTab extends DashboardTab implements ClickListener {

	private Panel m_panel  = new Panel("Fast Integration - Status: Ready");
    private Button m_fast = new Button("Start Fast Integration");
    final ProgressIndicator m_progress = new ProgressIndicator();

    private JobContainer m_jobcontainer;
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
		m_jobcontainer = getService().getTnDao().getJobContainer();
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
        layout.addComponent(m_jobTable);
        m_logTable.setVisible(false);
        layout.addComponent(m_logTable);
        return layout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_fast) 
			runfast();
	}
	
	private void runfast() {
        m_logTable.setVisible(true);
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
		try {
			m_jobcontainer.commit();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return m_jobcontainer.getLastJobId().getValue();

	}
	
	public void log(JobLogEntry jLogE) {
		m_logcontainer.addBean(jLogE);
	}
}
