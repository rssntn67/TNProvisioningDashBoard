package org.opennms.vaadin.provision.dashboard;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.model.BasicInterface;
import org.opennms.vaadin.provision.model.BasicInterface.OnmsPrimary;
import org.opennms.vaadin.provision.model.BasicNode;
import org.opennms.vaadin.provision.model.BasicService;

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
    public void testPrimaryUpdate() throws Exception {
    	BasicNode node = new BasicNode("prova", "prova");
    	node.setPrimary("1.1.1.1");
    	BasicInterface two = new BasicInterface();
    	two.setIp("2.2.2.2");
    	two.setDescr(DashBoardUtils.DESCR_TNPD);
    	two.setOnmsprimary(OnmsPrimary.N);
    	BasicService http2 = new BasicService(two);
    	http2.setService("HTTP");
    	node.addService(http2);
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
    	
    	BasicInterface one = new BasicInterface();
    	one.setIp("1.1.1.1");
    	one.setDescr(DashBoardUtils.DESCR_TNPD);
    	one.setOnmsprimary(OnmsPrimary.N);
    	BasicService http1 = new BasicService(one);
    	http1.setService("HTTP");
    	node.addService(http1);
    	node.setPrimary("10.10.10.10");
    	System.out.println("------------1.1.1.1/HTTP primary 10.10.10.10-------------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());
    	
    	
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
    	BasicInterface two = new BasicInterface();
    	two.setIp("2.2.2.2");
    	two.setDescr(DashBoardUtils.DESCR_TNPD);
    	two.setOnmsprimary(OnmsPrimary.N);
    	BasicService http2 = new BasicService(two);
    	http2.setService("HTTP");
    	node.addService(http2);
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
    	BasicService https2 = new BasicService(two);
    	https2.setService("HTTPS");

    	BasicInterface three = new BasicInterface();
    	three.setIp("3.3.3.3");
    	three.setDescr(DashBoardUtils.DESCR_TNPD);
    	three.setOnmsprimary(OnmsPrimary.N);
    	BasicService https3 = new BasicService(three);
    	https3.setService("HTTPS");

    	BasicInterface four = new BasicInterface();
    	four.setIp("4.4.4.4");
    	four.setDescr(DashBoardUtils.DESCR_TNPD);
    	four.setOnmsprimary(OnmsPrimary.N);
    	BasicService https4 = new BasicService(four);
    	https4.setService("HTTPS");

    	node.addService(https2);
    	node.addService(https3);
    	node.addService(https4);
    	System.out.println("--------added HTTPS----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	BasicInterface one = new BasicInterface();
    	one.setIp("1.1.1.1");
    	one.setDescr(DashBoardUtils.DESCR_TNPD);
    	one.setOnmsprimary(OnmsPrimary.N);
    	BasicService http1 = new BasicService(one);
    	http1.setService("HTTP");
    	node.delService(http1);
    	System.out.println("--------removed not existent HTTP----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.delService(https3);
    	System.out.println("--------removed 3.3.3.3 HTTPS----------------");
    	System.out.println("status:"+ node.getOnmstate());
    	System.out.println("updatemap:"+ node.getUpdatemap());
    	System.out.println("syncoperation"+ node.getSyncOperations());
    	System.out.println("serviceMap:"+node.getServiceMap());
    	System.out.println("interfaceToAdd:"+node.getInterfToAdd());
    	System.out.println("interfaceToDel:"+node.getInterfToDel());
    	System.out.println("serviceToAdd:"+node.getServiceToAdd());
    	System.out.println("serviceToDel:"+node.getServiceToDel());

    	node.delService(http2);
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

    	node.delService(https2);
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
