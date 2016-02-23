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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.web.svclayer.model.SnmpInfo;

import static junit.framework.Assert.assertEquals;

public class SnmpInfoServiceTest {
    
    private JerseySnmpInfoService m_snmpinfoservice;
    
    @Before
    public void setUp() throws Exception {
        m_snmpinfoservice = new JerseySnmpInfoService();
        JerseyClientImpl jerseyClient = new JerseyClientImpl(
                                                         "http://demo.arsinfo.it:8980/opennms/rest/","admin","admin");
        m_snmpinfoservice.setJerseyClient(jerseyClient);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    @Ignore
    public void testGet() throws Exception {
    	SnmpInfo snmp = m_snmpinfoservice.get("172.25.140.97");
    	assertEquals("ma14na165ge", snmp.getReadCommunity());
    	assertEquals(161, snmp.getPort().intValue());
    	assertEquals(1, snmp.getRetries().intValue());
    	assertEquals(5000, snmp.getTimeout().intValue());
    	assertEquals("v2c", snmp.getVersion());

    }

    @Test
    @Ignore
    public void testSet() throws Exception {
    	SnmpInfo snmp = new SnmpInfo();
    	snmp.setReadCommunity("sarag8");
    	snmp.setVersion("v1");
    	snmp.setTimeout(1890);
    	m_snmpinfoservice.set("10.10.1.1", snmp);
    	
    	SnmpInfo snmpstored = m_snmpinfoservice.get("10.10.1.1");
    	assertEquals("sarag8", snmpstored.getReadCommunity());
    	assertEquals(161, snmpstored.getPort().intValue());
    	assertEquals(1, snmpstored.getRetries().intValue());
    	assertEquals(1890, snmpstored.getTimeout().intValue());
    	assertEquals("v1", snmpstored.getVersion());
    	
    	SnmpInfo snmp2 = new SnmpInfo();
    	snmp2.setReadCommunity("sarag8");
    	snmp2.setTimeout(1800);
    	snmp2.setVersion("v2c");
    	m_snmpinfoservice.set("10.10.1.1", snmp2);
    	
    	SnmpInfo snmpstored2 = m_snmpinfoservice.get("10.10.1.1");
    	assertEquals("sarag8", snmpstored2.getReadCommunity());
    	assertEquals(161, snmpstored2.getPort().intValue());
    	assertEquals(1, snmpstored2.getRetries().intValue());
    	assertEquals(1800, snmpstored2.getTimeout().intValue());
    	assertEquals("v2c", snmpstored2.getVersion());
    	
    }
}
