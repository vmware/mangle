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

package com.vmware.mangle.faults.plugin.mockdata;

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.JAVA_HOME_PATH;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.TASK_ID;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.USER;
import static com.vmware.mangle.services.dto.AgentRuleConstants.HTTP_METHODS_STRING;
import static com.vmware.mangle.services.dto.AgentRuleConstants.SERVICES_STRING;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMProperties;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.services.enums.DockerFaultName;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.enums.VCenterDiskFaults;
import com.vmware.mangle.services.enums.VCenterNicFaults;
import com.vmware.mangle.services.enums.VCenterStateFaults;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Faults Mock Data.
 *
 * @author hkilari
 */
public class FaultsMockData {

    private Properties properties;

    private EndpointMockData endpointMockData = new EndpointMockData();
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();

    private String faultLoad;

    private String javahomePath;

    private String jvmProcess;

    private String jvmProcessPort;

    private String jvmProcessUser;

    private String testContainername;

    private String testPodLabels;

    private String faultExecutionTimeout;

    private String faultTaskId;

    private String httpMethodsString;

    private String servicesString;

    private String bytemanUser;

    public FaultsMockData() {
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        faultLoad = properties.getProperty("faultLoad");
        javahomePath = properties.getProperty("javahomePath");
        jvmProcess = properties.getProperty("jvmProcess");
        jvmProcessPort = properties.getProperty("jvmProcessPort");
        jvmProcessUser = properties.getProperty("jvmProcessUser");
        testContainername = properties.getProperty("testContainername");
        testPodLabels = properties.getProperty("testPodLabels");
        faultExecutionTimeout = properties.getProperty("faultExecutionTimeout");
        faultTaskId = properties.getProperty("faultTaskId");
        httpMethodsString = properties.getProperty("httpMethodsString");
        servicesString = properties.getProperty("servicesString");
        bytemanUser = properties.getProperty("bytemanUser");
    }

    public K8SFaultTriggerSpec getK8SCPUFaultTriggerSpec() {
        K8SFaultTriggerSpec k8SFaultSpec = new K8SFaultTriggerSpec();

        CpuFaultSpec cpuFaultSpec = getK8SCPUFaultSpec();

        k8SFaultSpec.setFaultSpec(cpuFaultSpec);
        k8SFaultSpec.setSchedule(cpuFaultSpec.getSchedule());
        return k8SFaultSpec;
    }

    public CpuFaultSpec getK8SCPUFaultSpec() {
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CpuFaultSpec cpuFaultSpec = new CpuFaultSpec();
        cpuFaultSpec.setCpuLoad(faultLoad);
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, cpuFaultSpec.getCpuLoad());
        cpuFaultSpec.setArgs(args);
        cpuFaultSpec.setEndpointName(endpointSpec.getName());

        cpuFaultSpec.setJvmProperties(getJVMProperties());

