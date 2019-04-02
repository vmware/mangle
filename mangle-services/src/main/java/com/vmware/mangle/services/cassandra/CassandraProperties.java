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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.SocketOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CompressionType;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Cassandra.
 *
 * @author kumargautam
 */
@Component
@ConfigurationProperties(prefix = "spring.data.cassandra")
@Setter
@Getter
@NoArgsConstructor
@ToString
public class CassandraProperties {

    /**
     * Keyspace name to use.
     */
    private String keyspaceName;

    /**
     * Name of the Cassandra cluster.
     */
    private String clusterName;

    /**
     * Cluster node addresses.
     */
    private String contactPoints = CassandraClusterFactoryBean.DEFAULT_CONTACT_POINTS;

    /**
     * Port of the Cassandra server.
     */
    private String port = String.valueOf(ProtocolOptions.DEFAULT_PORT);

    /**
     * Login user of the server.
     */
    private String username;

    /**
     * Login password of the server.
     */
    private String password;

    /**
     * Compression supported by the Cassandra binary protocol.
     */
    private CompressionType compression = CompressionType.NONE;

    /**
     * Queries consistency level.
     */
    private ConsistencyLevel consistencyLevel;

    /**
     * Queries serial consistency level.
     */
    private ConsistencyLevel serialConsistencyLevel;

    /**
     * Queries default fetch size.
     */
    private int fetchSize = QueryOptions.DEFAULT_FETCH_SIZE;

    /**
     * Socket option: connection time out.
     */
    private int connectTimeout = SocketOptions.DEFAULT_CONNECT_TIMEOUT_MILLIS;

    /**
     * Socket option: read time out.
     */
    private int readTimeout = SocketOptions.DEFAULT_READ_TIMEOUT_MILLIS;

    /**
     * Schema action to take at startup.
     */
    private SchemaAction schemaAction = SchemaAction.NONE;

    /**
     * Enable SSL support.
     */
    private boolean ssl = false;

    /**
     * Whether to enable JMX reporting. Default to false as Cassandra JMX reporting is not
     * compatible with Dropwizard Metrics.
     */
    private boolean jmxEnabled;

    /**
     * Reconnect delay in ms.
     */
    private int reconnectDelay;

    private String mangleKeyspace;

    private String alterSystemAuth;

    /**
     * Pool configuration.
     */
    private final Pool pool = new Pool();

    /**
     * Pool properties.
     */
    @Setter
    @Getter
    @NoArgsConstructor
    public static class Pool {

        /**
         * Idle timeout before an idle connection is removed. If a duration suffix is not specified,
         * seconds will be used.
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration idleTimeout = Duration.ofSeconds(120);

        /**
         * Pool timeout when trying to acquire a connection from a host's pool.
         */
        private Duration poolTimeout = Duration.ofMillis(5000);

        /**
         * Heartbeat interval after which a message is sent on an idle connection to make sure it's
         * still alive. If a duration suffix is not specified, seconds will be used.
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration heartbeatInterval = Duration.ofSeconds(30);

        /**
         * Maximum number of requests that get queued if no connection is available.
         */
        private int maxQueueSize = 256;

        /**
         * Minimum number of connections per host is available.
         */
        private int minConnectionsPerHost = 2;

        /**
         * Maximum number of connections per host is available.
         */
        private int maxConnectionsPerHost = 5;

        /**
         * Maximum number of Requests per Connection is available.
         */
        private int maxRequestsPerConnection = 1000;

    }

}
