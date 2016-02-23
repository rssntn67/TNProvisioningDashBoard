package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class DnsDomainDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3163337505690134726L;

	public DnsDomainDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(String domain) {
		save(addItem(),domain);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object id, String domain) {
		getContainerProperty(id, "dnsdomain").setValue(domain);

	}
	
	public synchronized void saveOrUpdate(Object id, String domain) {
		if (id == null)
			save(super.addItem(),domain);
		else
			save(id, domain);
	}

	
	public synchronized List<String> getDomains() {
    	List<String> domains = new ArrayList<String>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item dnsdomaintableRow = getItem(i.next());
			domains.add(dnsdomaintableRow.getItemProperty("dnsdomain").getValue().toString());
		}
		return domains;
	}


}
