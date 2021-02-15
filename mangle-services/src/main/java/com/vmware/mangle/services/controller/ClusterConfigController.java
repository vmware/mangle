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

package com.vmware.mangle.services.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.model.enums.MangleDeploymentMode;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
@RestController
@RequestMapping("rest/api/v1/cluster-config")
public class ClusterConfigController {

    private ClusterConfigService configService;

    @Autowired
    public ClusterConfigController(ClusterConfigService configService) {
        this.configService = configService;
    }

    @ApiOperation(value = "API to get hazelcast configuration", nickname = "getHazelcastConfig")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<HazelcastClusterConfig>> getHazelcastConfig() throws MangleException {
        log.info("Fetching cluster information");
        HazelcastClusterConfig config = configService.getClusterConfiguration();
        Resource<HazelcastClusterConfig> configResource = new Resource<>(config);
        configResource.add(getSelfLink(), getHateoasLinkForUpdateHazelcastConfigWithGetRel(),
                getHateoasLinkForUpdateDepTypeWithGetRel(), getHateoasLinkForUpdateQuorumWithGetRel());
        return new ResponseEntity<>(configResource, HttpStatus.OK);
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "API to update hazelcast configuration", nickname = "update-hazelcast-config", hidden = true)
    public ResponseEntity<Resource<HazelcastClusterConfig>> updateHazelcastConfig(
            @RequestBody HazelcastClusterConfig config) throws MangleException {
        log.info("Updating cluster information");
        HazelcastClusterConfig persistenceConfig = configService.getClusterConfiguration();
        if (!persistenceConfig.getMembers().equals(config.getMembers())) {
            log.error("Failed to update the cluster configuration, members list cannot be modified");
            throw new MangleException(ErrorCode.CLUSTER_CONFIG_MEMBER_MODIFICATION);
        }
        config.setId(persistenceConfig.getId());
        config = configService.updateClusterConfiguration(config);
        Resource<HazelcastClusterConfig> configResource = new Resource<>(config);
        configResource.add(getHateoasLinkForGetHazelcastConfigWithGetRel(), getSelfLink(),
                getHateoasLinkForUpdateDepTypeWithGetRel(), getHateoasLinkForUpdateQuorumWithGetRel());
        return new ResponseEntity<>(configResource, HttpStatus.OK);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "API to update mangle deployment type", nickname = "update-deployment-type")
    public ResponseEntity<Resource<HazelcastClusterConfig>> updateDeploymentMode(
            @RequestParam MangleDeploymentMode deploymentType) throws MangleException {
        HazelcastClusterConfig config = configService.updateMangleDeploymentType(deploymentType);
        Resource<HazelcastClusterConfig> configResource = new Resource<>(config);
        configResource.add(getHateoasLinkForGetHazelcastConfigWithGetRel(),
                getHateoasLinkForUpdateHazelcastConfigWithGetRel(), getSelfLink(),
                getHateoasLinkForUpdateQuorumWithGetRel());
        return new ResponseEntity<>(configResource, HttpStatus.OK);
    }

    @PostMapping(value = "/quorum", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "API to update mangle quorum", nickname = "update-quorum")
    public ResponseEntity<Resource<HazelcastClusterConfig>> updateQuorum(@RequestParam int quorumValue)
            throws MangleException {
        HazelcastClusterConfig config = configService.updateMangleQuorum(quorumValue);
        Resource<HazelcastClusterConfig> configResource = new Resource<>(config);
        configResource.add(getHateoasLinkForGetHazelcastConfigWithGetRel(),
                getHateoasLinkForUpdateHazelcastConfigWithGetRel(), getHateoasLinkForUpdateDepTypeWithGetRel(),
                getSelfLink());
        return new ResponseEntity<>(configResource, HttpStatus.OK);
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    private Link getHateoasLinkForUpdateQuorumWithGetRel() throws MangleException {
        return linkTo(methodOn(ClusterConfigController.class).updateQuorum(0)).withRel("UPDATE-QUORUM");
    }

    private Link getHateoasLinkForUpdateDepTypeWithGetRel() throws MangleException {
        return linkTo(methodOn(ClusterConfigController.class).updateDeploymentMode(MangleDeploymentMode.STANDALONE))
                .withRel("UPDATE-DEPLOYMENT");
    }

    private Link getHateoasLinkForUpdateHazelcastConfigWithGetRel() throws MangleException {
        return linkTo(methodOn(ClusterConfigController.class).updateHazelcastConfig(new HazelcastClusterConfig()))
                .withRel("UPDATE-HAZELCAST-CONFIG");
    }

    private Link getHateoasLinkForGetHazelcastConfigWithGetRel() throws MangleException {
        return linkTo(methodOn(ClusterConfigController.class).updateHazelcastConfig(new HazelcastClusterConfig()))
                .withRel("GET-HAZELCAST-CONFIG");
    }

}