        cpuFaultSpec.setK8sArguments(getK8SSpecificArguments());
        cpuFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);

        cpuFaultSpec.setEndpoint(endpointSpec);
        cpuFaultSpec.setCredentials(credentialsSpec);
        cpuFaultSpec.setInjectionHomeDir("/testDirectory/");
        return cpuFaultSpec;
    }

    public JVMAgentFaultSpec getK8sCpuJvmAgentFaultSpec() {
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        JVMAgentFaultSpec cpuFaultSpec = new JVMAgentFaultSpec();
        cpuFaultSpec.setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, faultLoad);
        cpuFaultSpec.setArgs(args);

        cpuFaultSpec.setJvmProperties(getJVMProperties());
        cpuFaultSpec.setK8sArguments(getK8SSpecificArguments());
        cpuFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);

        cpuFaultSpec.setEndpointName(endpointSpec.getName());
        cpuFaultSpec.setEndpoint(endpointSpec);
        cpuFaultSpec.setCredentials(credentialsSpec);
        return cpuFaultSpec;
    }

    public DockerFaultSpec getDockerPauseFaultSpec() {
        DockerFaultSpec faultSpec = new DockerFaultSpec();


        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put("--containerName", testContainername);
        faultSpec.setArgs(specificArgs);
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();

        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setDockerFaultName(DockerFaultName.DOCKER_PAUSE);
        faultSpec.setFaultName(DockerFaultName.DOCKER_PAUSE.name());

        return faultSpec;
    }

    public DockerFaultSpec getDockerStopFaultSpec() {
        DockerFaultSpec faultSpec = new DockerFaultSpec();


        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put("--containerName", testContainername);
        faultSpec.setArgs(specificArgs);
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();

        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setDockerFaultName(DockerFaultName.DOCKER_STOP);
        faultSpec.setFaultName(DockerFaultName.DOCKER_STOP.name());

        return faultSpec;
    }

    public JVMAgentFaultSpec getLinuxCpuJvmAgentFaultSpec() {
        RemoteMachineCredentials credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        JVMAgentFaultSpec cpuFaultSpec = new JVMAgentFaultSpec();
        cpuFaultSpec.setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, faultLoad);
        cpuFaultSpec.setArgs(args);

        cpuFaultSpec.setJvmProperties(getJVMProperties());
        cpuFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);

        cpuFaultSpec.setEndpointName(endpointSpec.getName());
        cpuFaultSpec.setEndpoint(endpointSpec);
        cpuFaultSpec.setCredentials(credentialsSpec);
        return cpuFaultSpec;
    }

    public JVMAgentFaultSpec getDockerCpuJvmAgentFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        JVMAgentFaultSpec cpuFaultSpec = new JVMAgentFaultSpec();
        cpuFaultSpec.setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, faultLoad);
        cpuFaultSpec.setArgs(args);

        cpuFaultSpec.setJvmProperties(getJVMProperties());
        cpuFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);

        cpuFaultSpec.setEndpointName(endpointSpec.getName());
        cpuFaultSpec.setEndpoint(endpointSpec);
        return cpuFaultSpec;
    }

    public JVMAgentFaultSpec getDockerCpuJvmAgentFaultSpecV2() {
        JVMAgentFaultSpec cpuFaultSpec = getDockerCpuJvmAgentFaultSpec();
        cpuFaultSpec.setArgs(setFaultArgsV2());
        return cpuFaultSpec;
    }

    private Map<String, String> setFaultArgsV2() {
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, faultLoad);
        args.put(JAVA_HOME_PATH, javahomePath);
        return args;

    }

    public JVMCodeLevelFaultSpec getK8sSpringExceptionJVMCodeLevelFaultSpec() {
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = new JVMCodeLevelFaultSpec();
        springServiceExceptionFaultSpec.setFaultType(BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        springServiceExceptionFaultSpec.setFaultName(BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        Map<String, String> args = new HashMap<>();
        args.put(SERVICES_STRING, servicesString);
        args.put(HTTP_METHODS_STRING, httpMethodsString);
        args.put(TASK_ID, faultTaskId);
        springServiceExceptionFaultSpec.setArgs(args);
        springServiceExceptionFaultSpec.setEndpointName(endpointSpec.getName());

        springServiceExceptionFaultSpec.setJvmProperties(getJVMProperties());
        springServiceExceptionFaultSpec.setK8sArguments(getK8SSpecificArguments());
        springServiceExceptionFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);

        springServiceExceptionFaultSpec.setEndpoint(endpointSpec);
        springServiceExceptionFaultSpec.setCredentials(credentialsSpec);
        return springServiceExceptionFaultSpec;
    }

    public JVMCodeLevelFaultSpec getLinuxJvmCodelevelFaultSpec() {
        RemoteMachineCredentials credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = new JVMCodeLevelFaultSpec();
        springServiceExceptionFaultSpec.setFaultType(BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        springServiceExceptionFaultSpec.setFaultName(BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        Map<String, String> args = new HashMap<>();
        args.put(SERVICES_STRING, servicesString);
        args.put(HTTP_METHODS_STRING, httpMethodsString);
        args.put(TASK_ID, faultTaskId);
        args.put(USER, bytemanUser);
        args.put(JAVA_HOME_PATH, javahomePath);
        springServiceExceptionFaultSpec.setArgs(args);
        springServiceExceptionFaultSpec.setEndpointName(endpointSpec.getName());
        springServiceExceptionFaultSpec.setEndpoint(endpointSpec);
        springServiceExceptionFaultSpec.setCredentials(credentialsSpec);

        springServiceExceptionFaultSpec.setJvmProperties(getJVMProperties());
        springServiceExceptionFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);
        return springServiceExceptionFaultSpec;
    }

    public JVMCodeLevelFaultSpec getDockerJvmCodelevelFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = new JVMCodeLevelFaultSpec();
        springServiceExceptionFaultSpec.setFaultType(BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        springServiceExceptionFaultSpec.setFaultName(BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        Map<String, String> args = new HashMap<>();
        args.put(SERVICES_STRING, servicesString);
        args.put(HTTP_METHODS_STRING, httpMethodsString);
        args.put(TASK_ID, faultTaskId);
        args.put(USER, bytemanUser);
        args.put(JAVA_HOME_PATH, javahomePath);
        springServiceExceptionFaultSpec.setArgs(args);
        springServiceExceptionFaultSpec.setEndpointName(endpointSpec.getName());
        springServiceExceptionFaultSpec.setEndpoint(endpointSpec);

        springServiceExceptionFaultSpec.setJvmProperties(getJVMProperties());
        springServiceExceptionFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);
        return springServiceExceptionFaultSpec;
    }

    public JVMProperties getJVMProperties() {
        JVMProperties jvmProperties = new JVMProperties();
        jvmProperties.setJavaHomePath(javahomePath);
        jvmProperties.setJvmprocess(jvmProcess);
        jvmProperties.setPort(jvmProcessPort);
        jvmProperties.setUser(jvmProcessUser);
        return jvmProperties;
    }

    public K8SSpecificArguments getK8SSpecificArguments() {
        K8SSpecificArguments k8sArguments = new K8SSpecificArguments();
        k8sArguments.setContainerName(testContainername);
        k8sArguments.setEnableRandomInjection(true);
        k8sArguments.setPodLabels(testPodLabels);
        return k8sArguments;
    }

    public K8SResourceNotReadyFaultSpec getK8SResourceNotReadyFaultSpec() {
        K8SResourceNotReadyFaultSpec k8SResourceNotReadyFaultSpec = new K8SResourceNotReadyFaultSpec();
        k8SResourceNotReadyFaultSpec.setAppContainerName(testContainername);
        k8SResourceNotReadyFaultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        k8SResourceNotReadyFaultSpec.setEndpointName(endpointMockData.k8sEndpointMockData().getName());
        k8SResourceNotReadyFaultSpec.setCredentials(credentialsSpecMockData.getk8SCredentialsData());
        //k8SResourceNotReadyFaultSpec.set
        k8SResourceNotReadyFaultSpec.setResourceType(K8SResource.POD);
        return k8SResourceNotReadyFaultSpec;
    }

    public K8SFaultSpec getDeleteK8SResourceFaultSpec() {
        K8SFaultSpec k8SFaultSpec = new K8SFaultSpec();
        k8SFaultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        k8SFaultSpec.setEndpointName(endpointMockData.k8sEndpointMockData().getName());
        k8SFaultSpec.setCredentials(credentialsSpecMockData.getk8SCredentialsData());
        return k8SFaultSpec;
    }

    public VMStateFaultSpec getVMStateFaultSpec() {
        VMStateFaultSpec faultSpec = new VMStateFaultSpec();
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put("--vmname", "vm");
        faultSpec.setArgs(specificArgs);
        VCenterCredentials creds = credentialsSpecMockData.getVCenterCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.vCenterEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(VCenterStateFaults.POWEROFF_VM);
        faultSpec.setFaultName(faultSpec.getFault().name());

        return faultSpec;
    }

    public VMDiskFaultSpec getVMDiskFaultSpec() {
        VMDiskFaultSpec faultSpec = new VMDiskFaultSpec();
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put("--vmname", "vm");
        faultSpec.setArgs(specificArgs);
        VCenterCredentials creds = credentialsSpecMockData.getVCenterCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.vCenterEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(VCenterDiskFaults.DISCONNECT_DISK);
        faultSpec.setFaultName(VCenterDiskFaults.DISCONNECT_DISK.name());

        return faultSpec;
    }

    public VMNicFaultSpec getVMNicFaultSpec() {
        VMNicFaultSpec faultSpec = new VMNicFaultSpec();
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put("--vmname", "vm");
        faultSpec.setArgs(specificArgs);
        VCenterCredentials creds = credentialsSpecMockData.getVCenterCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.vCenterEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(VCenterNicFaults.DISCONNECT_NIC);
        faultSpec.setFaultName(VCenterNicFaults.DISCONNECT_NIC.name());

        return faultSpec;
    }

    public SupportScriptInfo getSupportScriptInfo() {
        SupportScriptInfo faultInjectionScriptInfo = new SupportScriptInfo();
        faultInjectionScriptInfo.setClassPathResource(true);
        faultInjectionScriptInfo.setScriptFileName("mock_command.txt");
        faultInjectionScriptInfo.setTargetDirectoryPath(DEFAULT_TEMP_DIR);
        faultInjectionScriptInfo.setExecutable(false);
        return faultInjectionScriptInfo;
    }

}
