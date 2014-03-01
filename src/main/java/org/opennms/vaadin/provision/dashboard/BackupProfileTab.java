package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;

@Title("TrentinoNetwork Provision Dashboard - Trentino Network Requisition")
@Theme("runo")
public class BackupProfileTab extends DashboardTab {

	private Table m_backupProfilesTable;
	private boolean loaded = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public BackupProfileTab(String foreignsource, DashBoardService service) {
		super(foreignsource, service);
	}

	@Override
	public void load() {
		if (loaded)
			return;
		try {
			getService().loadBackupProfiles();
		} catch (SQLException e) {
			Notification.show("Backup Profile", "Load from db Failed", Type.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}
		try {
			m_backupProfilesTable = new Table("Profiles", getService().getBackupProfileContainer());
		} catch (SQLException e) {
			Notification.show("Backup Profiles", "Load from db Failed", Type.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.addComponent(m_backupProfilesTable);
		setCompositionRoot(new Panel(layout));
		loaded = true;
	}

}
