package org.opennms.vaadin.provision.dashboard;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

public class DashboardTabSheet extends TabSheet implements SelectedTabChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4835992723502900986L;
    private LoginBox m_loginbox;
    
    DashboardTabSheet() {
		addSelectedTabChangeListener(this);
    	
		m_loginbox = new LoginBox();
        addTab(m_loginbox, "Login Box", new ThemeResource("icons/16/user.png"));
    	
        TrentinoNetworkTab tab=new TrentinoNetworkTab();
    	addTab(tab, "Rete TN", new ThemeResource("icons/16/users.png"));
    	getTab(tab).setEnabled(false);

        MediaGatewayTab mtab=new MediaGatewayTab();
    	addTab(mtab, "Media Gw", new ThemeResource("icons/16/users.png"));
    	getTab(mtab).setEnabled(false);

    	SistemiInformativiTab stab=new SistemiInformativiTab();
    	addTab(stab, "SI", new ThemeResource("icons/16/users.png"));
    	getTab(stab).setEnabled(false);

        CategorieTab catTab=new CategorieTab();
    	addTab(catTab, "Categorie", new ThemeResource("icons/16/users.png"));
    	getTab(catTab).setEnabled(false);
    	
    	SnmpProfileTab snmpTab = new SnmpProfileTab();
        addTab(snmpTab, "Profili Snmp",new ThemeResource("icons/16/users.png"));
    	getTab(snmpTab).setEnabled(false);
    	
    	BackupProfileTab backupTab = new BackupProfileTab();
        addTab(backupTab, "Profili Backup",new ThemeResource("icons/16/users.png"));
        getTab(backupTab).setEnabled(false);
        
        FastTab fastTab= new FastTab();
        addTab(fastTab, "Fast",new ThemeResource("icons/16/users.png"));
        getTab(fastTab).setEnabled(false);
    	
	}
	
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		final TabSheet source = (TabSheet) event.getSource();
		if (source == this) {
				((DashboardTab)source.getSelectedTab()).load();
		}
	}

	public LoginBox getLoginBox() {
		return m_loginbox;
	}
}
