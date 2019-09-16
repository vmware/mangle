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

package com.vmware.mangle.services.cassandra;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.datastax.driver.core.AtomicMonotonicTimestampGenerator;
import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.LatencyTracker;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryLogger;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.TimestampGenerator;
import com.datastax.driver.core.policies.AddressTranslator;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.datastax.driver.core.policies.IdentityTranslator;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.ReconnectionPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.policies.SpeculativeExecutionPolicy;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.ClusterBuilderConfigurer;
import org.springframework.data.cassandra.config.CompressionType;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.lang.Nullable;

/**
 * Base class for Spring Cassandra configuration that can handle creating namespaces, execute
 * arbitrary CQL on startup & shutdown, and optionally drop keyspaces.
 *
 * @author kumargautam
 */
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
@Log4j2
public class CassandraConfig extends AbstractCassandraConfiguration {

    private CassandraClusterFactoryBean bean;
    @Autowired
    private CassandraProperties cassandraProperties;

    @Override
    public String getKeyspaceName() {
        return cassandraProperties.getKeyspaceName();
    }

    /*
     * Creating keyspace if does not exists
     */
    @Override
    protected List<String> getStartupScripts() {
        List<String> startupScripts = new ArrayList<>();
        startupScripts.add(cassandraProperties.getAlterSystemAuth());
        startupScripts.add(cassandraProperties.getMangleKeyspace());
        return startupScripts;
    }

    @Override
    public SchemaAction getSchemaAction() {
        return cassandraProperties.getSchemaAction();
    }

    /**
     * Base packages to scan for entities annotated with {@link Table} annotations. By default,
     * returns the package name of {@literal this} ({@code this.getClass().getPackage().getName()}.
     * This method must never return {@literal null}.
     */
    @Override
    public String[] getEntityBasePackages() {
        return new String[] { getClass().getPackage().getName(), "com.vmware.mangle.cassandra.model" };
    }

    /**
     * Creates a {@link CassandraClusterFactoryBean} that provides a Cassandra
     * {@link com.datastax.driver.core.Cluster}. The lifecycle of
     * {@link CassandraClusterFactoryBean} executes {@link #getStartupScripts() startup} and
     * {@link #getShutdownScripts() shutdown} scripts.
     *
     * @return the {@link CassandraClusterFactoryBean}.
     * @see #cluster()
     * @see #getStartupScripts()
     * @see #getShutdownScripts()
     */
    @Bean
    @Override
    public CassandraClusterFactoryBean cluster() {
        this.bean = super.cluster();

        bean.setUsername(getUsername());
        bean.setPassword(getPassword());
        bean.setSslEnabled(getSslEnabled());
        bean.setSslOptions(getSslOptions());
        bean.setLatencyTracker(getLatencyTracker());
        bean.setJmxReportingEnabled(false);
        return bean;
    }

    /**
     * Returns the cluster name.
     *
     * @return the cluster name; may be {@literal null}.
     * @since 1.5
     */
    @Nullable
    @Override
    public String getClusterName() {
        return cassandraProperties.getClusterName();
    }

    /**
     * Returns the Cassandra contact points. Defaults to {@code localhost}
     *
     * @return the Cassandra contact points
     * @see CassandraClusterFactoryBean#DEFAULT_CONTACT_POINTS
     */
    @Override
    public String getContactPoints() {
        return cassandraProperties.getContactPoints();
    }

    /**
     * Returns the Cassandra port. Defaults to {@code 9042}.
     *
     * @return the Cassandra port
     * @see CassandraClusterFactoryBean#DEFAULT_PORT
     */
    @Override
    public String getPort() {
        return cassandraProperties.getPort();
    }

    /**
     * SSL enabled or not.
     *
     * @return true/false
     */
    public boolean getSslEnabled() {
        return cassandraProperties.isSsl();
    }

    /**
     * Get Username of db.
     *
     * @return
     */
    public String getUsername() {
        return cassandraProperties.getUsername();
    }

    /**
     * Get Password of db.
     *
     * @return
     */
    public String getPassword() {
        return cassandraProperties.getPassword();
    }

    /**
     * Returns the {@link SocketOptions}.
     *
     * @return the {@link SocketOptions}, may be {@literal null}.
     */
    @Nullable
    @Override
    public SocketOptions getSocketOptions() {
        PropertyMapper map = PropertyMapper.get();
        SocketOptions options = new SocketOptions();
        map.from(this.cassandraProperties::getConnectTimeout).whenNonNull().to(options::setConnectTimeoutMillis);
        map.from(this.cassandraProperties::getReadTimeout).whenNonNull().to(options::setReadTimeoutMillis);
        return options;
    }

