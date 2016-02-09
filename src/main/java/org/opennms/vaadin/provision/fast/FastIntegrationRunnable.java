package org.opennms.vaadin.provision.fast;

import org.opennms.vaadin.provision.dashboard.FastTab;

public class FastIntegrationRunnable implements Runnable {

	final private FastTab m_tab;

	public FastIntegrationRunnable(FastTab tab) {
		m_tab = tab;
	}

	@Override
	public void run() {
		double current = 0.0;
		m_tab.setCaptionRunning(new Float(current));
		for (int i=0; i<100; i++) {
			try {
				Thread.sleep(100);
				current += 0.01;
				m_tab.setCaptionRunning(new Float(current));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		m_tab.setCaptionRunning(new Float(1.0));
		m_tab.setCaptionReady();
	}
			

}
