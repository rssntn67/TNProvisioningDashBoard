package org.opennms.vaadin.provision.dashboard;


import org.opennms.vaadin.provision.fast.FastIntegrationRunnable;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

@Title("TNPD - Fast Integration")
@Theme("runo")
public class FastTab extends DashboardTab implements ClickListener {

	private Panel m_panel  = new Panel("Fast Integration - Status: Ready");
    private Button m_fast = new Button("Start Fast Integration");
    final ProgressIndicator m_progress = new ProgressIndicator();

    private boolean m_loaded = false;

    private FastIntegrationRunnable runnable; 
	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public FastTab(DashBoardService service) {
		super(service);
	}

	@Override
	public void load() {
		if (m_loaded) 
			return;
		runnable = new FastIntegrationRunnable(this);
		m_panel.setContent(getFastBox());
        setCompositionRoot(m_panel);
        m_fast.addClickListener(this);
        m_loaded = true;
	}

	private Component getFastBox() {
	   	VerticalLayout layout = new VerticalLayout();
    	layout.setMargin(true);
        layout.addComponent(m_fast);
        m_progress.setEnabled(false);
        layout.addComponent(m_progress);
        return layout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == m_fast) 
			runfast();
	}
	
	private void runfast() {
		m_progress.setEnabled(true);
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

}
