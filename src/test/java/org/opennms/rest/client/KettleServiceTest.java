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
import org.junit.Test;
import org.opennms.rest.client.model.KettleJobStatus;
import org.opennms.rest.client.model.KettleRunJob;

import static junit.framework.Assert.assertEquals;

public class KettleServiceTest {
    
    private JerseyKettleService m_kettleservice;
    
    @Before
    public void setUp() throws Exception {
        m_kettleservice = new JerseyKettleService();
        JerseyClientImpl jerseyClient = new JerseyClientImpl(
                                                         "http://prdbi01.noc.tnnet.it:8080/kettle/","admin","3network");
        m_kettleservice.setJerseyClient(jerseyClient);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testKettle() throws Exception {
    	KettleRunJob job = m_kettleservice.runJob();
    	assertEquals("Job started", job.getMessage());
    	assertEquals("OK", job.getResult());
    	
    	KettleJobStatus status = m_kettleservice.jobStatus(job);
    	assertEquals(status.getId(), job.getId());
    	assertEquals("staging_fast", status.getJobname());
    	assertEquals("Finished", status.getStatusDescr());
    	assertEquals("Y", status.getResult().getResult());
       	assertEquals(0, Integer.valueOf(status.getResult().getExitStatus()).intValue());
    	assertEquals("N", status.getResult().getIsStopped());
           	
    	
    	
    }

}
