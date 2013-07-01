package org.opennms.vaadin.provision.dashboard;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class DashboardTabSheet extends CustomComponent implements
		SelectedTabChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4835992723502900986L;
	TabSheet tabsheet = new TabSheet();
    LoginBox loginbox;
    DashBoardService m_service;
    
    DashboardTabSheet(String[] tabs, String[] urls,DashBoardService service) {
    	m_service = service;
    	loginbox = new LoginBox(urls,tabsheet,m_service);
		setCompositionRoot(tabsheet);
		
		tabsheet.addSelectedTabChangeListener(this);
		
        tabsheet.addTab(loginbox, "Login Box", new ThemeResource("icons/16/user.png"));
        for (int i=0; i<tabs.length;i++) {
        	ProvisionGroupPanelNew tab=new ProvisionGroupPanelNew(tabs[i],m_service);
        	tabsheet.addTab(tab, tabs[i], new ThemeResource("icons/16/users.png"));
        	tabsheet.getTab(tab).setEnabled(false);
        }        
	}
	
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		final TabSheet source = (TabSheet) event.getSource();
		if (source == tabsheet) {
			if (source.getSelectedTab() != loginbox) {
				((ProvisionGroup)source.getSelectedTab()).load();
			}
		}
	}

}
