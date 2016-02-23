package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.vaadin.provision.model.BackupProfile;

import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;

public class BackupProfileDao extends SQLContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4984152104834873631L;

	public BackupProfileDao(QueryDelegate delegate) throws SQLException {
		super(delegate);
	}

	public synchronized void add(BackupProfile backup) {
		save(addItem(),backup);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void save(Object backupId, BackupProfile backup) {
		getContainerProperty(backupId, "name").setValue(backup.getName());
		getContainerProperty(backupId, "username").setValue(backup.getUsername());
		getContainerProperty(backupId, "password").setValue(backup.getPassword());
		getContainerProperty(backupId, "enable").setValue(backup.getEnable());
		getContainerProperty(backupId, "connection").setValue(backup.getConnection());
		getContainerProperty(backupId, "auto_enable").setValue(backup.getAutoenable());
	}
	
	public synchronized void saveOrUpdate(Object id, BackupProfile backup) {
		if (id == null)
			save(super.addItem(),backup);
		else
			save(id, backup);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized Map<String, BackupProfile> getBackupProfileMap() {
    	Map<String, BackupProfile> backupProfiles = new HashMap<String, BackupProfile>();
		for (Iterator<?> i = getItemIds().iterator(); i.hasNext();) {
			Item backupprofiletableRow = getItem(i.next());
			backupProfiles.put(backupprofiletableRow.getItemProperty("name").getValue().toString(),
					new BackupProfile(
							backupprofiletableRow.getItemProperty("name"),
							backupprofiletableRow.getItemProperty("username"), 
							backupprofiletableRow.getItemProperty("password"), 
							backupprofiletableRow.getItemProperty("enable"),
							backupprofiletableRow.getItemProperty("connection"), 
							backupprofiletableRow.getItemProperty("auto_enable")
							));
		}
		return backupProfiles;
	}

	public synchronized BackupProfile getBackupProfile(String name) {
    	return getBackupProfileMap().get(name);
	}

	@SuppressWarnings("unchecked")
	public synchronized BackupProfile get(Object backupId) {
		Item backuptableRow = getItem(backupId);
		if (backuptableRow == null)
			return null;
		return new BackupProfile(backuptableRow.getItemProperty("name"),
				backuptableRow.getItemProperty("username"),
				backuptableRow.getItemProperty("password"),
				backuptableRow.getItemProperty("enable"),
				backuptableRow.getItemProperty("connection"),
				backuptableRow.getItemProperty("auto_enable")
				);

	}
}
