/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import static junit.framework.Assert.assertEquals;

public class ProvisionRequisitionServiceTest {
    
    private JerseyProvisionRequisitionService m_requisitionservice;
    
    @Before
    public void setUp() throws Exception {
        m_requisitionservice = new JerseyProvisionRequisitionService();
        JerseyClientImpl jerseyClient = new JerseyClientImpl(
                                                         "http://demo.arsinfo.it:8980/opennms/rest/","admin","admin");
        m_requisitionservice.setJerseyClient(jerseyClient);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testList() throws Exception {
        RequisitionCollection nodelist = m_requisitionservice.getAll();
        assertEquals(7, nodelist.size());
        
        Requisition TN = m_requisitionservice.get("UpdateTN");
        assertEquals(21, TN.getNodeCount());

    }
    @Test
    public void testCityUpdate() {
    	RequisitionNode node = m_requisitionservice.getNode("smichele.mtr01", "UpdateTN");
    	assertEquals("San Michele all'Adige", node.getCity());
		MultivaluedMap< String, String> form = new MultivaluedMapImpl();
		form.add("city", "Positano");
		m_requisitionservice.update("UpdateTN", "smichele.mtr01", form);
    	RequisitionNode node2 = m_requisitionservice.getNode("smichele.mtr01", "UpdateTN");
    	assertEquals("Positano", node2.getCity());
		
    }

    @Test 
    public void testAddAsset() {
    	RequisitionAsset asset = new RequisitionAsset("address1", "prova antonio");
    	m_requisitionservice.add("UpdateTN", "smichele.mtr01", asset);
    	RequisitionNode node = m_requisitionservice.getNode("smichele.mtr01", "UpdateTN");
    	assertEquals("prova antonio", node.getAsset("address1").getValue());
    	m_requisitionservice.add("UpdateTN", "smichele.mtr01", new RequisitionAsset("address1", "via Biasi 1/A"));
    }
    
    @Test
    public void addRequisition() {
    	Requisition home = new Requisition("Home");
    	RequisitionNode node = new RequisitionNode();
    	node.setCity("Positano");
    	node.setForeignId("rssntn67");
    	node.setNodeLabel("rssntn67.arsinfo.it");
    	home.putNode(node);
    	m_requisitionservice.add(home);

    	RequisitionNode nodeA = new RequisitionNode();
    	nodeA.setCity("Positano");
    	nodeA.setForeignId("roberta");
    	nodeA.setNodeLabel("roberta.arsinfo.it");
    	m_requisitionservice.add("Home",nodeA);

    	RequisitionCategory category = new RequisitionCategory("CasaDolceCasa");
    	m_requisitionservice.add("Home", "roberta", category);
    	m_requisitionservice.add("Home", "rssntn67", category);

    	RequisitionAsset asset = new RequisitionAsset("connection", "telnet");
    	m_requisitionservice.add("Home", "roberta", asset);
    	m_requisitionservice.add("Home", "rssntn67", asset);    	

    	RequisitionInterface iface = new RequisitionInterface();
    	iface.setDescr("Manually added by Antonio to test");
    	iface.setIpAddr("10.10.10.1");
    	iface.setSnmpPrimary(PrimaryType.PRIMARY);
    	m_requisitionservice.add("Home", "roberta", iface);
    	RequisitionInterface iface2 = new RequisitionInterface();
    	iface2.setDescr("Manually added by Antonio to test");
    	iface2.setIpAddr("10.10.10.2");
    	iface2.setSnmpPrimary(PrimaryType.PRIMARY);
       	m_requisitionservice.add("Home", "rssntn67", iface2);
      	RequisitionInterface iface3 = new RequisitionInterface();
    	iface3.setDescr("Manually added by Antonio to test");
    	iface3.setIpAddr("10.10.10.3");
    	iface3.setSnmpPrimary(PrimaryType.SECONDARY);
       	m_requisitionservice.add("Home", "rssntn67", iface3);

       	RequisitionMonitoredService icmp = new RequisitionMonitoredService("ICMP");
    	m_requisitionservice.add("Home", "rssntn67", "10.10.10.2", icmp);
    	m_requisitionservice.add("Home", "rssntn67", "10.10.10.3", icmp);
    	m_requisitionservice.add("Home", "roberta", "10.10.10.1", icmp);
    	RequisitionMonitoredService http = new RequisitionMonitoredService("HTTP");
    	m_requisitionservice.add("Home", "roberta", "10.10.10.1", http);

    	m_requisitionservice.delete("Home", "roberta", "10.10.10.1", http);
   	    m_requisitionservice.delete("Home", "rssntn67", iface3);
   	    m_requisitionservice.delete("Home", "rssntn67", category);
   	    m_requisitionservice.delete("Home", "rssntn67", asset);
    	m_requisitionservice.delete("Home", node);
    	m_requisitionservice.delete("Home");
    }
}
