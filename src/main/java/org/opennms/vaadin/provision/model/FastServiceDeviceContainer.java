package org.opennms.vaadin.provision.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class FastServiceDeviceContainer extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6920237072015173649L;

	public FastServiceDeviceContainer(QueryDelegate delegate)
			throws SQLException {
		super(delegate);
	}
	
    @SuppressWarnings("unchecked")
	public synchronized List<FastServiceDevice> getFastServiceDevices() {
    	List<FastServiceDevice> devices = new ArrayList<FastServiceDevice>();
    	
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item fastservicedevicetableRow = getItem(i.next());
			devices.add(new FastServiceDevice(fastservicedevicetableRow.getItemProperty("hostname"), 
					fastservicedevicetableRow.getItemProperty("ipaddr"),
					fastservicedevicetableRow.getItemProperty("ipaddr_lan"),
					fastservicedevicetableRow.getItemProperty("netmask_lan"),
					fastservicedevicetableRow.getItemProperty("serial_number"),
					fastservicedevicetableRow.getItemProperty("address_desc"),
					fastservicedevicetableRow.getItemProperty("address_name"),
					fastservicedevicetableRow.getItemProperty("address_number"),
					fastservicedevicetableRow.getItemProperty("floor"),
					fastservicedevicetableRow.getItemProperty("room"),
					fastservicedevicetableRow.getItemProperty("city"),
					fastservicedevicetableRow.getItemProperty("istat_code"),
					fastservicedevicetableRow.getItemProperty("master_device"),
					fastservicedevicetableRow.getItemProperty("snmpprofiles"),
					fastservicedevicetableRow.getItemProperty("backupprofiles"),
					fastservicedevicetableRow.getItemProperty("not_monitoring"),
					fastservicedevicetableRow.getItemProperty("save_config"),
					fastservicedevicetableRow.getItemProperty("notify_category"),
					fastservicedevicetableRow.getItemProperty("order_code"),
					fastservicedevicetableRow.getItemProperty("device_type")));
		}
    	return devices;
	}

}
