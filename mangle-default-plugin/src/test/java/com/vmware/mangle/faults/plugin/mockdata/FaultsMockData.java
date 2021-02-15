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

import static com.vmware.mangle.services.dto.AgentRuleConstants.HTTP_METHODS_STRING;
import static com.vmware.mangle.services.dto.AgentRuleConstants.SERVICES_STRING;
import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.JAVA_HOME_PATH;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;
import static com.vmware.mangle.utils.constants.FaultConstants.USER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.AzureCredentials;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FilehandlerLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMProperties;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SServiceUnavailableFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.ThreadLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VCenterFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDropConnectionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisReturnErrorFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.model.aws.AwsEC2StateFaults;
import com.vmware.mangle.model.aws.AwsRDSFaults;
import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2InstanceStateFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.model.azure.AzureVMStateFaults;
import com.vmware.mangle.model.azure.faults.spec.AzureVMStateFaultSpec;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.services.enums.DockerFaultName;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.enums.VCenterDiskFaults;
import com.vmware.mangle.services.enums.VCenterNicFaults;
import com.vmware.mangle.services.enums.VCenterStateFaults;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.FaultConstants;

/**
 * Faults Mock Data.
 *
 * @author hkilari
 */
public class FaultsMockData {

    private Properties properties;

    private EndpointMockData endpointMockData = new EndpointMockData();
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();

    private Integer faultLoad;

    private String javahomePath;

    private String jvmProcess;

    private Integer jvmProcessPort;

    private String jvmProcessUser;

    private String testContainername;

    private String testPodLabels;

    private Integer faultExecutionTimeout;

    private String faultTaskId;

    private String httpMethodsString;

    private String servicesString;

    private String bytemanUser;

    private String testPodInAction;

    public FaultsMockData() {
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        faultLoad = Integer.parseInt(properties.getProperty("faultLoad"));
        javahomePath = properties.getProperty("javahomePath");
        jvmProcess = properties.getProperty("jvmProcess");
        jvmProcessPort = Integer.parseInt(properties.getProperty("jvmProcessPort"));
        jvmProcessUser = properties.getProperty("jvmProcessUser");
        testContainername = properties.getProperty("testContainername");
        testPodLabels = properties.getProperty("testPodLabels");
        testPodInAction = properties.getProperty("testPodInAction");
        faultExecutionTimeout = Integer.parseInt(properties.getProperty("faultExecutionTimeout"));
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

    public EndpointGroupFaultTriggerSpec getEndpointGroupCPUFaultTriggerSpec() {
        EndpointGroupFaultTriggerSpec endpointGroupFaultSpec = new EndpointGroupFaultTriggerSpec();
        CpuFaultSpec cpuFaultSpec = getLinuxCpuFaultSpec();
        endpointGroupFaultSpec.setFaultSpec(cpuFaultSpec);
        endpointGroupFaultSpec.setSchedule(cpuFaultSpec.getSchedule());
        return endpointGroupFaultSpec;
    }

    public EndpointGroupFaultTriggerSpec getEndpointGroupBytemanCPUFaultTriggerSpec() {
        EndpointGroupFaultTriggerSpec endpointGroupFaultSpec = new EndpointGroupFaultTriggerSpec();
        CpuFaultSpec cpuFaultSpec = getLinuxCpuJvmAgentFaultSpec();
        endpointGroupFaultSpec.setFaultSpec(cpuFaultSpec);
        endpointGroupFaultSpec.setSchedule(cpuFaultSpec.getSchedule());
        return endpointGroupFaultSpec;
    }

    public EndpointGroupFaultTriggerSpec getEndpointGroupCodeLevelFaultTriggerSpec() {
        EndpointGroupFaultTriggerSpec endpointGroupFaultSpec = new EndpointGroupFaultTriggerSpec();
        JVMCodeLevelFaultSpec cpuFaultSpec = getLinuxJvmCodelevelFaultSpec();
        endpointGroupFaultSpec.setFaultSpec(cpuFaultSpec);
        endpointGroupFaultSpec.setSchedule(cpuFaultSpec.getSchedule());
        return endpointGroupFaultSpec;
    }

    public K8SFaultTriggerSpec getK8SCPUFaultTriggerSpecWithDisabledRsourceLabels() {
        K8SFaultTriggerSpec k8SFaultSpec = getK8SCPUFaultTriggerSpec();
        k8SFaultSpec.getFaultSpec().getEndpoint().getK8sConnectionProperties()
                .setDisabledResourceLabels(CommonUtils.stringKeyValuePairToMap(testPodLabels));
        return k8SFaultSpec;
    }

    public K8SFaultTriggerSpec getK8SJVMCodeLevelFaultTriggerSpec() {
        K8SFaultTriggerSpec k8SFaultSpec = new K8SFaultTriggerSpec();

        JVMCodeLevelFaultSpec jvmCodeLevelFaultSpec = getK8sSpringExceptionJVMCodeLevelFaultSpec();

        k8SFaultSpec.setFaultSpec(jvmCodeLevelFaultSpec);
        k8SFaultSpec.setSchedule(jvmCodeLevelFaultSpec.getSchedule());
        return k8SFaultSpec;
    }

    public CpuFaultSpec getK8SCPUFaultSpec() {
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CpuFaultSpec cpuFaultSpec = new CpuFaultSpec();
        cpuFaultSpec.setCpuLoad(faultLoad);
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, String.valueOf(cpuFaultSpec.getCpuLoad()));
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

    public CpuFaultSpec getK8sCpuJvmAgentFaultSpec() {
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CpuFaultSpec cpuFaultSpec = new CpuFaultSpec();
        cpuFaultSpec.setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, String.valueOf(faultLoad));
        cpuFaultSpec.setArgs(args);
        cpuFaultSpec.setCpuLoad(faultLoad);
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
        faultSpec.setEndpointName(endpointSpec.getName());
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

    public CpuFaultSpec getLinuxCpuJvmAgentFaultSpec() {
        CpuFaultSpec cpuFaultSpec = getLinuxCpuFaultSpec();
        cpuFaultSpec.setJvmProperties(getJVMProperties());
        return cpuFaultSpec;
    }

    public CpuFaultSpec getLinuxCpuFaultSpec() {
        RemoteMachineCredentials credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        CpuFaultSpec cpuFaultSpec = new CpuFaultSpec();
        cpuFaultSpec.setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        Map<String, String> args = new HashMap<>();
        cpuFaultSpec.setCpuLoad(faultLoad);
        args.put(LOAD_ARG, String.valueOf(faultLoad));
        cpuFaultSpec.setArgs(args);
        cpuFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);
        List<SupportScriptInfo> listSupportScript = new ArrayList<>();
        listSupportScript.add(getSupportScriptInfo());
        cpuFaultSpec.setSupportScriptInfo(listSupportScript);
        cpuFaultSpec.setEndpointName(endpointSpec.getName());
        cpuFaultSpec.setEndpoint(endpointSpec);
        cpuFaultSpec.setCredentials(credentialsSpec);
        return cpuFaultSpec;
    }

