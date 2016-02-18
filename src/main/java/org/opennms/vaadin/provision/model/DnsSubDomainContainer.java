package org.opennms.vaadin.provision.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class DnsSubDomainContainer extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3163337505690134726L;

	public DnsSubDomainContainer(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(String domain) {
		save(addItem(),domain);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object id, String domain) {
		getContainerProperty(id, "dnssubdomain").setValue(domain);

	}
	
	public synchronized void saveOrUpdate(Object id, String domain) {
		if (id == null)
			save(super.addItem(),domain);
		else
			save(id, domain);
	}

	
	public synchronized List<String> getSubdomains() {
    	List<String> subdomains = new ArrayList<String>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item dnssubdomaintableRow = getItem(i.next());
			subdomains.add(dnssubdomaintableRow.getItemProperty("dnssubdomain").getValue().toString());
		}
		return subdomains;
	}
	    


}
