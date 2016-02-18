package org.opennms.vaadin.provision.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class VrfContainer extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8281743245940512436L;

	public VrfContainer(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}
	
	public synchronized void add(Vrf vrf) {
		save(addItem(),vrf);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object vrfId, Vrf vrf) {
		getContainerProperty(vrfId, "name").setValue(vrf.getName());
		getContainerProperty(vrfId, "notifylevel").setValue(vrf.getNotifylevel());
		getContainerProperty(vrfId, "networklevel").setValue(vrf.getNetworklevel());
		getContainerProperty(vrfId, "dnsdomain").setValue(vrf.getDnsdomain());
		getContainerProperty(vrfId, "thresholdlevel").setValue(vrf.getThresholdlevel());
		getContainerProperty(vrfId, "backupprofile").setValue(vrf.getBackupprofile());
		getContainerProperty(vrfId, "snmpprofile").setValue(vrf.getSnmpprofile());
	}
	
	public synchronized void saveOrUpdate(Object id, Vrf vrf) {
		if (id == null)
			save(super.addItem(),vrf);
		else
			save(id, vrf);
	}
	
	public synchronized Vrf getVrf(String name) {
		return getVrfMap().get(name);
	}
	
    public synchronized Map<String,Vrf> getVrfMap() {
    	Map<String, Vrf> vrf = new HashMap<String, Vrf>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item vrftableRow = getItem(i.next());
			vrf.put(vrftableRow.getItemProperty("name").getValue().toString(),
					new Vrf(vrftableRow.getItemProperty("name").getValue().toString(),
							vrftableRow.getItemProperty("notifylevel").getValue().toString(),
							vrftableRow.getItemProperty("networklevel").getValue().toString(),
							vrftableRow.getItemProperty("dnsdomain").getValue().toString(),
							vrftableRow.getItemProperty("thresholdlevel").getValue().toString(),
							vrftableRow.getItemProperty("backupprofile").getValue().toString(),
							vrftableRow.getItemProperty("snmpprofile").getValue().toString()
							));
		}
    	return vrf;
    }


    public synchronized Vrf get(Object vrfId) {
		Item vrftableRow = getItem(vrfId);
		if (vrfId == null)
			return null;
		return new Vrf(
				vrftableRow.getItemProperty("name").getValue().toString(),
				vrftableRow.getItemProperty("notifylevel").getValue().toString(),
				vrftableRow.getItemProperty("networklevel").getValue().toString(),
				vrftableRow.getItemProperty("dnsdomain").getValue().toString(),
				vrftableRow.getItemProperty("thresholdlevel").getValue().toString(),
				vrftableRow.getItemProperty("backupprofile").getValue().toString(),
				vrftableRow.getItemProperty("snmpprofile").getValue().toString()
				);
    }
	
}
