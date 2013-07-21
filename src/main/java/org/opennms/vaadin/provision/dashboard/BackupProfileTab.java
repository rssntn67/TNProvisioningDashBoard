package org.opennms.vaadin.provision.dashboard;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Title("TrentinoNetwork Provision Dashboard - Trentino Network Requisition")
@Theme("runo")
public class BackupProfileTab extends DashboardTab {

	private Table m_backupProfilesTable;;

	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public BackupProfileTab(String foreignsource, DashboardService service) {
		super(foreignsource, service);
		m_backupProfilesTable = new Table("Profiles", service.getBackupProfiles());
	}

	@Override
	public void load() {
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.addComponent(m_backupProfilesTable);
		setCompositionRoot(new Panel(layout));
	}

}
