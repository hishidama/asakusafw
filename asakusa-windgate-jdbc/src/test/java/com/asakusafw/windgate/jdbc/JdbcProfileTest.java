/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.windgate.jdbc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.ResourceProfile;

/**
 * Test for {@link JdbcProfile}.
 */
public class JdbcProfileTest {

    /**
     * Test database.
     */
    @Rule
    public H2Resource h2 = new H2Resource("testing") {
        @Override
        protected void before() throws Exception {
            executeFile("simple.sql");
        }
    };

    /**
     * Minimum profile.
     * @throws Exception if failed
     */
    @Test
    public void convert() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());

        ResourceProfile rp = toProfile(map);
        JdbcProfile profile = JdbcProfile.convert(rp);
        assertThat(profile.getResourceName(), is(rp.getName()));
        assertThat(profile.getBatchPutUnit(), greaterThan(0L));
        Connection conn = profile.openConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO SIMPLE (VALUE) VALUES ('Hello, world!')");
            stmt.close();
            conn.commit();
        } finally {
            conn.close();
        }
        assertThat(h2.count("SIMPLE"), is(1));
    }

    /**
     * Fully specified profile.
     * @throws Exception if failed
     */
    @Test
    public void convert_all() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        map.put(JdbcProfile.KEY_USER, "");
        map.put(JdbcProfile.KEY_PASSWORD, "");
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "10000");

        JdbcProfile profile = JdbcProfile.convert(toProfile(map));
        assertThat(profile.getBatchPutUnit(), is(10000L));
        Connection conn = profile.openConnection();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO SIMPLE (VALUE) VALUES ('Hello, world!')");
            stmt.close();
            conn.commit();
        } finally {
            conn.close();
        }
        assertThat(h2.count("SIMPLE"), is(1));
    }

    /**
     * Attempts to convert a profile with empty configuration.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_empty() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        JdbcProfile.convert(toProfile(map));
    }

    /**
     * Attempts to convert a profile with negative batch put unit.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_negative_batchPutUnit() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "-1");
        JdbcProfile.convert(toProfile(map));
    }

    /**
     * Attempts to convert a profile with invalid batch put unit.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_invalid_batchPutUnit() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        map.put(JdbcProfile.KEY_BATCH_PUT_UNIT, "Hello, world!");
        JdbcProfile.convert(toProfile(map));
    }

    /**
     * Attempts to open connection with invalid driver.
     * @throws Exception if failed
     */
    @Test(expected = Exception.class)
    public void openConnection_invalid_driver() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(JdbcProfile.KEY_DRIVER, ".INVALID");
        map.put(JdbcProfile.KEY_URL, h2.getJdbcUrl());
        JdbcProfile profile = JdbcProfile.convert(toProfile(map));
        Connection conn = profile.openConnection();
        conn.close();
    }

    /**
     * Attempts to open connection with invalid URL.
     * @throws Exception if failed
     */
    @Test(expected = Exception.class)
    public void openConnection_invalid_url() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(JdbcProfile.KEY_DRIVER, org.h2.Driver.class.getName());
        map.put(JdbcProfile.KEY_URL, ".INVALID");
        JdbcProfile profile = JdbcProfile.convert(toProfile(map));
        Connection conn = profile.openConnection();
        conn.close();
    }

    private ResourceProfile toProfile(Map<String, String> map) {
        return new ResourceProfile(
                "jdbc",
                JdbcResourceProvider.class,
                ProfileContext.system(getClass().getClassLoader()),
                map);
    }
}
