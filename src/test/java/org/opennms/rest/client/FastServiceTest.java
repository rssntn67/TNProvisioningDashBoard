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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    
    private static boolean supportedDomain(String domain) {
        for (String validomain: domains) {
            if (domain.equals(validomain))
                return true;
        }
        return false;
    }
    private static String[] domains = {
            "acsm.tnnet.it",
            "aglav.tnnet.it",
            "apss.tnnet.it",
            "bb.tnnet.it",
            "biblio.tnnet.it",
            "comunetn.tnnet.it",
            "comuni.tnnet.it",
            "conspro.tnnet.it",
            "cue.tnnet.it",
            "esterni.tnnet.it",
            "fem.tnnet.it",
            "geosis.tnnet.it",
            "hq.tnnet.it",
            "iasma.tnnet.it",
            "info.tnnet.it",
            "infotn.tnnet.it",
            "internet-esterni.tnnet.it",
            "internet.tnnet.it",
            "medici.tnnet.it",
            "mitt.tnnet.it",
            "multivoce.tnnet.it",
            "operaunitn.tnnet.it",
            "pat.tnnet.it",
            "patacquepub.tnnet.it",
            "patdighe.tnnet.it",
            "patvoce.tnnet.it",
            "pat-tecnica.tnnet.it",
            "phoenix.tnnet.it",
            "reperibilitnet.tnnet.it",
            "rsacivicatn.tnnet.it",
            "rsaspes.tnnet.it",
            "scuolematerne.tnnet.it",
            "scuole.tnnet.it",
            "serviziovds.tnnet.it",
            "telpat-autonome.tnnet.it",
            "unitn.tnnet.it",
            "vdsrovereto.tnnet.it",
            "winwinet.tnnet.it",
            "wl.tnnet.it",
            "lan.tnnet.it",
            "noc.tnnet.it",
            "ros.tnnet.it",
            "infotnvoce.tnnet.it",
            "pa.tnnet.it",
            "si.tnnet.it",
            "videoconf.provincia.tn.it",
            "testbed.tnnet.it",
            "trentinonetwork.it",
            "tetra.tnnet.it",
            "unitnvoce.tnnet.it",
            "operavoce.tnnet.it",
            "copvoce.tnnet.it",
            "internetbb.tnnet.it",
            "consrtaa.tnnet.it",
            "win.hq.tnnet.it"
    };
    private JerseyClientImpl m_jerseyClient;
    
    private class Stats {
        @Override
        public String toString() {
            return "total=" + total 
                    + "\nnot monitored=" + notmon
                    + "\nnot production=" + notpro 
                    + "\nvalid=" + valid 
                    + "\nerrori hostname=" + ehos 
                    + "\nerrori ip="+ eip 
                    + "\ncheck=" + check 
                    + "\nerrori vrf=" + evrf 
                    + "\nerrori dominio=" + edom 
                    + "\nerrori profilo snmp="+ esnmp 
                    + "\nerrori profilo backup="+ eback 
                    + "\n" + domini;
        }
        Integer total = 0;
        Integer notmon = 0;
        Integer notpro = 0; 
        Integer valid = 0;
        Integer check = 0;
        Integer edom = 0;
        Integer evrf = 0;
        Integer ehos =0;
        Integer eip =0;
        Integer esnmp =0;
        Integer eback =0;
        Set<String> domini = new HashSet<>();
    }
    
    @Before
    public void setUp() throws Exception {
        m_fastservice = new JerseyFastService();
        m_jerseyClient = 
                new JerseyClientImpl("https://fast-api.si.tnnet.it/api/v1/fast/","arusso","arusso");
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
        logger.info("run: loading fast orders");
        Map<Long,FastOrder> fastOrderMap = new HashMap<>();
        for (FastOrder order : m_fastservice.getOrders()) {
            fastOrderMap.put(order.getOrder_id(), order);
        }
        logger.info("run: loaded fast orders");
        logger.info("run: loading fast Asset: " + Meta.Router);
        FastAsset[] assets = m_fastservice.getAssetsByMeta(Meta.Router);
        logger.info("run: loaded requisition: " + Meta.Router);
        
        Stats stats = new Stats();
        for (FastAsset asset: assets) {
            verify(asset, fastOrderMap, stats);
        }
        System.out.println("stats " + Meta.Router);
        System.out.println(stats);
    }
    
    @Test
    public void testValidSwitch() throws Exception {
        logger.info("run: loading fast orders");
        Map<Long,FastOrder> fastOrderMap = new HashMap<>();
        for (FastOrder order : m_fastservice.getOrders()) {
            fastOrderMap.put(order.getOrder_id(), order);
        }
        logger.info("run: loaded fast orders");
        logger.info("run: loading fast Asset: " + Meta.Switch);
        FastAsset[] assets = m_fastservice.getAssetsByMeta(Meta.Switch);
        logger.info("run: loaded requisition: " + Meta.Switch);
        
        Stats stats = new Stats();
        for (FastAsset asset: assets) {
            verify(asset, fastOrderMap, stats);
        }
        System.out.println("stats " + Meta.Switch);
        System.out.println(stats);
    }
        
    @Test
    public void testValidRadio() throws Exception {
        logger.info("run: loading fast orders");
        Map<Long,FastOrder> fastOrderMap = new HashMap<>();
        for (FastOrder order : m_fastservice.getOrders()) {
            fastOrderMap.put(order.getOrder_id(), order);
        }
        logger.info("run: loaded fast orders");
        logger.info("run: loading fast Asset: " + Meta.Radio);
        FastAsset[] assets = m_fastservice.getAssetsByMeta(Meta.Radio);
        logger.info("run: loaded requisition: " + Meta.Radio);
        
        Stats stats = new Stats();
        for (FastAsset asset: assets) {
            verify(asset, fastOrderMap, stats);
        }
        System.out.println("stats " + Meta.Radio);
        System.out.println(stats);
    }

    @Test
    public void testValidModem() throws Exception {
        logger.info("run: loading fast orders");
        Map<Long,FastOrder> fastOrderMap = new HashMap<>();
        for (FastOrder order : m_fastservice.getOrders()) {
            fastOrderMap.put(order.getOrder_id(), order);
        }
        logger.info("run: loaded fast orders");
        logger.info("run: loading fast Asset: " + Meta.Modem);
        FastAsset[] assets = m_fastservice.getAssetsByMeta(Meta.Modem);
        logger.info("run: loaded requisition: " + Meta.Modem);
        
        Stats stats = new Stats();
        for (FastAsset asset: assets) {
            verify(asset, fastOrderMap, stats);
        }
        System.out.println("stats " + Meta.Modem);
        System.out.println(stats);
    }

    private void verify(
        FastAsset asset, 
        Map<Long,FastOrder> fastOrderMap, 
        Stats stats) {
        stats.total++;
        assertNotNull(asset.getAttributes());
        if (!asset.getAttributes().monitorato()) {
            stats.notmon++;
            return;
        }
        assertNotNull(fastOrderMap.get(asset.getOrder_id()));
        if (!fastOrderMap.get(asset.getOrder_id()).produzione()) {
            stats.notpro++;
            return;
        }
        if (isValid(asset,stats)) {
            stats.valid++;
            checkFastAsset(asset,stats);
        }

    }
    private boolean isValid(FastAsset device, Stats stats) {
        boolean valid = true;
        if (device.getAttributes().getIndirizzoIP() == null) {
            stats.eip++;
            System.err.println("Ip null: "+device);
            valid = false;
        }  else if (DashBoardUtils.hasInvalidIp(device.getAttributes().getIndirizzoIP().trim())) {
            stats.eip++;
            System.err.println("Ip invalido: "+device);
            valid = false;
        } 
        if (device.getAttributes().getHostName() == null) {
            stats.ehos++;
            System.err.println("HostName null: "+device);
            valid = false;
        } else if (DashBoardUtils.hasInvalidDnsBind9Label(device.getAttributes().getHostName().trim())) {
            stats.ehos++;
            System.err.println("HostName invalido: "+device);
            valid = false;
        } 
        return valid;
    }

    private void checkFastAsset(FastAsset device, Stats stats) {
        stats.check++;
        if (device.getAttributes().getVrf() == null) {
            stats.evrf++;
            System.err.println("Vrf null: " + device);
            stats.check--;
        }
        if (device.getAttributes().getDominio() != null && !supportedDomain(device.getAttributes().getDominio())) {
            stats.edom++;
            System.err.println("Dominio invalido: " + device);
            stats.check--;
        } else {
            stats.domini.add(device.getAttributes().getDominio());
        }
        if (device.getAttributes().getProfiloSNMP() == null) {
            System.out.println("Profilo SNMP: "+device.getAttributes());
            stats.esnmp++;
            stats.check--;
        }
        if (device.getAttributes().getBackup() != null && device.getAttributes().getBackup() ==  1 
            && device.getAttributes().getProfiloBackup() == null) {
            System.out.println("Profilo Backup: " + device.getAttributes());
            stats.eback++;
            stats.check--;
        }                    
    }

}
