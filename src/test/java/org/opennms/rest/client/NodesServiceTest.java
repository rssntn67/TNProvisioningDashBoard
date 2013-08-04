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
import org.opennms.core.test.MockLogAppender;
import org.opennms.rest.client.model.OnmsIpInterface;
import org.opennms.rest.client.model.OnmsIpInterfaceList;
import org.opennms.rest.client.model.OnmsNode;
import org.opennms.rest.client.model.OnmsNodeList;
import org.opennms.vaadin.provision.dashboard.TrentinoNetworkRequisitionNode;

import com.sun.jersey.core.util.MultivaluedMapImpl;

import static junit.framework.Assert.assertEquals;

public class NodesServiceTest {
    
    private JerseyNodesService m_nodesservice;
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_nodesservice = new JerseyNodesService();
        JerseyClientImpl jerseyClient = new JerseyClientImpl(
                                                         "http://demo.arsinfo.it:8980/opennms/rest/","admin","admin");
        m_nodesservice.setJerseyClient(jerseyClient);
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @Test
    public void testNodes() throws Exception {
        OnmsNodeList nodeslist = m_nodesservice.getAll();
        assertEquals(36, nodeslist.getCount());
        assertEquals(36,nodeslist.getTotalCount());
        for (OnmsNode node: nodeslist){
        	System.out.println(node);
        }
    }

    @Test
    public void testGetNodesByNodelabel() throws Exception {
    	MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
    	queryParams.add("label", "ASR9K-1.bb.tnnet.it");
    	queryParams.add("foreignSource", "TrentinoNetworkTest");
        OnmsNodeList nodeslist = m_nodesservice.find(queryParams);
        assertEquals(1, nodeslist.getCount());
        assertEquals(1,nodeslist.getTotalCount());
        OnmsNode node = nodeslist.getFirst();
        assertEquals(942, node.getId().intValue());
    }

    @Test
    public void testGetNode() throws Exception {
        OnmsNode node = m_nodesservice.get(942);
        assertEquals(942, node.getId().intValue());
        assertEquals("ASR9K-1.bb.tnnet.it", node.getLabel());
        assertEquals("ASR9K-1", node.getForeignId());
        assertEquals("TrentinoNetworkTest", node.getForeignSource());
    }

    @Test
    public void testGetIpAddresses() throws Exception {
    	OnmsIpInterfaceList ips = m_nodesservice.getIpInterfaces(942);
    	assertEquals(14, ips.size());
    	for (OnmsIpInterface ip: ips.getInterfaces()) {
    		System.out.println(ip.getIpAddress());
    		if (ip.getSnmpInterface() != null)
    			System.out.println(ip.getSnmpInterface().getNetMask());
    		System.out.println(ip.getIpHostName());
    	}
    }
    
    @Test
    public void testMatchSubDomain() {
    	String hostname = "prova.alv01";
    	String nodelabel = "prova.alv01.wl.tnnet.it";
		System.out.println(nodelabel);
		if (hostname.contains(".")) {
			String hostlabel = hostname.substring(0,hostname.indexOf("."));
			System.out.println(hostlabel);
			for (String subdomain: TrentinoNetworkRequisitionNode.m_sub_domains ) {
				if (nodelabel.equals(hostlabel+"."+subdomain))
					System.out.println(hostlabel+"."+subdomain);
			}
		}

    }
    
    @Test
    public void testUnMatchSubDomain() {
    	String hostname = "prova.alv07";
    	String nodelabel = "prova.alv07.wl.tnnet.it";
		System.out.println(nodelabel);
		if (hostname.contains(".")) {
			String hostlabel = hostname.substring(0,hostname.indexOf("."));
			System.out.println(hostlabel);
			for (String subdomain: TrentinoNetworkRequisitionNode.m_sub_domains ) {
				if (nodelabel.equals(hostlabel+"."+subdomain))
					System.out.println(hostlabel+"."+subdomain);
			}
		}

    }

    @Test
    public void testDnsLabel() {
    	String nodelabel = "prova.alv01.wl.tnnet.it";
    	String regularExpression = "\\.";
    	System.out.println("Prova che almeno scrivo......");
		for (String label: nodelabel.split(regularExpression) ) {
			System.out.println(label);
		}
    }

    @Test
    public void testDnsLabelSyntax() {
    	String nodelabel = "pro-va.alv01.wl.tnnet.it";
    	String regularExpression = "\\.";
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split(regularExpression) ) {
			System.out.println(label);
			System.out.println(label.matches(re));
		}
    }
    
    @Test
    public void testDnsLabelSyntaxWithHyphen() {
    	String nodelabel = "pro_va.alv01.wl.tnnet.it";
    	String regularExpression = "\\.";
    	String re ="^[a-zA-Z0-9]+[a-zA-Z0-9-\\-]*[a-zA-Z0-9]+";
		for (String label: nodelabel.split(regularExpression) ) {
			System.out.println(label);
			System.out.println(label.matches(re));
		}
    }

}
