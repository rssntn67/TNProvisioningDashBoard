package org.opennms.vaadin.provision.core;

import java.util.Map;

import org.opennms.netmgt.provision.persist.foreignsource.PolicyWrapper;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.vaadin.provision.model.BackupProfile;

public class DashBoardUtils {

	public static boolean hasInvalidDnsBind9Size(String nodelabel ) {		
		if (nodelabel.length() > 253)
			return true;
		return false;
	}

	public static boolean hasInvalidDnsBind9LabelSize(String nodelabel ) {		
		for (String label: nodelabel.split("\\.")) {
			if (label.length() > 63)
				return true;
		}
		return false;
	}

	public static boolean hasInvalidDnsBind9Label(String nodelabel ) {		
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split("\\.")) {
			if (!label.matches(re))	
				return true;
		}
		return false;
	}
	
	public static boolean hasUnSupportedDnsDomain(String hostname, String nodelabel, String[] sub_domains) {
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




}
