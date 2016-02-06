package org.opennms.vaadin.provision.dashboard;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dao.TNDao;

public class DashBoardServiceTest {

    @Test
    public void testArrayEquals() {
    	String[] netcat = {"Core","Backbone"};
    	if (netcat[0].equals(TNDao.m_network_categories[0][0]) &&
    			netcat[1].equals(TNDao.m_network_categories[0][1])) {
    		System.out.println("Well Done");
    	} else {
    		System.out.println("Nay");    		
    	}
    }
    
    @Test
    public void testInvalidDnsLabel() {
    	assertTrue(DashBoardUtils.hasInvalidDnsBind9Label(null));
    	assertTrue(DashBoardUtils.hasInvalidDnsBind9Label(""));
    	assertFalse(DashBoardUtils.hasInvalidDnsBind9Label("antonio"));
    	assertFalse(DashBoardUtils.hasInvalidDnsBind9Label("antonio01"));
    	assertFalse(DashBoardUtils.hasInvalidDnsBind9Label("antonio.pi.tn.it"));
    	assertTrue(DashBoardUtils.hasInvalidDnsBind9Label("antonio"
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldlak"
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldlak"
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldlak"
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldlak"
    			+ ".pi.tn.it"));
    	assertTrue(DashBoardUtils.hasInvalidDnsBind9Label("antonio."
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldl."
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldl."
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldl."
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldl."
    			+ "laklkdlakldkalkdlakladklakdlakalkdalkdalkdalkadlldl."
    			+ ".pi.tn.it"));
    }

    
    @Test
    public void testInvalidIp() {
    	assertTrue(DashBoardUtils.hasInvalidIp(null));
    	assertTrue(DashBoardUtils.hasInvalidIp("antonio"));
    	assertTrue(DashBoardUtils.hasInvalidIp("0.0.0.0"));
    	assertTrue(DashBoardUtils.hasInvalidIp("127.0.0.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("127.0.0.a"));
    	assertTrue(DashBoardUtils.hasInvalidIp("a.0.0.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("0.a.0.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("1.0.a.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("256.0.0.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("0.256.0.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("0.0.256.1"));
    	assertTrue(DashBoardUtils.hasInvalidIp("0.0.0.256"));
    	assertTrue(DashBoardUtils.hasInvalidIp("255.0.0.1.2"));
    	assertFalse(DashBoardUtils.hasInvalidIp("10.0.1.2"));

    }
}
