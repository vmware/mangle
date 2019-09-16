/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.unittest.services.cassandra;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.SessionFactory;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.convert.CustomConversions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.cassandra.AbstractCassandraConfiguration;
import com.vmware.mangle.services.cassandra.CassandraClusterFactoryBean;

/**
 * Unit Test for AbstractCassandraConfiguration.
 *
 * @author kumargautam
 */
public class AbstractCassandraConfigurationTest extends PowerMockTestCase {

    private AbstractCassandraConfiguration abstractCassandraConfiguration;
    private String keySpaceName = "mangledb";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.abstractCassandraConfiguration = null;
    }

    @BeforeMethod
    public void tearUp() throws Exception {
        this.abstractCassandraConfiguration = spy(AbstractCassandraConfiguration.class);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterTest
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.AbstractCassandraConfiguration#getRequiredSession()}.
     */
    @Test
    public void testGetRequiredSession() {
        CassandraSessionFactoryBean cassandraSessionFactoryBean = mock(CassandraSessionFactoryBean.class);
        Session session = mock(Session.class);
        when(cassandraSessionFactoryBean.getObject()).thenReturn(session);
        doReturn(cassandraSessionFactoryBean).when(abstractCassandraConfiguration).session();
        Session actualResult = abstractCassandraConfiguration.getRequiredSession();
        Assert.assertNotNull(actualResult);
        verify(abstractCassandraConfiguration, times(1)).session();
        verify(cassandraSessionFactoryBean, times(1)).getObject();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.AbstractCassandraConfiguration#cassandraTemplate()}.
     */
    @Test
    public void testCassandraTemplate() {
        SessionFactory sessionFactory = mock(SessionFactory.class);
        doReturn(sessionFactory).when(abstractCassandraConfiguration).sessionFactory();
        CassandraConverter cassandraConverter = mock(CassandraConverter.class);
        doReturn(cassandraConverter).when(abstractCassandraConfiguration).cassandraConverter();
        CassandraAdminTemplate cassandraAdminTemplate = abstractCassandraConfiguration.cassandraTemplate();
        Assert.assertNotNull(cassandraAdminTemplate);
        verify(abstractCassandraConfiguration, times(1)).sessionFactory();
        verify(abstractCassandraConfiguration, times(1)).cassandraConverter();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.AbstractCassandraConfiguration#cluster()}.
     */
    @Test
    public void testCluster() {
        CassandraClusterFactoryBean cassandraClusterFactoryBean = abstractCassandraConfiguration.cluster();
        Assert.assertNotNull(cassandraClusterFactoryBean);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.AbstractCassandraConfiguration#session()}.
     */
    @Test
    public void testSession() {
        Cluster cluster = mock(Cluster.class);
        doReturn(cluster).when(abstractCassandraConfiguration).getRequiredCluster();
        CassandraConverter cassandraConverter = mock(CassandraConverter.class);
        doReturn(cassandraConverter).when(abstractCassandraConfiguration).cassandraConverter();
        doReturn("testkey").when(abstractCassandraConfiguration).getKeyspaceName();
        CassandraSessionFactoryBean cassandraSessionFactoryBean = abstractCassandraConfiguration.session();
        Assert.assertNotNull(cassandraSessionFactoryBean);
        verify(abstractCassandraConfiguration, times(1)).getRequiredCluster();
        verify(abstractCassandraConfiguration, times(1)).cassandraConverter();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.AbstractCassandraConfiguration#cassandraConverter()}.
     *
     * @throws ClassNotFoundException
     */
    @Test
    public void testCassandraConverter() throws ClassNotFoundException {
        doThrow(ClassNotFoundException.class).when(abstractCassandraConfiguration).cassandraMapping();
        CustomConversions customConversions = mock(CustomConversions.class);
        doReturn(customConversions).when(abstractCassandraConfiguration).cassandraCustomConversions();
        CassandraConverter cassandraConverter;
        try {
            cassandraConverter = abstractCassandraConfiguration.cassandraConverter();
        } catch (Exception e) {
            cassandraConverter = null;
        }
        Assert.assertNull(cassandraConverter);
        verify(abstractCassandraConfiguration, times(1)).cassandraMapping();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.AbstractCassandraConfiguration#sessionFactory()}.
     */
    @Test
    public void testSessionFactory() {
        Session session = mock(Session.class);
        doReturn(session).when(abstractCassandraConfiguration).getRequiredSession();
        SessionFactory sessionFactory = abstractCassandraConfiguration.sessionFactory();
        Assert.assertNotNull(sessionFactory);
        verify(abstractCassandraConfiguration, times(1)).getRequiredSession();
    }

    /**
     * Test method for {@link AbstractCassandraConfiguration#cassandraCustomConversions()} ()}.
     */
    @Test
    public void testCustomConversions() {
        Cluster cluster = mock(Cluster.class);
        doReturn(cluster).when(abstractCassandraConfiguration).getRequiredCluster();
        doReturn(keySpaceName).when(abstractCassandraConfiguration).getKeyspaceName();
        CustomConversions customConversions = abstractCassandraConfiguration.cassandraCustomConversions();
        Assert.assertNotNull(customConversions);
    }
}
