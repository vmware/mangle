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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
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
import com.vmware.mangle.cassandra.model.faults.specs.SpringServiceExceptionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.SpringServiceLatencyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.ThreadLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2InstanceStateFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2NetworkFaultSpec;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.helpers.faults.CPUFault;
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
import com.vmware.mangle.services.helpers.faults.SimulateJavaExceptionFault;
import com.vmware.mangle.services.helpers.faults.SpringServiceExceptionFault;
import com.vmware.mangle.services.helpers.faults.SpringServiceLatencyFault;
import com.vmware.mangle.services.helpers.faults.ThreadLeakFault;
import com.vmware.mangle.services.helpers.faults.aws.AwsEC2InstanceStateChangeFault;
import com.vmware.mangle.services.helpers.faults.aws.AwsEC2NetworkFault;
import com.vmware.mangle.services.helpers.faults.vcenter.VMDiskFault;
import com.vmware.mangle.services.helpers.faults.vcenter.VMNicFault;
import com.vmware.mangle.services.helpers.faults.vcenter.VMStateChangeFault;
import com.vmware.mangle.services.helpers.k8s.faults.DeleteK8SResourceFault;
import com.vmware.mangle.services.helpers.k8s.faults.K8SResourceNotReadyFault;
import com.vmware.mangle.services.helpers.k8s.faults.K8SServiceUnavailableFault;
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

    @Autowired
    private FaultInjectionHelper faultInjectionHelper;

    @ApiOperation(value = "API to inject cpu fault on Endpoint", nickname = "injectCPUFault")
    @PostMapping(value = "/cpu", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectCPUFault(@Validated @RequestBody CpuFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new CPUFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to trigger injecting a K8S Specifc fault", nickname = "deleteK8SResourceFault")
    @PostMapping(value = "/k8s/delete-resource", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> deleteK8SResourceFault(
            @Validated @RequestBody K8SDeleteResourceFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new DeleteK8SResourceFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a K8S NotReady State Fault", nickname = "K8SResourceNotReadyFault")
    @PostMapping(value = "/k8s/resource-not-ready", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> k8SResourceNotReadyFault(
            @Validated @RequestBody K8SResourceNotReadyFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new K8SResourceNotReadyFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject a K8S Service Unavailable Fault", nickname = "K8SServiceUnavailableFault")
    @PostMapping(value = "/k8s/service-unavailable", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> K8SServiceUnavailableyFault(
            @Validated @RequestBody K8SServiceUnavailableFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new K8SServiceUnavailableFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to remediate an injected fault - taskId/taskName", nickname = "remediateFault")
    @DeleteMapping(value = "/{taskIdentifier}", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> remediateFault(@PathVariable String taskIdentifier) throws MangleException {
        Task<TaskSpec> task = faultInjectionHelper.triggerRemediation(taskIdentifier);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @ApiOperation(value = "API to rerun an injected fault - taskId", nickname = "rerunFault")
    @PostMapping(value = "/{taskIdentifier}", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> rerunFault(@PathVariable String taskIdentifier) throws MangleException {
        Task<TaskSpec> task = faultInjectionHelper.rerunFault(taskIdentifier);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject docker Fault on Endpoint", nickname = "injectDockerFault")
    @PostMapping(value = "/docker", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectDockerFault(@Validated @RequestBody DockerFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new DockerFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter NIC fault on Endpoint", nickname = "injectVCenterNICFault")
    @PostMapping(value = "/vcenter/nic", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectVCenterNicFault(@Validated @RequestBody VMNicFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new VMNicFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter State fault on Endpoint", nickname = "injectVCenterStateFault")
    @PostMapping(value = "/vcenter/state", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectVCenterStateFault(@Validated @RequestBody VMStateFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new VMStateChangeFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject VCenter Disk fault on Endpoint", nickname = "injectVCenterDiskFault")
    @PostMapping(value = "/vcenter/disk", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectVCenterDiskFault(@Validated @RequestBody VMDiskFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new VMDiskFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject memory fault on Endpoint", nickname = "injectMemoryFault")
    @PostMapping(value = "/memory", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectMemoryFault(@Validated @RequestBody MemoryFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new MemoryFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject diskIO fault on Endpoint", nickname = "injectDiskIOFault")
    @PostMapping(value = "/diskIO", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectDiskIOFault(@Validated @RequestBody DiskIOFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new DiskIOFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Disk Space fault on Endpoint", nickname = "injectDiskSpaceFault")
    @PostMapping(value = "/disk-space", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectDiskSpaceFault(@Validated @RequestBody DiskSpaceSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new DiskSpaceFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Kill Process fault on Endpoint", nickname = "injectKillProcessFault")
    @PostMapping(value = "/kill-process", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectKillProcessFault(@Validated @RequestBody KillProcessFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateKillProcessFaultSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new KillProcessFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject filehandler leak fault on Endpoint", nickname = "injectFileHandlerLeakFault")
    @PostMapping(value = "/filehandler-leak", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectfileHandlerLeakFault(
            @Validated @RequestBody FilehandlerLeakFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new FileHandlerLeakFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Network fault on Endpoint", nickname = "injectNetworkFault")
    @PostMapping(value = "/network-fault", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectNetworkFault(@Validated @RequestBody NetworkFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        faultInjectionHelper.validateNertworkFaultSpec(faultSpec);
        return new ResponseEntity<>(new NetworkFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject JavaMethod Latency fault on Endpoint", nickname = "injectMethodLatencyFault")
    @PostMapping(value = "/java-method-latency", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectjavaMethodLatencyFault(
            @Validated @RequestBody JavaMethodLatencyFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new JavaMethodLatencyFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Spring service Latency fault on Endpoint", nickname = "injectSpringServiceLatencyFault")
    @PostMapping(value = "/spring-service-latency", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectSpringServiceLatencyFault(
            @Validated @RequestBody SpringServiceLatencyFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new SpringServiceLatencyFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Spring service exception fault on Endpoint", nickname = "injectSpringServiceExceptionFault")
    @PostMapping(value = "/spring-service-exception", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectSpringServiceExceptionFault(
            @Validated @RequestBody SpringServiceExceptionFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new SpringServiceExceptionFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Java throw exception fault on Endpoint", nickname = "simulateJavaExceptionFault")
    @PostMapping(value = "/simulate-java-exception", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectSimulateJavaExceptionFault(
            @Validated @RequestBody JavaThrowExceptionFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new SimulateJavaExceptionFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject KIll JVM exception fault on Endpoint", nickname = "injectKillJVMFault")
    @PostMapping(value = "/kill-jvm", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectKillJVMExceptionFault(
            @Validated @RequestBody KillJVMFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new KillJVMFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }


    @ApiOperation(value = "API to inject Thread leak fault on Endpoint", nickname = "injectThreadLeakFault")
    @PostMapping(value = "/thread-leak", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectThreadLeakFault(@Validated @RequestBody ThreadLeakFaultSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        return new ResponseEntity<>(new ThreadLeakFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject AWS EC2 Instance State fault on AwsEndpoint", nickname = "injectAwsEC2InstanceStateFault")
    @PostMapping(value = "/aws/ec2/state", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectAwsEC2InstanceStateFault(
            @Validated @RequestBody AwsEC2InstanceStateFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new AwsEC2InstanceStateChangeFault(faultSpec).invokeFault(faultInjectionHelper),
                HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject AWS EC2 Instance Block_All_Networkfault on AwsEndpoint", nickname = "injectAwsEC2InstanceBlockAllNetworkFault")
    @PostMapping(value = "/aws/ec2/network", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectAwsEC2InstanceBlockAllNetworkFault(
            @Validated @RequestBody AwsEC2NetworkFaultSpec faultSpec) throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new AwsEC2NetworkFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Kernel Panic fault on Endpoint", nickname = "injectKernelPanicFault")
    @PostMapping(value = "/kernel-panic", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectKernelPanicFault(@Validated @RequestBody KernelPanicSpec faultSpec)
            throws MangleException {
        faultInjectionHelper.validateSpec(faultSpec);
        return new ResponseEntity<>(new KernelPanicFault(faultSpec).invokeFault(faultInjectionHelper), HttpStatus.OK);
    }
}
