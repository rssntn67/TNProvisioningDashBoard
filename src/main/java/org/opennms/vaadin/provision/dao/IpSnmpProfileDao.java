package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.vaadin.provision.model.IpSnmpProfile;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class IpSnmpProfileDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1038128084131099555L;

	public IpSnmpProfileDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(IpSnmpProfile snmp) {
		save(addItem(), snmp);
	}

	@SuppressWarnings("unchecked")
	public synchronized void save(Object snmpId, IpSnmpProfile ipsnmp) {
		getContainerProperty(snmpId, "ipaddr").setValue(
				ipsnmp.getIp());
		getContainerProperty(snmpId, "snmpprofile").setValue(
				ipsnmp.getSnmprofile());
	}

	@SuppressWarnings("unchecked")
	public synchronized void update(IpSnmpProfile snmp) {
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item ipsnmpprofiletableRow = getItem(i.next());
			String ip = (String) ipsnmpprofiletableRow.getItemProperty("ipaddr")
					.getValue();
			if (snmp.getIp().equals(ip))
				ipsnmpprofiletableRow.getItemProperty("snmpprofile").setValue(
						snmp.getSnmprofile());
		}
	}

	public synchronized void saveOrUpdate(Object id, IpSnmpProfile snmpprofile) {
		if (id == null)
			save(super.addItem(), snmpprofile);
		else
			save(id, snmpprofile);
	}

	public synchronized IpSnmpProfile getSnmpProfile(String ip) {
		return getIpSnmpProfileMap().get(ip);
	}

	public synchronized void remove(String ip) {
		if (ip == null)
			return;
		Item item = getItem(ip);
		removeItem(item);
	}
	
	public synchronized Object getItemId(String ip) {
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Object itemId = i.next();
			Item ipsnmpprofiletableRow = getItem(itemId);
			if (ipsnmpprofiletableRow.getItemProperty("ipaddr").equals(ip)) {
				return itemId;
			}
		}
		return null;
	}
	public synchronized Map<String, IpSnmpProfile> getIpSnmpProfileMap() {
		Map<String, IpSnmpProfile> ipSnmpProfiles = new HashMap<String, IpSnmpProfile>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item ipsnmpprofiletableRow = getItem(i.next());
			ipSnmpProfiles.put(ipsnmpprofiletableRow.getItemProperty("ipaddr")
					.getValue().toString(), new IpSnmpProfile(
					(String) ipsnmpprofiletableRow.getItemProperty("ipaddr")
							.getValue(), (String) ipsnmpprofiletableRow
							.getItemProperty("snmpprofile").getValue()));
		}
		return ipSnmpProfiles;
	}

	public synchronized IpSnmpProfile get(Object ipsnmpId) {
		Item ipsnmptableRow = getItem(ipsnmpId);
		if (ipsnmptableRow == null)
			return null;
		return new IpSnmpProfile((String) ipsnmptableRow.getItemProperty("ipaddr")
				.getValue(), (String) ipsnmptableRow.getItemProperty(
				"snmpprofile").getValue());

	}

}
