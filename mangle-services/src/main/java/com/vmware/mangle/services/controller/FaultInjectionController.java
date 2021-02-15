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

import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.faults.specs.ClockSkewSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbTransactionErrorFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbTransactionLatencyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DiskIOFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DiskSpaceSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FilehandlerLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JavaMethodLatencyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JavaThrowExceptionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SDeleteResourceFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SServiceUnavailableFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KernelPanicSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KillJVMFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KillProcessFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.MemoryFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.NetworkFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.NetworkPartitionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.SpringServiceExceptionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.SpringServiceLatencyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.StopServiceFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.ThreadLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDropConnectionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisReturnErrorFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2InstanceStateFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2NetworkFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2StorageFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMNetworkFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMStateFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMStorageFaultSpec;
import com.vmware.mangle.model.enums.DatabaseType;
import com.vmware.mangle.model.vcenter.specs.HostFaultSpec;
import com.vmware.mangle.services.helpers.DbTransactionErrorHelper;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.helpers.faults.CPUFault;
import com.vmware.mangle.services.helpers.faults.ClockSkewFault;
import com.vmware.mangle.services.helpers.faults.DbConnectionLeakFault;
import com.vmware.mangle.services.helpers.faults.DbTransactionErrorFault;
import com.vmware.mangle.services.helpers.faults.DbTransactionLatencyFault;
import com.vmware.mangle.services.helpers.faults.DiskIOFault;
import com.vmware.mangle.services.helpers.faults.DiskSpaceFault;
import com.vmware.mangle.services.helpers.faults.DockerFault;
import com.vmware.mangle.services.helpers.faults.FileHandlerLeakFault;
import com.vmware.mangle.services.helpers.faults.JavaMethodLatencyFault;
import com.vmware.mangle.services.helpers.faults.KernelPanicFault;
import com.vmware.mangle.services.helpers.faults.KillJVMFault;
import com.vmware.mangle.services.helpers.faults.KillProcessFault;
import com.vmware.mangle.services.helpers.faults.MemoryFault;
import com.vmware.mangle.services.helpers.faults.NetworkFault;
import com.vmware.mangle.services.helpers.faults.NetworkPartitionFault;
import com.vmware.mangle.services.helpers.faults.SimulateJavaExceptionFault;
import com.vmware.mangle.services.helpers.faults.SpringServiceExceptionFault;
import com.vmware.mangle.services.helpers.faults.SpringServiceLatencyFault;
import com.vmware.mangle.services.helpers.faults.StopServiceFault;
import com.vmware.mangle.services.helpers.faults.ThreadLeakFault;
import com.vmware.mangle.services.helpers.faults.aws.AwsEC2InstanceStateChangeFault;
import com.vmware.mangle.services.helpers.faults.aws.AwsEC2NetworkFault;
import com.vmware.mangle.services.helpers.faults.aws.AwsEC2StorageFault;
import com.vmware.mangle.services.helpers.faults.aws.AwsRDSFault;
import com.vmware.mangle.services.helpers.faults.azure.AzureVMNetworkFault;
import com.vmware.mangle.services.helpers.faults.azure.AzureVMStateChangeFault;
import com.vmware.mangle.services.helpers.faults.azure.AzureVMStorageFault;
import com.vmware.mangle.services.helpers.faults.vcenter.HostFault;
import com.vmware.mangle.services.helpers.faults.vcenter.VMDiskFault;
import com.vmware.mangle.services.helpers.faults.vcenter.VMNicFault;
import com.vmware.mangle.services.helpers.faults.vcenter.VMStateChangeFault;
import com.vmware.mangle.services.helpers.k8s.faults.DeleteK8SResourceFault;
import com.vmware.mangle.services.helpers.k8s.faults.K8SResourceNotReadyFault;
import com.vmware.mangle.services.helpers.k8s.faults.K8SServiceUnavailableFault;
import com.vmware.mangle.services.helpers.redis.faults.RedisDelayFault;
import com.vmware.mangle.services.helpers.redis.faults.RedisDropConnectionFault;
import com.vmware.mangle.services.helpers.redis.faults.RedisReturnEmptyFault;
import com.vmware.mangle.services.helpers.redis.faults.RedisReturnErrorFault;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Controller class for Fault Injection.
 *
 * @author bkaranam
 */
@RestController
@RequestMapping("/rest/api/v1/faults")
@Api("/rest/api/v1/faults")
public class FaultInjectionController {

