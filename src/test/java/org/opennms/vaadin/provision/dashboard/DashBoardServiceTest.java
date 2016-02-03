package org.opennms.vaadin.provision.dashboard;

import org.junit.Test;
import org.opennms.vaadin.provision.model.TrentinoNetworkRequisitionNode;

public class DashBoardServiceTest {

    @Test
    public void testArrayEquals() {
    	String[] netcat = {"Core","Backbone"};
    	if (netcat[0].equals(TrentinoNetworkRequisitionNode.m_network_categories[0][0]) &&
    			netcat[1].equals(TrentinoNetworkRequisitionNode.m_network_categories[0][1])) {
    		System.out.println("Well Done");
    	} else {
    		System.out.println("Nay");    		
    	}
    }

}
