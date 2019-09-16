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

package com.vmware.mangle.unittest.services.cassandra.converters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datastax.driver.core.Row;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.cassandra.converters.CredentialsSpecReadingConverter;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;

/**
 * Unit Test Case for CredentialsSpecReadingConverter.
 *
 * @author kumargautam
 */
public class CredentialsSpecReadingConverterTest extends PowerMockTestCase {

    private CredentialsSpecReadingConverter credentialsSpecReadingConverter;
    @Mock
    private MappingCassandraConverter mappingCassandraConverter;
    private CredentialsSpecMockData credentialsSpecMockData;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.credentialsSpecMockData = new CredentialsSpecMockData();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() {
        this.credentialsSpecReadingConverter = null;
        this.mappingCassandraConverter = null;
        this.credentialsSpecMockData = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterTest
    public void tearDown() {
        validateMockitoUsage();
    }

    @BeforeMethod
    public void tearUp() {
        this.credentialsSpecReadingConverter = spy(new CredentialsSpecReadingConverter(mappingCassandraConverter));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.converters.CredentialsSpecReadingConverter#convert(com.datastax.driver.core.Row)}.
     */
    @Test
    public void testConvertForMachineCase() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EndpointType.MACHINE.toString());
        when(mappingCassandraConverter.read(any(), any(Row.class)))
                .thenReturn(credentialsSpecMockData.getRMCredentialsData());
        CredentialsSpec actualResult = credentialsSpecReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof RemoteMachineCredentials);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.converters.CredentialsSpecReadingConverter#convert(com.datastax.driver.core.Row)}.
     */
    @Test
    public void testConvertForAwsCase() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EndpointType.AWS.toString());
        when(mappingCassandraConverter.read(any(), any(Row.class)))
                .thenReturn(credentialsSpecMockData.getAWSCredentialsData());
        CredentialsSpec actualResult = credentialsSpecReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof AWSCredentials);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.converters.CredentialsSpecReadingConverter#convert(com.datastax.driver.core.Row)}.
     */
    @Test
    public void testConvertForK8sclusterCase() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EndpointType.K8S_CLUSTER.toString());
        when(mappingCassandraConverter.read(any(), any(Row.class)))
                .thenReturn(credentialsSpecMockData.getk8SCredentialsData());
        CredentialsSpec actualResult = credentialsSpecReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof K8SCredentials);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.converters.CredentialsSpecReadingConverter#convert(com.datastax.driver.core.Row)}.
     */
    @Test
    public void testConvertForVcenterCase() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EndpointType.VCENTER.toString());
        when(mappingCassandraConverter.read(any(), any(Row.class)))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        CredentialsSpec actualResult = credentialsSpecReadingConverter.convert(source);
        Assert.assertTrue(actualResult instanceof VCenterCredentials);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.cassandra.converters.CredentialsSpecReadingConverter#convert(com.datastax.driver.core.Row)}.
     */
    @Test
    public void testConvertForDefaultCase() {
        Row source = mock(Row.class);
        when(source.getString(anyString())).thenReturn(EndpointType.DOCKER.toString());
        when(mappingCassandraConverter.read(any(), any(Row.class)))
                .thenReturn(credentialsSpecMockData.getAWSCredentialsData());
        CredentialsSpec actualResult = credentialsSpecReadingConverter.convert(source);
        Assert.assertTrue(actualResult != null);
        verify(source, times(1)).getString(anyString());
        verify(mappingCassandraConverter, times(1)).read(any(), any(Row.class));
    }
}
