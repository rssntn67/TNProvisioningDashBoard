package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.vaadin.provision.model.Categoria;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class CategoriaDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8281743245940512436L;

	public CategoriaDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}
	
	public synchronized void add(Categoria cat) {
		save(addItem(),cat);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object catId, Categoria cat) {
		getContainerProperty(catId, "name").setValue(cat.getName());
		getContainerProperty(catId, "notifylevel").setValue(cat.getNotifylevel());
		getContainerProperty(catId, "networklevel").setValue(cat.getNetworklevel());
		getContainerProperty(catId, "dnsdomain").setValue(cat.getDnsdomain());
		getContainerProperty(catId, "thresholdlevel").setValue(cat.getThresholdlevel());
		getContainerProperty(catId, "backupprofile").setValue(cat.getBackupprofile());
		getContainerProperty(catId, "snmpprofile").setValue(cat.getSnmpprofile());
	}
	
	public synchronized void saveOrUpdate(Object id, Categoria cat) {
		if (id == null)
			save(super.addItem(),cat);
		else
			save(id, cat);
	}
	
	public synchronized Categoria getCategoria(String name) {
		return getCatMap().get(name);
	}
	
    public synchronized Map<String,Categoria> getCatMap() {
    	Map<String, Categoria> catMap = new HashMap<String, Categoria>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item cattableRow = getItem(i.next());
			catMap.put(cattableRow.getItemProperty("name").getValue().toString(),
					new Categoria(cattableRow.getItemProperty("name").getValue().toString(),
							cattableRow.getItemProperty("notifylevel").getValue().toString(),
							cattableRow.getItemProperty("networklevel").getValue().toString(),
							cattableRow.getItemProperty("dnsdomain").getValue().toString(),
							cattableRow.getItemProperty("thresholdlevel").getValue().toString(),
							cattableRow.getItemProperty("backupprofile").getValue().toString(),
							cattableRow.getItemProperty("snmpprofile").getValue().toString()
							));
		}
    	return catMap;
    }


    public synchronized Categoria get(Object catId) {
		Item cattableRow = getItem(catId);
		if (catId == null)
			return null;
		return new Categoria(
				cattableRow.getItemProperty("name").getValue().toString(),
				cattableRow.getItemProperty("notifylevel").getValue().toString(),
				cattableRow.getItemProperty("networklevel").getValue().toString(),
				cattableRow.getItemProperty("dnsdomain").getValue().toString(),
				cattableRow.getItemProperty("thresholdlevel").getValue().toString(),
				cattableRow.getItemProperty("backupprofile").getValue().toString(),
				cattableRow.getItemProperty("snmpprofile").getValue().toString()
				);
    }
	
}
