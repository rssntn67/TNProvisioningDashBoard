package org.opennms.vaadin.provision.dashboard;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Title("Trentino Network Provision Dashboard - Backup Profiles")
@Theme("runo")
public class BackupProfileTab extends DashboardTab {

	private Table m_backupProfilesTable;
	private boolean loaded = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public BackupProfileTab(DashBoardService service) {
		super(service);
	}

	@Override
	public void load() {
		if (loaded)
			return;
		m_backupProfilesTable = new Table("Profiles", getService().getTnDao().getBackupProfileContainer());

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.addComponent(m_backupProfilesTable);
		setCompositionRoot(new Panel(layout));
		loaded = true;
	}

}
