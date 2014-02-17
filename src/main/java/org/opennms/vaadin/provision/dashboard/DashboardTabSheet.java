package org.opennms.vaadin.provision.dashboard;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class DashboardTabSheet extends CustomComponent implements
		SelectedTabChangeListener {

	protected final static String[] FOREIGN_SOURCE_LIST = new String[] {"TrentinoNetwork"};
	//,
	//	"SI","SIVirtualNodes"};

	/**
	 * 
	 */
	private static final long serialVersionUID = -4835992723502900986L;
	TabSheet tabsheet = new TabSheet();
    LoginBox loginbox;
    DashBoardService m_service = new DashBoardService();
    
    DashboardTabSheet() {

    	loginbox = new LoginBox(tabsheet,m_service);
		setCompositionRoot(tabsheet);
		
		tabsheet.addSelectedTabChangeListener(this);
        tabsheet.addTab(loginbox, "Login Box", new ThemeResource("icons/16/user.png"));
        for (String foreignSource: FOREIGN_SOURCE_LIST) {
        	TrentinoNetworkTab tab=new TrentinoNetworkTab(foreignSource,m_service);
        	tabsheet.addTab(tab, foreignSource, new ThemeResource("icons/16/users.png"));
        	tabsheet.getTab(tab).setEnabled(false);
        }
        SnmpProfileTab snmpTab = new SnmpProfileTab(null, m_service);
        tabsheet.addTab(snmpTab, "Snmp Profiles",new ThemeResource("icons/16/users.png"));
    	tabsheet.getTab(snmpTab).setEnabled(false);
    	BackupProfileTab backupTab = new BackupProfileTab(null, m_service);
        tabsheet.addTab(backupTab, "Backup Profiles",new ThemeResource("icons/16/users.png"));
        tabsheet.getTab(backupTab).setEnabled(false);
    	
	}
	
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		final TabSheet source = (TabSheet) event.getSource();
		if (source == tabsheet) {
			if (source.getSelectedTab() != loginbox) {
				((DashboardTab)source.getSelectedTab()).load();
			}
		}
	}

}
