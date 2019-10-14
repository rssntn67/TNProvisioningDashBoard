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


import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.rest.client.model.FastAsset;
import org.opennms.rest.client.model.FastAssetAttributes;

public class FastServiceTest {
    
    private JerseyFastService m_fastservice;
    
    private JerseyClientImpl m_jerseyClient;
    
    @Before
    public void setUp() throws Exception {
        m_fastservice = new JerseyFastService();
        m_jerseyClient = 
                new JerseyClientImpl("https://localhost/api/v1/fast/","admin","admin");
        m_fastservice.setJerseyClient(m_jerseyClient);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testGetAssets() throws Exception {
           	FastAsset[] assets = m_fastservice.getAssets();
           	assertNotNull(assets);
           	for (FastAsset asset: assets) {
           	    System.out.println(asset);
           	}
    }
    
    @Test
    public void testGetAssetById() throws Exception {
                FastAsset asset = m_fastservice.getAssetById(198263);
                assertNotNull(asset);
                System.out.println(asset);
    }

    @Test
    public void testGetAssetWithAttributesById() throws Exception {
                FastAsset asset = m_fastservice.getAssetWithAttributesById(198263);
                assertNotNull(asset);
                System.out.println(asset);
    }

    @Test
    public void testGetFastAssetAttributesById() throws Exception {
                FastAssetAttributes attributes = m_fastservice.getFastAssetAttributesByIt(198263);
                assertNotNull(attributes);
                System.out.println(attributes);
    }

    @Test
    public void testGetAttributesString() throws Exception {
        String result = m_jerseyClient.get("/assets/198263/attrs");
        assertNotNull(result);
        System.out.println(result);
    }

}
