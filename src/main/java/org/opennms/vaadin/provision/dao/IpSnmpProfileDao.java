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
		save(addItem(),snmp);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object snmpId, IpSnmpProfile ipsnmp) {
		getContainerProperty(snmpId, "ip").setValue(ipsnmp.getIp());
		getContainerProperty(snmpId, "snmpprofile").setValue(ipsnmp.getSnmprofile());
	}
	
	public synchronized void saveOrUpdate(Object id, IpSnmpProfile snmpprofile) {
		if (id == null)
			save(super.addItem(),snmpprofile);
		else
			save(id, snmpprofile);
	}
	
	public synchronized IpSnmpProfile getSnmpProfile(String ip) {
		return getIpSnmpProfileMap().get(ip);
	}
	
	public synchronized Map<String, IpSnmpProfile> getIpSnmpProfileMap() {
		Map<String, IpSnmpProfile> ipSnmpProfiles = new HashMap<String, IpSnmpProfile>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item ipsnmpprofiletableRow = getItem(i.next());
			ipSnmpProfiles.put(ipsnmpprofiletableRow.getItemProperty("ip")
					.getValue().toString(),
					new IpSnmpProfile(
							(String)ipsnmpprofiletableRow.getItemProperty("ip").getValue(),
							(String)ipsnmpprofiletableRow.getItemProperty("snmpprofile").getValue()
							));
		}
		return ipSnmpProfiles;
	}
	
	public synchronized IpSnmpProfile get(Object ipsnmpId) {
		Item ipsnmptableRow = getItem(ipsnmpId);
		if (ipsnmptableRow == null )
			return null;
		return 					new IpSnmpProfile(
				(String)ipsnmptableRow.getItemProperty("ip").getValue(),
				(String)ipsnmptableRow.getItemProperty("snmpprofile").getValue()
				);
		
	}

}