    /**
     * Returns the {@link ReconnectionPolicy}.
     *
     * @return the {@link ReconnectionPolicy}, may be {@literal null}.
     */
    @Nullable
    @Override
    public ReconnectionPolicy getReconnectionPolicy() {
        return new ExponentialReconnectionPolicy(1000, cassandraProperties.getReconnectDelay());
    }

    /**
     * Returns the {@link QueryOptions}.
     *
     * @return the {@link QueryOptions}, may be {@literal null}.
     * @since 1.5
     */
    @Nullable
    @Override
    public QueryOptions getQueryOptions() {
        PropertyMapper map = PropertyMapper.get();
        QueryOptions options = new QueryOptions();
        map.from(cassandraProperties::getConsistencyLevel).whenNonNull().to(options::setConsistencyLevel);
        map.from(cassandraProperties::getSerialConsistencyLevel).whenNonNull().to(options::setSerialConsistencyLevel);
        map.from(cassandraProperties::getFetchSize).to(options::setFetchSize);
        return options;
    }

    /**
     * Returns the {@link LatencyTracker}.
     *
     * @return the {@link LatencyTracker}.
     */
    public LatencyTracker getLatencyTracker() {
        return QueryLogger.builder().build();
    }

    /**
     *
     * @return the {@link SSLOptions}
     */
    private SSLOptions getSslOptions() {
        String[] cipherSuites = { "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA" };
        return new FISslOptions(getSslContext(), cipherSuites);
    }

    /**
     * Returns the {@link LoadBalancingPolicy}.
     *
     * @return the {@link LoadBalancingPolicy}, may be {@literal null}.
     */
    @Nullable
    @Override
    public LoadBalancingPolicy getLoadBalancingPolicy() {
        return DCAwareRoundRobinPolicy.builder().build();
    }

    /**
     * Returns the {@link CompressionType}.
     *
     * @return the {@link CompressionType}, may be {@literal null}.
     */
    @Nullable
    @Override
    public CompressionType getCompressionType() {
        return cassandraProperties.getCompression();
    }

    /**
     * Returns the {@link PoolingOptions}.
     *
     * @return the {@link PoolingOptions}, may be {@literal null}.
     */
    @Nullable
    @Override
    public PoolingOptions getPoolingOptions() {
        CassandraProperties.Pool pool = cassandraProperties.getPool();
        PropertyMapper map = PropertyMapper.get();
        PoolingOptions options = new PoolingOptions();
        map.from(pool::getIdleTimeout).whenNonNull().asInt(Duration::getSeconds).to(options::setIdleTimeoutSeconds);
        map.from(pool::getPoolTimeout).whenNonNull().asInt(Duration::toMillis).to(options::setPoolTimeoutMillis);
        map.from(pool::getHeartbeatInterval).whenNonNull().asInt(Duration::getSeconds)
                .to(options::setHeartbeatIntervalSeconds);
        map.from(pool::getMaxQueueSize).whenNonNull().to(options::setMaxQueueSize);
        options.setConnectionsPerHost(HostDistance.LOCAL, pool.getMinConnectionsPerHost(),
                pool.getMaxConnectionsPerHost());
        options.setConnectionsPerHost(HostDistance.REMOTE, pool.getMinConnectionsPerHost(),
                pool.getMaxConnectionsPerHost());
        options.setMaxRequestsPerConnection(HostDistance.LOCAL, pool.getMaxRequestsPerConnection());
        options.setMaxRequestsPerConnection(HostDistance.REMOTE, pool.getMaxRequestsPerConnection());
        return options;
    }

    private SSLContext getSslContext() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Noncompliant, this method never throws exception, it means trust any client
                log.log(Level.TRACE, "it means trust any client");
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Noncompliant, nothing means trust any client
                log.log(Level.TRACE, "nothing means trust any client");
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, null);
            return sc;
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /*
     * Method is used to close the db connection.
     */
    @PreDestroy
    public void destroy() {
        bean.destroy();
    }

    @Override
    protected AddressTranslator getAddressTranslator() {
        return new IdentityTranslator();
    }

    @Override
    protected AuthProvider getAuthProvider() {
        return null;
    }

    @Override
    protected ClusterBuilderConfigurer getClusterBuilderConfigurer() {
        return null;
    }

    @Override
    protected RetryPolicy getRetryPolicy() {
        return DefaultRetryPolicy.INSTANCE;
    }

    @Override
    protected SpeculativeExecutionPolicy getSpeculativeExecutionPolicy() {
        return null;
    }

    @Override
    protected TimestampGenerator getTimestampGenerator() {
        return new AtomicMonotonicTimestampGenerator();
    }
}
