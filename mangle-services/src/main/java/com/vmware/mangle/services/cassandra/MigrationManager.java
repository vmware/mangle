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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;

import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import io.smartcat.migration.MigrationEngine;
import io.smartcat.migration.MigrationResources;
import io.smartcat.migration.SchemaMigration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.vmware.mangle.services.cassandra.migrationscripts.MangleDBMigration;

/**
 * @author chetanc
 */
@Component
@Log4j2
public class MigrationManager {
    @Value("${spring.data.cassandra.keyspace-name}")
    private String keyspace;

    private Session session;

    private List<MangleDBMigration> mangleDBMigrations;
    private Map<Integer, MangleDBMigration> mangleDBMigrationsMap = new TreeMap<>();

    @Autowired
    public MigrationManager(Session session, List<MangleDBMigration> mangleDBMigrations) {
        this.session = session;
        this.mangleDBMigrations = mangleDBMigrations;
    }

    private void migrateSchema(final Session session) {
        log.info("Executing schema migrations");

        final MigrationResources resources = findMigrationResources();

        MigrationEngine.withSession(session).migrate(resources);

        log.info("Done with schema migrations");
    }

    private MigrationResources findMigrationResources() {
        MigrationResources resources = new MigrationResources();
        orderDBMigrations();
        for (Map.Entry<Integer, MangleDBMigration> migrationScriptEntry : mangleDBMigrationsMap.entrySet()) {
            MangleDBMigration migrationScript = migrationScriptEntry.getValue();
            ((SchemaMigration) migrationScript).setSession(session);
            if (migrationScript.proceedDBUpgrade()) {
                resources.addMigration((SchemaMigration) migrationScript);
            } else {
                log.info("Migration script {} not executed, proceed DB upgrade returned false",
                        ((SchemaMigration) migrationScript).getDescription());
            }
        }
        return resources;
    }

    private void orderDBMigrations() {
        for (MangleDBMigration mangleDBMigration : mangleDBMigrations) {
            mangleDBMigrationsMap.put(((SchemaMigration) mangleDBMigration).getVersion(), mangleDBMigration);
        }
    }

    public void doMigration() {
        Assert.notNull(session, "Session object is null");
        Assert.hasText(keyspace, "Keyspace cannot be null or empty");
        printMetadata(session);
        migrateSchema(session);
    }

    private void printMetadata(final Session session) {
        final Metadata metadata = session.getCluster().getMetadata();
        log.info("Connected to cluster = {}", metadata.getClusterName());

        for (final Host host : metadata.getAllHosts()) {
            log.info("Datacenter = {} host = {}", host.getDatacenter(), host.getAddress());
        }
    }

    @PostConstruct
    public void setup() {
        doMigration();
    }
}
