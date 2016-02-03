package org.opennms.vaadin.provision.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.vaadin.provision.model.BackupProfile;
import org.opennms.vaadin.provision.model.SnmpProfile;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;

public class TNDao {

	public static final String[] m_notif_categories = {
		"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"
	};

	public static final String[] m_thresh_categories = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};
	
	public static final String[][] m_network_categories = {
		{"Core","Backbone","bb.tnnet.it","EMERGENCY_F0","ThresholdWARNING","backbone_7","snmp_default_v2"},
		{"Ponte5p4","Backbone","wl.tnnet.it","EMERGENCY_F1","ThresholdWARNING","backbone_accesspoint_alvarion_tftp","public_v1"},
		{"PontePDH","Backbone","wl.tnnet.it","EMERGENCY_F1","ThresholdWARNING","backbone_accesso_switch_pat","public_v2"},
		{"AccessPoint","Distribuzione","wl.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesspoint_essentia_ssh","wifless_v2"},
		{"SwitchWiNet","Distribuzione","wl.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_switch_alcatel_winet","wifless_v2"},
		{"SwitchTetra","Distribuzione","wl.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_switch_alcatel_winet","wifless_v2"},
		{"Fiemme2013","Distribuzione","bb.tnnet.it","EMERGENCY_F0","ThresholdWARNING","backbone_2013","snmp_default_v2"},
		{"ACSM","Accesso","acsm.tnnet.it","EMERGENCY_F1","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"AgLav","Accesso","aglav.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Apss","Accesso","apss.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Audiovisivi","Accesso","wl.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Biblio","Accesso","biblio.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"CPE","Accesso","wl.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesspoint_essentia_ssh","wifless_v2"},
		{"CUE","Accesso","cue.tnnet.it","EMERGENCY_F0","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ComuneTN","Accesso","comunetn.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v1"},
		{"Comuni","Accesso","comuni.tnnet.it","EMERGENCY_F4","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ConsPro","Accesso","conspro.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ConsProVoce","Accesso","conspro.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"FEM","Accesso","fem.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"GeoSis","Accesso","geosis.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Iasma","Accesso","iasma.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Info","Accesso","info.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Infotn","Accesso","infotn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"InfotnVoce","Accesso","infotn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Internet","Accesso","internet.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Internet-Esterni","Accesso","internet-esterni.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v1"},
		{"InternetScuole","Accesso","wl.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Intra-TNSviluppo","Accesso","wl.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"LAN","Accesso","hq.tnnet.it","EMERGENCY_F0","ThresholdWARNING","lan","snmp_default_v2"},
		{"Medici","Accesso","medici.tnnet.it","INFORMATION","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Mitt","Accesso","mitt.tnnet.it","EMERGENCY_F1","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Multivoce","Accesso","multivoce.tnnet.it","EMERGENCY_F1","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"OperaUnitn","Accesso","operaunitn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"OperaVoce","Accesso","operavoce.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Pat","Accesso","pat.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"PatAcquePub","Accesso","patacquepub.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"PatDighe","Accesso","patdighe.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Pat-Tecnica","Accesso","pat-tecnica.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"PatVoce","Accesso","patvoce.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Phoenix","Accesso","phoenix.tnnet.it","EMERGENCY_F3","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ReperibiliTnet","Accesso","reperibilitnet.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"RSACivicaTN","Accesso","rsacivicatn.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"RSASpes","Accesso","rsaspes.tnnet.it","EMERGENCY_F4","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Scuole","Accesso","scuole.tnnet.it","EMERGENCY_F2","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"ScuoleMaterne","Accesso","scuolematerne.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"ServizioVDS","Accesso","serviziovds.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Telpat-Autonome","Accesso","telpat-autonome.tnnet.it","EMERGENCY_F4","ThresholdWARNING","accesso_radius","snmp_default_v2"},
		{"Unitn","Accesso","unitn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"UnitnVoce","Accesso","unitn.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"VdsRovereto","Accesso","vdsrovereto.tnnet.it","EMERGENCY_F2","ThresholdWARNING","backbone_accesso_switch_pat","snmp_default_v2"},
		{"Winwinet","Accesso","winwinet.tnnet.it","INFORMATION","ThresholdWARNING","accesso_radius","snmp_default_v2"}
	};


	public static final String[] m_sub_domains = {
		"alv01.wl.tnnet.it",
		"alv02.wl.tnnet.it",
		"alv03.wl.tnnet.it",
		"alv04.wl.tnnet.it",
		"alv05.wl.tnnet.it",
		"alv06.wl.tnnet.it",
		"asw01.wl.tnnet.it",
		"cavalese-l3.pat.tnnet.it",
		"cpe01.biblio.tnnet.it",
		"cpe01.pat.tnnet.it",
		"cpe01.patacquepub.tnnet.it",
		"cpe01.scuole.tnnet.it",
		"cpe01.wl.tnnet.it",
		"ess01.wl.tnnet.it",
		"ess02.wl.tnnet.it",
		"ess03.wl.tnnet.it",
		"ess04.wl.tnnet.it",
		"ess05.wl.tnnet.it",
		"ess06.wl.tnnet.it",
		"ess07.wl.tnnet.it",
		"ess08.wl.tnnet.it",
		"mktic.comuni.tnnet.it",
		"mtk01.reperibilitnet.tnnet.it",
		"mtr01.wl.tnnet.it",
		"sw01.bb.tnnet.it",
		"sw02.bb.tnnet.it",
		"uby.wl.tnnet.it"
	};
	
	public static final String[] m_vrfs = {
		"acsm.tnnet.it",
		"aglav.tnnet.it",
		"apss.tnnet.it",
		"bb.tnnet.it",
		"biblio.tnnet.it",
		"comunetn.tnnet.it",
		"comuni.tnnet.it",
		"conspro.tnnet.it",
		"cue.tnnet.it",
		"esterni.tnnet.it",
		"fem.tnnet.it",
		"geosis.tnnet.it",
		"hq.tnnet.it",
		"iasma.tnnet.it",
		"info.tnnet.it",
		"infotn.tnnet.it",
		"internet-esterni.tnnet.it",
		"internet.tnnet.it",
		"medici.tnnet.it",
		"mitt.tnnet.it",
		"multivoce.tnnet.it",
		"operaunitn.tnnet.it",
		"pat.tnnet.it",
		"patacquepub.tnnet.it",
		"patdighe.tnnet.it",
		"patvoce.tnnet.it",
		"pat-tecnica.tnnet.it",
		"phoenix.tnnet.it",
		"reperibilitnet.tnnet.it",
		"rsacivicatn.tnnet.it",
		"rsaspes.tnnet.it",
		"scuolematerne.tnnet.it",
		"scuole.tnnet.it",
		"serviziovds.tnnet.it",
		"telpat-autonome.tnnet.it",
		"unitn.tnnet.it",
		"vdsrovereto.tnnet.it",
		"winwinet.tnnet.it",
		"wl.tnnet.it"
	};
	
	private JDBCConnectionPool m_pool; 

	public TNDao() {
	}

	public void createPool(String driver, String dburl, String username, String password) throws SQLException {
		m_pool = new SimpleJDBCConnectionPool(driver, dburl, username, password);
	}

	public void destroy() {
		if (m_pool != null)
			m_pool.destroy();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public Map<String, SnmpProfile> getSnmpProfiles() throws SQLException {
		
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		SQLContainer snmpProfileTable = new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
		Map<String, SnmpProfile> snmpProfiles = new HashMap<String, SnmpProfile>();
		for (Iterator<?> i = snmpProfileTable.getItemIds().iterator(); i.hasNext();) {
			Item snmpprofiletableRow = snmpProfileTable.getItem(i.next());
			snmpProfiles.put(snmpprofiletableRow.getItemProperty("name").getValue().toString(),
					new SnmpProfile(snmpprofiletableRow.getItemProperty("name"),snmpprofiletableRow.getItemProperty("community"), 
							snmpprofiletableRow.getItemProperty("version"), 
							snmpprofiletableRow.getItemProperty("timeout")));
		}
		return snmpProfiles;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public SnmpProfile getSnmpProfile(String name) throws SQLException {
		
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		SQLContainer snmpProfileTable = new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
		Item snmpprofiletableRow = snmpProfileTable.getItem(snmpProfileTable.firstItemId());
		return	new SnmpProfile(snmpprofiletableRow.getItemProperty("name"),snmpprofiletableRow.getItemProperty("community"), 
							snmpprofiletableRow.getItemProperty("version"), 
							snmpprofiletableRow.getItemProperty("timeout"));
	}
	
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public Map<String, BackupProfile> getBackupProfiles() throws SQLException {
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
    	SQLContainer backupProfileTable = new SQLContainer(new FreeformQuery("select * from isi.asset_profiles", primarykeys,m_pool));
    	Map<String, BackupProfile> backupProfiles = new HashMap<String, BackupProfile>();
		for (Iterator<?> i = backupProfileTable.getItemIds().iterator(); i.hasNext();) {
			Item backupprofiletableRow = backupProfileTable.getItem(i.next());
			backupProfiles.put(backupprofiletableRow.getItemProperty("name").getValue().toString(),
					new BackupProfile(backupprofiletableRow.getItemProperty("name"),backupprofiletableRow.getItemProperty("username"), 
							backupprofiletableRow.getItemProperty("password"), 
							backupprofiletableRow.getItemProperty("enable"),
							backupprofiletableRow.getItemProperty("connection"), 
							backupprofiletableRow.getItemProperty("auto_enable")
							));
		}
		return backupProfiles;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public BackupProfile getBackupProfile(String name) throws SQLException {
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
    	SQLContainer backupProfileTable = new SQLContainer(new FreeformQuery("select * from isi.asset_profiles where name = '+name"+"'", primarykeys,m_pool));
    	Item backupprofiletableRow =backupProfileTable.getItem(backupProfileTable.firstItemId());
		return	new BackupProfile(backupprofiletableRow.getItemProperty("name"),backupprofiletableRow.getItemProperty("username"), 
							backupprofiletableRow.getItemProperty("password"), 
							backupprofiletableRow.getItemProperty("enable"),
							backupprofiletableRow.getItemProperty("connection"), 
							backupprofiletableRow.getItemProperty("auto_enable")
							);
	}

	@SuppressWarnings("deprecation")
	public Container getSnmpProfileContainer() throws SQLException {
        	List<String> primarykeys = new ArrayList<String>();
        	primarykeys.add("name");
			return new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
	}

	@SuppressWarnings("deprecation")
	public Container getBackupProfileContainer() throws SQLException {
        	List<String> primarykeys = new ArrayList<String>();
        	primarykeys.add("name");
			return new SQLContainer(new FreeformQuery("select * from isi.asset_profiles", primarykeys,m_pool));
	}

    public String[] getDefaultValuesFromNetworkCategory(Object networkcategory) {
    	if (networkcategory == null)
        	return m_network_categories[0];
    	String[] netcat = (String[]) networkcategory;
    	for (int i = 0; i< m_network_categories.length;i++) {
    	   	if (netcat[0].equals(m_network_categories[i][0]) &&
        			netcat[1].equals(m_network_categories[i][1]))
    	    	return m_network_categories[i];
    	}
    	return m_network_categories[0];
    }



}
