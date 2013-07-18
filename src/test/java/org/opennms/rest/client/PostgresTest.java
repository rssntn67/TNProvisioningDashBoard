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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;

import com.vaadin.data.Container;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;

public class PostgresTest {
    
	private JDBCConnectionPool m_pool; 
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    	try {
			m_pool = new SimpleJDBCConnectionPool("org.postgresql.Driver", "jdbc:postgresql://172.25.200.36:5432/tnnet", "isi_writer", "Oof6Eezu");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }
    
    @Test
	@SuppressWarnings("deprecation")
    public void testSnmpProfiles() throws Exception {
    	List<String> primarykeys = new ArrayList<String>();
    	primarykeys.add("name");
		Container snmp_profiles = new SQLContainer(new FreeformQuery("select * from isi.snmp_profiles", primarykeys,m_pool));
		for (Object id: snmp_profiles.getItemIds()) {
			System.out.println(snmp_profiles.getItem(id));
		}
		Container backup_profiles = new SQLContainer(new FreeformQuery("select * from isi.asset_profiles", primarykeys,m_pool));
		for (Object id: backup_profiles.getItemIds()) {
			System.out.println(backup_profiles.getItem(id));
		}
    }

}
