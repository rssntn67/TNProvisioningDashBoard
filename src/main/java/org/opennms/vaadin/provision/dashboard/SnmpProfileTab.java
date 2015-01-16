package org.opennms.vaadin.provision.dashboard;

import java.sql.SQLException;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@Title("TrentinoNetwork Provision Dashboard - Trentino Network Requisition")
@Theme("runo")
public class SnmpProfileTab extends DashboardTab {

	private boolean loaded = false;
	private Table m_snmpProfilesTable;;

	/**
	 * 
	 */
	private static final long serialVersionUID = 9020194832144108254L;

	public SnmpProfileTab(DashBoardService service) {
		super(service);
	}

	@Override
	public void load() {
		if (loaded)
			return;
		try {
			m_snmpProfilesTable = new Table("Profiles", getService().getSnmpProfileContainer());
		} catch (SQLException e) {
			Notification.show("Snmp Profiles", "Load from db Failed", Type.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.addComponent(m_snmpProfilesTable);
		setCompositionRoot(new Panel(layout));
		loaded=true;
	}

}
