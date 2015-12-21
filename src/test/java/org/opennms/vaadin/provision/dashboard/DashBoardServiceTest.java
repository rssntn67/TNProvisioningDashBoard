package org.opennms.vaadin.provision.dashboard;

import org.junit.Test;

public class DashBoardServiceTest {

    @Test
    public void testArrayEquals() {
    	String[] netcat = {"Core","Backbone"};
    	if (netcat[0].equals(DashBoardService.m_network_categories[0][0]) &&
    			netcat[1].equals(DashBoardService.m_network_categories[0][1])) {
    		System.out.println("Well Done");
    	} else {
    		System.out.println("Nay");
    		
    	}
    }

}