    private final FaultInjectionHelper faultInjectionHelper;

    @Autowired
    public FaultInjectionController(FaultInjectionHelper faultInjectionHelper) {
        this.faultInjectionHelper = faultInjectionHelper;
    }

    @ApiOperation(value = "API to inject cpu fault on Endpoint", nickname = "injectCPUFault")
    @PostMapping(value = "/cpu", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectCPUFault(@Validated @RequestBody CpuFaultSpec faultSpec)
            throws MangleException {
        Task<TaskSpec> task = new CPUFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to trigger injecting a K8S Specifc fault", nickname = "deleteK8SResourceFault")
    @PostMapping(value = "/k8s/delete-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> deleteK8SResourceFault(
            @Validated @RequestBody K8SDeleteResourceFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateScheduleForK8sSpecificFault(faultSpec);
        Task<TaskSpec> task = new DeleteK8SResourceFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a K8S NotReady State Fault", nickname = "K8SResourceNotReadyFault")
    @PostMapping(value = "/k8s/resource-not-ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> k8SResourceNotReadyFault(
            @Validated @RequestBody K8SResourceNotReadyFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new K8SResourceNotReadyFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());

        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a K8S Service Unavailable Fault", nickname = "k8SServiceUnavailableFault")
    @PostMapping(value = "/k8s/service-unavailable", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> k8SServiceUnavailableyFault(
            @Validated @RequestBody K8SServiceUnavailableFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new K8SServiceUnavailableFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to remediate an injected fault - taskId/taskName", nickname = "remediateFault")
    @DeleteMapping(value = "/{taskIdentifier}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> remediateFault(@PathVariable String taskIdentifier)
            throws MangleException {
        Task<TaskSpec> task = faultInjectionHelper.triggerRemediation(taskIdentifier);

        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRerunCLBLink());

        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to rerun an injected fault - taskId", nickname = "rerunFault")
    @PostMapping(value = "/{taskIdentifier}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> rerunFault(@PathVariable String taskIdentifier)
            throws MangleException {
        Task<TaskSpec> task = faultInjectionHelper.rerunFault(taskIdentifier);

        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject docker Fault on Endpoint", nickname = "injectDockerFault")
    @PostMapping(value = "/docker", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectDockerFault(@Validated @RequestBody DockerFaultSpec faultSpec)
            throws MangleException {

        Task<TaskSpec> task = new DockerFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter NIC fault on Endpoint", nickname = "injectVCenterNICFault")
    @PostMapping(value = "/vcenter/nic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectVCenterNicFault(
            @Validated @RequestBody VMNicFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new VMNicFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter State fault on Endpoint", nickname = "injectVCenterStateFault")
    @PostMapping(value = "/vcenter/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectVCenterStateFault(
            @Validated @RequestBody VMStateFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new VMStateChangeFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter host fault on Endpoint", nickname = "injectVCenterHostFault")
    @PostMapping(value = "/vcenter/host", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectVCenterHostFault(
            @Validated @RequestBody HostFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new HostFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter Disk fault on Endpoint", nickname = "injectVCenterDiskFault")
    @PostMapping(value = "/vcenter/disk", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectVCenterDiskFault(
            @Validated @RequestBody VMDiskFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new VMDiskFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject memory fault on Endpoint", nickname = "injectMemoryFault")
    @PostMapping(value = "/memory", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectMemoryFault(@Validated @RequestBody MemoryFaultSpec faultSpec)
            throws MangleException {
        Task<TaskSpec> task = new MemoryFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject clock skew fault on Endpoint", nickname = "injectClockSkewFault")
    @PostMapping(value = "/clockSkew", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectClockSkewFault(
            @Validated @RequestBody ClockSkewSpec clockSkewSpec) throws MangleException {
        Task<TaskSpec> task = new ClockSkewFault(clockSkewSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject diskIO fault on Endpoint", nickname = "injectDiskIOFault")
    @PostMapping(value = "/diskIO", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectDiskIOFault(@Validated @RequestBody DiskIOFaultSpec faultSpec)
            throws MangleException {

        Task<TaskSpec> task = new DiskIOFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Disk Space fault on Endpoint", nickname = "injectDiskSpaceFault")
    @PostMapping(value = "/disk-space", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectDiskSpaceFault(
            @Validated @RequestBody DiskSpaceSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new DiskSpaceFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Kill Process fault on Endpoint", nickname = "injectKillProcessFault")
    @PostMapping(value = "/kill-process", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectKillProcessFault(
            @Validated @RequestBody KillProcessFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateKillProcessFaultSpec(faultSpec);

        Task<TaskSpec> task = new KillProcessFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to stop service fault on Endpoint", nickname = "injectStopServiceFault")
    @PostMapping(value = "/stop-service", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectstopServiceFault(
            @Validated @RequestBody StopServiceFaultSpec stopServicefaultSpec) throws MangleException {
        Task<TaskSpec> task = new StopServiceFault(stopServicefaultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject filehandler leak fault on Endpoint", nickname = "injectFileHandlerLeakFault")
    @PostMapping(value = "/filehandler-leak", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectfileHandlerLeakFault(
            @Validated @RequestBody FilehandlerLeakFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new FileHandlerLeakFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Network fault on Endpoint", nickname = "injectNetworkFault")
    @PostMapping(value = "/network-fault", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectNetworkFault(
            @Validated @RequestBody NetworkFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateNetworkFaultSpec(faultSpec);

        Task<TaskSpec> task = new NetworkFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Network partition fault on Endpoint", nickname = "injectNetworkPartitionFault")
    @PostMapping(value = "/network-partition", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectNetworkPartitionFault(
            @Validated @RequestBody NetworkPartitionFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateNetworkPartitionFaultSpec(faultSpec);
        Task<TaskSpec> task = new NetworkPartitionFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject JavaMethod Latency fault on Endpoint", nickname = "injectMethodLatencyFault")
    @PostMapping(value = "/java-method-latency", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectjavaMethodLatencyFault(
            @Validated @RequestBody JavaMethodLatencyFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new JavaMethodLatencyFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Spring service Latency fault on Endpoint", nickname = "injectSpringServiceLatencyFault")
    @PostMapping(value = "/spring-service-latency", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectSpringServiceLatencyFault(
            @Validated @RequestBody SpringServiceLatencyFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new SpringServiceLatencyFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Spring service exception fault on Endpoint", nickname = "injectSpringServiceExceptionFault")
    @PostMapping(value = "/spring-service-exception", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectSpringServiceExceptionFault(
            @Validated @RequestBody SpringServiceExceptionFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new SpringServiceExceptionFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Java throw exception fault on Endpoint", nickname = "simulateJavaExceptionFault")
    @PostMapping(value = "/simulate-java-exception", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectSimulateJavaExceptionFault(
            @Validated @RequestBody JavaThrowExceptionFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new SimulateJavaExceptionFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject KIll JVM exception fault on Endpoint", nickname = "injectKillJVMFault")
    @PostMapping(value = "/kill-jvm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectKillJVMExceptionFault(
            @Validated @RequestBody KillJVMFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new KillJVMFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }


    @ApiOperation(value = "API to inject Thread leak fault on Endpoint", nickname = "injectThreadLeakFault")
    @PostMapping(value = "/thread-leak", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectThreadLeakFault(
            @Validated @RequestBody ThreadLeakFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new ThreadLeakFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject AWS EC2 Instance State fault on AwsEndpoint", nickname = "injectAwsEC2InstanceStateFault")
    @PostMapping(value = "/aws/ec2/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAwsEC2InstanceStateFault(
            @Validated @RequestBody AwsEC2InstanceStateFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new AwsEC2InstanceStateChangeFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject AWS EC2 Instance Block_All_Networkfault on AwsEndpoint", nickname = "injectAwsEC2InstanceBlockAllNetworkFault")
    @PostMapping(value = "/aws/ec2/network", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAwsEC2InstanceBlockAllNetworkFault(
            @Validated @RequestBody AwsEC2NetworkFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new AwsEC2NetworkFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject AWS EC2 Instance Detach Volumes fault on AwsEndpoint", nickname = "injectAwsEC2InstanceDetachVolumes")
    @PostMapping(value = "/aws/ec2/storage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAwsEC2InstanceDetachVolumes(
            @Validated @RequestBody AwsEC2StorageFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new AwsEC2StorageFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject AWS RDS faults on AwsEndpoint", nickname = "injectAwsRDSFaults")
    @PostMapping(value = "/aws/rds", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAwsRDSFaults(
            @Validated @RequestBody AwsRDSFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new AwsRDSFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Azure Virtual machines State fault on AzureEndpoint", nickname = "injectAzureVMStateFault")
    @PostMapping(value = "/azure/virtualmachine/state", produces = "application/json")
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAzureVMStateFault(
            @Validated @RequestBody AzureVMStateFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new AzureVMStateChangeFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Azure virtual machine Block_All_Network Fault on AzureEndpoint", nickname = "injectAzureVMNetworkFault")
    @PostMapping(value = "/azure/virtualmachine/network", produces = "application/json")
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAzureVMNetworkFault(
            @Validated @RequestBody AzureVMNetworkFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new AzureVMNetworkFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject detach disk from virtual machine fault on AzureEndpoint", nickname = "injectAzureVMDetachDisk")
    @PostMapping(value = "/azure/virtualmachine/storage", produces = "application/json")
    public ResponseEntity<Resource<Task<TaskSpec>>> injectAzureVMDetachDisk(
            @Validated @RequestBody AzureVMStorageFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new AzureVMStorageFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Kernel Panic fault on Endpoint", nickname = "injectKernelPanicFault")
    @PostMapping(value = "/kernel-panic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectKernelPanicFault(
            @Validated @RequestBody KernelPanicSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new KernelPanicFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Db connection leak fault on Endpoint", nickname = "injectDbConnectionLeakFault")
    @PostMapping(value = "/db-connection-leak", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectDbConnectionLeakFault(
            @Validated @RequestBody DbFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new DbConnectionLeakFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Db transaction error fault on Endpoint", nickname = "injectDbTransactionErrorFault")
    @PostMapping(value = "/db-transaction-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectDbTransactionErrorFault(
            @Validated @RequestBody DbTransactionErrorFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new DbTransactionErrorFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Db transaction latency fault on Endpoint", nickname = "injectDbTransactionLatencyFault")
    @PostMapping(value = "/db-transaction-latency", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectDbTransactionLatencyFault(
            @Validated @RequestBody DbTransactionLatencyFaultSpec faultSpec) throws MangleException {
        Task<TaskSpec> task = new DbTransactionLatencyFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());
        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a Redis Delay Fault", nickname = "injectRedisDelayFault")
    @PostMapping(value = "/redis/delay", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectRedisDelayFault(
            @Validated @RequestBody RedisDelayFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new RedisDelayFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());

        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a Redis Return Error Fault", nickname = "injectRedisReturnErrorFault")
    @PostMapping(value = "/redis/return-error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectRedisReturnErrorFault(
            @Validated @RequestBody RedisReturnErrorFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new RedisReturnErrorFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());

        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a Redis Return Empty Fault", nickname = "injectRedisReturnEmptyFault")
    @PostMapping(value = "/redis/return-empty", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectRedisReturnEmptyFault(
            @Validated @RequestBody RedisFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new RedisReturnEmptyFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());

        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a Redis Drop Connection Fault", nickname = "injectRedisDropConnectionFault")
    @PostMapping(value = "/redis/drop-connection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Task<TaskSpec>>> injectRedisDropConnectionFault(
            @Validated @RequestBody RedisDropConnectionFaultSpec faultSpec) throws MangleException {

        Task<TaskSpec> task = new RedisDropConnectionFault(faultSpec).invokeFault(faultInjectionHelper);
        Resource<Task<TaskSpec>> taskResource = new Resource<>(task);
        taskResource.add(getSelfLink(), getHateoasRemediateCLBLink(), getHateoasRerunCLBLink());

        return new ResponseEntity<>(taskResource, HttpStatus.OK);
    }

    private Link getHateoasRemediateCLBLink() throws MangleException {
        return linkTo(methodOn(FaultInjectionController.class).remediateFault("")).withRel("REMEDIATE");
    }

    private Link getHateoasRerunCLBLink() throws MangleException {
        return linkTo(methodOn(FaultInjectionController.class).rerunFault("")).withRel("RERUN");
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    @ApiOperation(value = "API to get all db transaction error code by database type ", nickname = "getDbTransactionErrorCodes")
    @GetMapping(value = "/db-transaction-error-code", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<String>> getDbTransactionErrorCodes(
            @RequestParam(value = "databaseType") DatabaseType databaseType) {
        Resources<String> resources = new Resources<>(DbTransactionErrorHelper.getDbTransactionErrorCodes(databaseType)
                .keySet().stream().collect(Collectors.toList()));
        resources.add(getSelfLink());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
}
