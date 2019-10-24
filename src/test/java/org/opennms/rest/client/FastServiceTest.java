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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAsset.Meta;
import org.opennms.rest.client.model.FastAssetAttributes;
import org.opennms.rest.client.model.FastOrder;
import org.opennms.vaadin.provision.core.DashBoardUtils;
import org.opennms.vaadin.provision.dashboard.FastRunnable;

public class FastServiceTest {
    
    private static final Logger logger = Logger.getLogger(FastRunnable.class.getName());
    private JerseyFastService m_fastservice;
    
    private JerseyClientImpl m_jerseyClient;
    
    @Before
    public void setUp() throws Exception {
        m_fastservice = new JerseyFastService();
        m_jerseyClient = 
                new JerseyClientImpl("https://fast-api.si.tnnet.it/api/v1/fast/","arusso","conferenzaatrento");
        m_fastservice.setJerseyClient(m_jerseyClient);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testGetAssets() throws Exception {
           	FastAsset[] assets = m_fastservice.getAssets();
                assertNotNull(assets);
           	Set<Meta> meta = EnumSet.noneOf(Meta.class); 
           	for (FastAsset asset: assets) {
           	    assertNotNull(asset.getMeta());
                    assertNotNull(asset.getOrder_id());
                    assertNotNull(asset.getId());
                    assertNull(asset.getAttributes());
                    meta.add(asset.getMeta());
                    if (asset.getMeta() == Meta.Internet) {
                        System.out.println(m_fastservice.getAssetWithAttributesById(asset.getId()));
                    }
           	}
           	
           	System.out.println(meta);
    }

    @Test
    public void testGetOrders() throws Exception {
                FastOrder[] orders = m_fastservice.getOrders();
                assertNotNull(orders);
                Set<String> meta = new HashSet<>(); 
                for (FastOrder order: orders) {
                    assertNotNull(order.getOrder_state());
                    meta.add(order.getOrder_state());
                    if (!order.produzione()) {
                        System.out.println(order);
                    }
                }
                
                System.out.println(meta);
    }

    @Test
    public void testGetAssetById() throws Exception {
                FastAsset asset = m_fastservice.getAssetById(Long.valueOf(198263));
                assertNotNull(asset);
                assertEquals(220769, asset.getOrder_id().intValue());
                System.out.println(asset);
    }

    @Test
    public void testGetOrderById() throws Exception {
                FastOrder order = m_fastservice.getOrderById(Long.valueOf(220769));
                assertNotNull(order);
                System.out.println(order);
    }

    @Test
    public void testGetAssetWithAttributesById() throws Exception {
                FastAsset asset = m_fastservice.getAssetWithAttributesById(Long.valueOf(198263));
                assertNotNull(asset);
                System.out.println(asset);
    }

    @Test
    public void testGetFastAssetAttributesById() throws Exception {
                FastAssetAttributes attributes = m_fastservice.getAssetAttributesById(Long.valueOf(198263));
                assertNotNull(attributes);
                System.out.println(attributes);
    }

    @Test
    public void testGetAttributesString() throws Exception {
        String result = m_jerseyClient.get("/assets/198263/attrs");
        assertNotNull(result);
        System.out.println(result);
    }
    
    @Test
    public void testValidRouter() throws Exception {
        logger.info("run: loading fast Asset: " + Meta.Router);
        int total = 0;
        int valid = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.Router)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (!asset.getAttributes().monitorato()) {
                valid++;
                continue;
            }
            if (isValid(asset)) {
                valid++;
            }
        }
        logger.info("run: loaded requisition: " + Meta.Router);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        int check = total-valid;
        System.out.println("error: " + check);
    }
    
    @Test
    public void testValidSwitch() throws Exception {
        logger.info("run: loading fast Asset: " + Meta.Switch);
        int total = 0;
        int valid = 0;
        int check = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.Switch)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (isValid(asset)) {
                valid++;
                if (checkFastAsset(asset)) {
                    check++;
                }
            }
        }
        logger.info("run: loaded requisition: " + Meta.Switch);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        System.out.println("check: " + check);
    }

    @Test
    public void testValidFirewall() throws Exception {

        logger.info("run: loading fast Asset: " + Meta.Firewall);
        int total = 0;
        int valid = 0;
        int check = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.Firewall)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (isValid(asset)) {
                valid++;
                if (checkFastAsset(asset)) {
                    check++;
                }
            }
        }
        logger.info("run: loaded requisition: " + Meta.Firewall);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        System.out.println("check: " + check);
    }
        
    @Test
    public void testValidRadio() throws Exception {
        logger.info("run: loading fast Asset: " + Meta.Radio);
        int total = 0;
        int valid = 0;
        int check = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.Radio)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (isValid(asset)) {
                valid++;
                if (checkFastAsset(asset)) {
                    check++;
                }
            }
        }
        logger.info("run: loaded requisition: " + Meta.Radio);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        System.out.println("check: " + check);
    }
    @Test
    public void testValidWireless() throws Exception {
        logger.info("run: loading fast Asset: " + Meta.Wireless);
        int total = 0;
        int valid = 0;
        int check = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.Wireless)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (isValid(asset)) {
                valid++;
                if (checkFastAsset(asset)) {
                    check++;
                }
            }
        }
        logger.info("run: loaded requisition: " + Meta.Wireless);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        System.out.println("check: " + check);
    }

    @Test
    public void testValidModem() throws Exception {
        logger.info("run: loading fast Asset: " + Meta.Modem);
        int total = 0;
        int valid = 0;
        int check = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.Modem)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (isValid(asset)) {
                valid++;
                if (checkFastAsset(asset)) {
                    check++;
                }
            }
        }
        logger.info("run: loaded requisition: " + Meta.Modem);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        System.out.println("check: " + check);
    }

    @Test
    public void testValidMediaGw() throws Exception {

        logger.info("run: loading fast Asset: " + Meta.MediaGW);
        int total = 0;
        int valid = 0;
        int check = 0;
        for (FastAsset asset: m_fastservice.getAssetsByMeta(Meta.MediaGW)) {
            total++;
            assertNotNull(asset.getAttributes());
            if (isValid(asset)) {
                valid++;
                if (checkFastAsset(asset)) {
                    check++;
                }
            }
        }
        logger.info("run: loaded requisition: " + Meta.MediaGW);
        System.out.println("total: " + total);
        System.out.println("valid: " + valid);
        System.out.println("check: " + check);
    }

    private boolean isValid(FastAsset device) {
        boolean valid = true;
        if (device.getAttributes().getIndirizzoIP() == null) {
            System.err.println("Ip null: "+device);
            valid = false;
        }  else if (DashBoardUtils.hasInvalidIp(device.getAttributes().getIndirizzoIP())) {
            System.err.println("Ip invalido: "+device);
            valid = false;
        } 
        if (device.getAttributes().getHostName() == null) {
            System.err.println("HostName null: "+device);
            valid = false;
        } else if (DashBoardUtils.hasInvalidDnsBind9Label(device.getAttributes().getHostName())) {
            System.err.println("HostName invalido: "+device);
            valid = false;
        } 
        if (device.getAttributes().getVrf() == null) {
            System.err.println("Vrf: " + device);
            valid = false;
        }
        if (device.getOrder_id() == null) {
            System.err.println("OrderId: "+device);
            valid = false;
        }

        return valid;
    }

    private boolean checkFastAsset(FastAsset device) {
        boolean valid = true;
        if (device.getAttributes().getDominio() == null) {
            System.out.println("Dominio: " + device);
            valid = false;
        }
        
        if (device.getAttributes().getProfiloSNMP() == null) {
            System.out.println("Profilo SNMP: "+device);
            valid = false;
        }
        if (device.getAttributes().getProfiloBackup() == null) {
            System.out.println("Profilo Backup: " + device);
                      valid = false;
        }
        return valid;
        
    }

}