    public CpuFaultSpec getDockerCpuJvmAgentFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        CpuFaultSpec cpuFaultSpec = new CpuFaultSpec();
        cpuFaultSpec.setFaultName(AgentFaultName.INJECT_CPU_FAULT.getValue());
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, String.valueOf(faultLoad));
        cpuFaultSpec.setArgs(args);
        cpuFaultSpec.setCpuLoad(faultLoad);
        cpuFaultSpec.setJvmProperties(getJVMProperties());
        cpuFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);
        cpuFaultSpec.setEndpointName(endpointSpec.getName());
        cpuFaultSpec.setEndpoint(endpointSpec);
        cpuFaultSpec.setId("cpuid");
        SupportScriptInfo supportScript = new SupportScriptInfo();
        supportScript.setScriptFileName(FaultConstants.INFRA_AGENT_NAME);
        supportScript.setTargetDirectoryPath("/tmp/");
        supportScript.setClassPathResource(true);
        supportScript.setExecutable(true);
        List<SupportScriptInfo> listSupportScript = new ArrayList<>();
        listSupportScript.add(supportScript);
        cpuFaultSpec.setSupportScriptInfo(listSupportScript);
        cpuFaultSpec.setDockerArguments(getDockerSpecificArguments());
        return cpuFaultSpec;
    }


    public JVMAgentFaultSpec getDockerCpuJvmAgentFaultSpecV2() {
        JVMAgentFaultSpec cpuFaultSpec = getDockerCpuJvmAgentFaultSpec();
        cpuFaultSpec.setArgs(setFaultArgsV2());
        return cpuFaultSpec;
    }

    public FilehandlerLeakFaultSpec getFilehandlerLeakFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        FilehandlerLeakFaultSpec filehandlerFaultSpec = new FilehandlerLeakFaultSpec();
        filehandlerFaultSpec.setFaultName(AgentFaultName.INJECT_FILE_HANDLER_FAULT.getValue());
        filehandlerFaultSpec.setJvmProperties(getJVMProperties());
        filehandlerFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);
        filehandlerFaultSpec.setEndpointName(endpointSpec.getName());
        filehandlerFaultSpec.setEndpoint(endpointSpec);
        filehandlerFaultSpec.setDockerArguments(getDockerSpecificArguments());
        Map<String, String> args = new HashMap<>();
        args.put(JAVA_HOME_PATH, javahomePath);
        filehandlerFaultSpec.setArgs(args);
        return filehandlerFaultSpec;
    }

    public ThreadLeakFaultSpec getThreadLeakFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        ThreadLeakFaultSpec threadLeakFaultSpec = new ThreadLeakFaultSpec();
        threadLeakFaultSpec.setFaultName(AgentFaultName.INJECT_THREAD_LEAK_FAULT.getValue());
        threadLeakFaultSpec.setJvmProperties(getJVMProperties());
        threadLeakFaultSpec.setTimeoutInMilliseconds(faultExecutionTimeout);
        threadLeakFaultSpec.setEndpointName(endpointSpec.getName());
        threadLeakFaultSpec.setEndpoint(endpointSpec);
        threadLeakFaultSpec.setEnableOOM(false);
        threadLeakFaultSpec.setDockerArguments(getDockerSpecificArguments());
        Map<String, String> args = new HashMap<>();
        args.put(JAVA_HOME_PATH, javahomePath);
        threadLeakFaultSpec.setArgs(args);
        return threadLeakFaultSpec;
    }

    private Map<String, String> setFaultArgsV2() {
        Map<String, String> args = new HashMap<>();
        args.put(LOAD_ARG, String.valueOf(faultLoad));
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

        DockerSpecificArguments dockerArguments = new DockerSpecificArguments();
        dockerArguments.setContainerName("testContainer");
        springServiceExceptionFaultSpec.setDockerArguments(dockerArguments);
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
        k8sArguments.setPodInAction(testPodInAction);
        k8sArguments.setPodLabels(testPodLabels);
        return k8sArguments;
    }

    public DockerSpecificArguments getDockerSpecificArguments() {
        DockerSpecificArguments dockerArgs = new DockerSpecificArguments();
        dockerArgs.setContainerName(testContainername);
        return dockerArgs;
    }

    public K8SResourceNotReadyFaultSpec getK8SResourceNotReadyFaultSpec() {
        K8SResourceNotReadyFaultSpec k8SResourceNotReadyFaultSpec = new K8SResourceNotReadyFaultSpec();
        k8SResourceNotReadyFaultSpec.setAppContainerName(testContainername);
        k8SResourceNotReadyFaultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        k8SResourceNotReadyFaultSpec.setEndpointName(endpointMockData.k8sEndpointMockData().getName());
        k8SResourceNotReadyFaultSpec.setCredentials(credentialsSpecMockData.getk8SCredentialsData());
        k8SResourceNotReadyFaultSpec.setResourceType(K8SResource.POD);
        Map<String, String> resourceLabels = new HashMap<>();
        resourceLabels.put("app", "app-inventory-service");
        k8SResourceNotReadyFaultSpec.setResourceLabels(resourceLabels);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put("operation", K8SFaultName.NOTREADY_RESOURCE.name());
        k8SResourceNotReadyFaultSpec.setArgs(specificArgs);
        k8SResourceNotReadyFaultSpec.setResourceType(K8SResource.POD);
        k8SResourceNotReadyFaultSpec.setRandomInjection(false);
        return k8SResourceNotReadyFaultSpec;
    }

    public K8SServiceUnavailableFaultSpec getK8SServiceUnavailableFaultSpec() {
        K8SServiceUnavailableFaultSpec k8SServiceUnavailableFaultSpec = new K8SServiceUnavailableFaultSpec();
        k8SServiceUnavailableFaultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        k8SServiceUnavailableFaultSpec.setEndpointName(endpointMockData.k8sEndpointMockData().getName());
        k8SServiceUnavailableFaultSpec.setCredentials(credentialsSpecMockData.getk8SCredentialsData());
        Map<String, String> resourceLabels = new HashMap<>();
        resourceLabels.put("app", "app-inventory-service");
        k8SServiceUnavailableFaultSpec.setResourceLabels(resourceLabels);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put("operation", K8SFaultName.SERVICE_UNAVAILABLE.name());
        k8SServiceUnavailableFaultSpec.setArgs(specificArgs);
        k8SServiceUnavailableFaultSpec.setResourceType(K8SResource.SERVICE);
        k8SServiceUnavailableFaultSpec.setRandomInjection(false);
        return k8SServiceUnavailableFaultSpec;
    }

    public K8SFaultSpec getDeleteK8SResourceFaultSpec() {
        K8SFaultSpec k8SFaultSpec = new K8SFaultSpec();
        k8SFaultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        k8SFaultSpec.setEndpointName(endpointMockData.k8sEndpointMockData().getName());
        k8SFaultSpec.setCredentials(credentialsSpecMockData.getk8SCredentialsData());
        Map<String, String> resourceLabels = new HashMap<>();
        resourceLabels.put("app", "app-inventory-service");
        k8SFaultSpec.setResourceLabels(resourceLabels);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put("operation", K8SFaultName.DELETE_RESOURCE.name());
        k8SFaultSpec.setArgs(specificArgs);
        k8SFaultSpec.setResourceType(K8SResource.POD);
        k8SFaultSpec.setRandomInjection(false);
        return k8SFaultSpec;
    }

    public VMStateFaultSpec getVMStateFaultSpec() {
        VMStateFaultSpec faultSpec = new VMStateFaultSpec();
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put(Constants.VM_NAME_ARG, "vm");
        faultSpec.setArgs(specificArgs);
        VCenterCredentials creds = credentialsSpecMockData.getVCenterCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.vCenterEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(VCenterStateFaults.POWEROFF_VM);
        faultSpec.setFaultName(faultSpec.getFault().name());
        faultSpec.setVmName("vm");

        return faultSpec;
    }

    public VMDiskFaultSpec getVMDiskFaultSpec() {
        VMDiskFaultSpec faultSpec = new VMDiskFaultSpec();
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put(Constants.VM_NAME_ARG, "vm");
        faultSpec.setArgs(specificArgs);
        VCenterCredentials creds = credentialsSpecMockData.getVCenterCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.vCenterEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(VCenterDiskFaults.DISCONNECT_DISK);
        faultSpec.setFaultName(VCenterDiskFaults.DISCONNECT_DISK.name());
        faultSpec.setVmName("vm");
        return faultSpec;
    }

    public VMNicFaultSpec getVMNicFaultSpec() {
        VMNicFaultSpec faultSpec = new VMNicFaultSpec();
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put(Constants.VM_NAME_ARG, "vm");
        faultSpec.setArgs(specificArgs);
        VCenterCredentials creds = credentialsSpecMockData.getVCenterCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.vCenterEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(VCenterNicFaults.DISCONNECT_NIC);
        faultSpec.setFaultName(VCenterNicFaults.DISCONNECT_NIC.name());
        faultSpec.setVmName("vm");
        return faultSpec;
    }

    public AwsEC2InstanceStateFaultSpec getAwsEC2InstanceStateFaultSpec() {
        AwsEC2InstanceStateFaultSpec faultSpec = new AwsEC2InstanceStateFaultSpec();
        AWSCredentials creds = credentialsSpecMockData.getAwsCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(AwsEC2StateFaults.STOP_INSTANCES);
        faultSpec.setInstanceIds(new ArrayList<>());
        faultSpec.setFaultName(faultSpec.getFault().name());
        return faultSpec;
    }

    public AwsRDSFaultSpec getAwsRDSFaultSpec() {
        AwsRDSFaultSpec faultSpec = new AwsRDSFaultSpec();
        List<String> dbIdentifiers = new ArrayList<>();
        dbIdentifiers.add("id1");
        dbIdentifiers.add("id2");
        List<AwsRDSInstance> selectedRdsInstances = new ArrayList<>();
        AwsRDSInstance rdsInstance = new AwsRDSInstance();
        rdsInstance.setDbEngine("postgres");
        rdsInstance.setDbRole("Instance");
        rdsInstance.setInstanceIdentifier(dbIdentifiers.get(0));
        selectedRdsInstances.add(rdsInstance);
        AWSCredentials creds = credentialsSpecMockData.getAwsCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(AwsRDSFaults.STOP_INSTANCES);
        faultSpec.setDbIdentifiers(dbIdentifiers);
        faultSpec.setSelectedRDSInstances(selectedRdsInstances);
        faultSpec.setFaultName(faultSpec.getFault().name());
        return faultSpec;
    }

    public AzureVMStateFaultSpec getAzureVMStateFaultSpec() {
        AzureVMStateFaultSpec faultSpec = new AzureVMStateFaultSpec();
        AzureCredentials creds = credentialsSpecMockData.getAzureCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        faultSpec.setCredentials(creds);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setFault(AzureVMStateFaults.STOP_VMS);
        faultSpec.setResourceIds(new ArrayList<>());
        faultSpec.setFaultName(faultSpec.getFault().name());

        return faultSpec;
    }

    public SupportScriptInfo getSupportScriptInfo() {
        SupportScriptInfo faultInjectionScriptInfo = new SupportScriptInfo();
        faultInjectionScriptInfo.setClassPathResource(true);
        faultInjectionScriptInfo.setScriptFileName(FaultConstants.INFRA_AGENT_NAME);
        faultInjectionScriptInfo.setTargetDirectoryPath(DEFAULT_TEMP_DIR);
        faultInjectionScriptInfo.setExecutable(false);
        return faultInjectionScriptInfo;
    }

    public RedisDelayFaultSpec getRedisDelayFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.getRedisProxyEndpointMockData();
        RedisDelayFaultSpec delayFaultSpec = new RedisDelayFaultSpec();
        delayFaultSpec.setEndpointName(endpointSpec.getName());
        delayFaultSpec.setEndpoint(endpointSpec);
        delayFaultSpec.setDelay(1000);
        delayFaultSpec.setPercentage(50);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.OPERATION, delayFaultSpec.getFaultName());
        delayFaultSpec.setArgs(specificArgs);
        return delayFaultSpec;
    }

    public RedisReturnErrorFaultSpec getRedisReturnErrorFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.getRedisProxyEndpointMockData();
        RedisReturnErrorFaultSpec faultSpec = new RedisReturnErrorFaultSpec();
        faultSpec.setEndpointName(endpointSpec.getName());
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setErrorType("CON");
        faultSpec.setPercentage(50);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.OPERATION, faultSpec.getFaultName());
        faultSpec.setArgs(specificArgs);
        return faultSpec;
    }

    public RedisFaultSpec getRedisFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.getRedisProxyEndpointMockData();
        RedisFaultSpec faultSpec = new RedisFaultSpec();
        faultSpec.setEndpointName(endpointSpec.getName());
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setPercentage(25);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.OPERATION, faultSpec.getFaultName());
        faultSpec.setArgs(specificArgs);
        return faultSpec;
    }

    public RedisDropConnectionFaultSpec getRedisDropConnectionFaultSpec() {
        EndpointSpec endpointSpec = endpointMockData.getRedisProxyEndpointMockData();
        RedisDropConnectionFaultSpec faultSpec = new RedisDropConnectionFaultSpec();
        faultSpec.setEndpointName(endpointSpec.getName());
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setPercentage(25);
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.OPERATION, faultSpec.getFaultName());
        faultSpec.setArgs(specificArgs);
        return faultSpec;
    }

    public VCenterFaultTriggerSpec getVcenterFaultTriggerSpecForVMstateFault() {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = new VCenterFaultTriggerSpec();
        VMStateFaultSpec vmStateFaultSpec = getVMStateFaultSpec();
        vCenterFaultTriggerSpec.setFaultSpec(vmStateFaultSpec);
        vCenterFaultTriggerSpec.setSchedule(vmStateFaultSpec.getSchedule());
        return vCenterFaultTriggerSpec;
    }

    public VCenterFaultTriggerSpec getVcenterFaultTriggerSpecForVMDiskFault() {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = new VCenterFaultTriggerSpec();
        VMDiskFaultSpec vmDiskFaultSpec = getVMDiskFaultSpec();
        vCenterFaultTriggerSpec.setFaultSpec(vmDiskFaultSpec);
        vCenterFaultTriggerSpec.setSchedule(vmDiskFaultSpec.getSchedule());
        return vCenterFaultTriggerSpec;
    }

    public VCenterFaultTriggerSpec getVcenterFaultTriggerSpecForVMNicFault() {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = new VCenterFaultTriggerSpec();
        VMNicFaultSpec vmNicFaultSpec = getVMNicFaultSpec();
        vCenterFaultTriggerSpec.setFaultSpec(vmNicFaultSpec);
        vCenterFaultTriggerSpec.setSchedule(vmNicFaultSpec.getSchedule());
        return vCenterFaultTriggerSpec;
    }
}
