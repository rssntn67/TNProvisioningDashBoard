package org.opennms.vaadin.provision.dashboard;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicNode;

public class DashBoardServiceTest {

    
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
    
    @Test
    public void testPrimary() throws Exception {
    	BasicNode node = new BasicNode("prova", "prova");
    	node.setPrimary("1.1.1.1");
    	System.out.println("-----set primary 1.1.1.1----------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	node.setPrimary("2.2.2.2");
    	System.out.println("-----set primary 2.2.2.2----------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	node.clear();
    	System.out.println("------------clear=saved-------------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	node.setPrimary("1.1.1.1");
    	System.out.println("-----set primary 1.1.1.1----------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	node.addService("2.2.2.2", "HTTP");
    	System.out.println("--------added 2.2.2.2/HTTP----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	node.clear();
    	System.out.println("------------clear=saved-------------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.addService("2.2.2.2", "HTTPS");
    	node.addService("3.3.3.3", "HTTPS");
    	node.addService("4.4.4.4", "HTTPS");
    	System.out.println("--------added HTTPS----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.delService("1.1.1.1", "HTTP");
    	System.out.println("--------removed not existent HTTP----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.delService("3.3.3.3", "HTTPS");
    	System.out.println("--------removed 3.3.3.3 HTTPS----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.delService("2.2.2.2", "HTTP");
    	System.out.println("--------removed 2.2.2.2 HTTP----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	node.clear();
    	System.out.println("------------clear=saved-------------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.delService("2.2.2.2", "HTTPS");
    	System.out.println("--------removed 2.2.2.2 HTTPS----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	node.clear();
    	System.out.println("------------clear=saved-------------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());


    }


}
