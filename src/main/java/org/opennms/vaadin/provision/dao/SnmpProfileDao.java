package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.vaadin.provision.model.SnmpProfile;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class SnmpProfileDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1038128084131099555L;

	public SnmpProfileDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(SnmpProfile snmp) {
		save(addItem(),snmp);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object snmpId, SnmpProfile snmp) {
		getContainerProperty(snmpId, "name").setValue(snmp.getName());
		getContainerProperty(snmpId, "community").setValue(snmp.getCommunity());
		getContainerProperty(snmpId, "version").setValue(snmp.getVersion());
		getContainerProperty(snmpId, "timeout").setValue(snmp.getTimeout());
	}
	
	public synchronized void saveOrUpdate(Object id, SnmpProfile snmpprofile) {
		if (id == null)
			save(super.addItem(),snmpprofile);
		else
			save(id, snmpprofile);
	}
	
	public synchronized SnmpProfile getSnmpProfile(String name) {
		return getSnmpProfileMap().get(name);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Map<String, SnmpProfile> getSnmpProfileMap() {
    	Map<String, SnmpProfile> snmpProfiles = new HashMap<String, SnmpProfile>();
        for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item snmpprofiletableRow = getItem(i.next());
			snmpProfiles.put(snmpprofiletableRow.getItemProperty("name").getValue().toString(),
					new SnmpProfile(snmpprofiletableRow.getItemProperty("name"),snmpprofiletableRow.getItemProperty("community"), 
							snmpprofiletableRow.getItemProperty("version"), 
							snmpprofiletableRow.getItemProperty("timeout")));
		}
		return snmpProfiles;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized SnmpProfile get(Object snmpId) {
		Item snmptableRow = getItem(snmpId);
		if (snmptableRow == null )
			return null;
		return new SnmpProfile(
				snmptableRow.getItemProperty("name"),
				snmptableRow.getItemProperty("community"),
				snmptableRow.getItemProperty("version"),
				snmptableRow.getItemProperty("timeout")
				);
		
	}

}
