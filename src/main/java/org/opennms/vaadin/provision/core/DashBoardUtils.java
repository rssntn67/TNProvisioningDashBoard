package org.opennms.vaadin.provision.core;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.model.BackupProfile;

public class DashBoardUtils {

	public static final String[] m_network_levels = {
		"Backbone",
		"Distribuzione",
		"Accesso"
	};

	public static final String[] m_notify_levels = {
		"EMERGENCY_F0",
		"EMERGENCY_F1",
		"EMERGENCY_F2",
		"EMERGENCY_F3",
		"EMERGENCY_F4",
		"INFORMATION"
	};

	public static final String[] m_threshold_levels = {
		"ThresholdWARNING",
		"ThresholdALERT"
	};
	
	public static final String CITY    = "city";
	public static final String ADDRESS1 = "address1";

	public static final String m_fast_default_notify = "Default";
	
	public static boolean hasInvalidIp(String ip) {
		if (ip == null)
			return true;
		String re ="^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"; 
		if (!ip.matches(re))	
			return true;
		if (ip.equals("127.0.0.1"))
			return true;
		if (ip.equals("0.0.0.0"))
			return true;
		return false;
	}

	public static boolean hasInvalidDnsBind9Label(String nodelabel ) {
		if (nodelabel == null)
			return true;
		if (nodelabel.length() > 253)
			return true;
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split("\\.")) {
			if (!label.matches(re))	
				return true;
			if (label.length() > 63)
				return true;
		}
		return false;
	}
	
	public static boolean hasUnSupportedDnsDomain(String hostname, String nodelabel, List<String> sub_domains) {
		if (hostname == null)
			return true;
		if (hostname.contains(".")) {
			String hostlabel = hostname.substring(0,hostname.indexOf("."));
			for (String subdomain: sub_domains ) {
				if (nodelabel.equals(hostlabel+"."+subdomain)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	public static PolicyWrapper getPolicyWrapper(String ipaddr) {
		PolicyWrapper manage = new PolicyWrapper();
    	manage.setName(getPolicyName(ipaddr));
    	manage.setPluginClass("org.opennms.netmgt.provision.persist.policies.MatchingIpInterfacePolicy");
    	manage.addParameter("action", "MANAGE");
    	manage.addParameter("matchBehavior", "ALL_PARAMETERS");
    	manage.addParameter("ipAddress", "~^"+ipaddr+"$");		
    	return manage;
	}
	
	public static String getPolicyName(String ipaddr) {
		return "Manage"+ipaddr;
	}

	public static String getBackupProfile(RequisitionNode requisitionNode,Map<String,BackupProfile> backupprofilemap) {
		for (String profileId: backupprofilemap.keySet()) {
			RequisitionNode profile = backupprofilemap.get(profileId).getRequisitionAssets();
		
			if (requisitionNode.getAsset("username") != null && profile.getAsset("username") != null 
			 && requisitionNode.getAsset("username").getValue().equals(profile.getAsset("username").getValue()) 
		     && requisitionNode.getAsset("password") != null && profile.getAsset("password") != null 
		     && requisitionNode.getAsset("password").getValue().equals(profile.getAsset("password").getValue())
			 && requisitionNode.getAsset("enable") != null   && profile.getAsset("enable") != null 
			 && requisitionNode.getAsset("enable").getValue().equals(profile.getAsset("enable").getValue())
			 && requisitionNode.getAsset("connection") != null && profile.getAsset("connection") != null 
			 && requisitionNode.getAsset("connection").getValue().equals(profile.getAsset("connection").getValue())
			 && (
					 (requisitionNode.getAsset("autoenable") != null && profile.getAsset("autoenable") != null 
					 && requisitionNode.getAsset("autoenable").getValue().equals(profile.getAsset("autoenable").getValue()))
			 	|| (requisitionNode.getAsset("autoenable") == null 
			 	   && (profile.getAsset("autoenable") == null || profile.getAsset("autoenable").getValue().equals("")	
			)))
			) {
				return profileId;
			}
		}
		return null;
	}

	public static final String TN = "TrentinoNetwork";




}
