# Custom Faults

## **Mangle Custom Fault Development**

1.  The Mangle custom fault plugin framework is designed with the help of [pf4j](https://github.com/pf4j/pf4j) using [pf4j-spring](https://github.com/pf4j/pf4j-spring). Please go through their documentation to understand the design of custom fault plugins in Mangle.


2.  Clone [https://github.com/vmware/mangle.git](https://github.com/vmware/mangle.git)


3.  Rename module _**mangle-plugin-skeleton**_ with any desired name.&#x20;


4.  Update fields present in _**plugin.properties**_ file available at location src/main/resources

    `plugin.id=mangle-plugin-skeleton `

    `plugin.class=com.vmware.mangle.plugin.ManglePlugin `

    `plugin.version=2.0.0 `

    `plugin.provider=VMware Inc. `

    `plugin.dependencies=,`


5.  The user can Rename `ManglePlugin` to his desired name and can update its corresponding reference in plugin.properties introduced in step-4.&#x20;


6.  The user can Rename `ManglePluginSpringConfig` to his desired name and have to update its corresponding reference in `ManglePlugin` introduced in step-5. The user has to retain it as a Spring Configuration class and he should not be removing Beans defined already. He should be declaring any new Spring beans going to be implemented by him in `ManglePluginSpringConfig`.&#x20;


7.  All the Mangle faults are designed as Asynchronous task and the user can verify the status by polling on the task status. So, the user required to define his Custom faults as the task can be triggered by mangle-task-framework. The Mangle provides well defined interface and an extensive implementation of that interface to help the plugin developer.&#x20;


8.  The user should develop three types of classes as pf4j extensions for implementing the custom fault.&#x20;

    1. Model-Extension: Model extension is the data object for the task corresponding to Custom Fault. It should also extend the `PluginFaultSpec`, the base class for all the mangle fault inputs from user.&#x20;
    2. Task-Extension: Task extension is the logical implementation of the Fault. It must implement the `AbstractTaskHelper `interface defined by mangle-task -framework. ‘mangle-task-framework' also provide `AbstractRemoteCommandExecutionTaskHelper `extensive implemented version of `AbstractTaskHelper`. By using the `AbstractRemoteCommandExecutionTaskHelper`, the developer of plugin is required to only provide the Injection and Remediation Commands and should not be concerned with rest of the task management.&#x20;
    3. Fault-Extension: Fault extension hold the transformation logic to convert the User inputs of the Fault to the Model-Extension corresponding to its Task-Extension. This help user of custom fault to provide a very simple input and the task to have elaborated data class supporting wider management options. The Fault-Extension should extend `AbstractCustomFault `of mangle.&#x20;

    More details provided for each of the above extensions in their own sections.&#x20;


9.  The last step for the user is to create the descriptor file with the Information as shown below.&#x20;

    1. Model Class of Descriptor File: com.vmware.mangle.cassandra.model.plugin. ManglePluginDescriptor&#x20;
    2.  Sample Descriptor File: The descriptor file should be placed at ‘src/main/resources’&#x20;

        \
        `{`

        `    "pluginId": "mangle-plugin-skeleton",`

        `    "faults": [`

        `        {`

        `            "faultParameters": {`

        `                "field1": "field1",`

        `                "field2": "field2"`

        `            },`

        `            "extensionDetails": {`

        `                "modelExtensionName": "com.vmware.mangle.plugin.model.faults.specs.HelloMangleFaultSpec",`

        `                "taskExtensionName": "com.vmware.mangle.plugin.tasks.impl.HelloManglePluginTaskHelper",`

        `                "faultExtensionName": "com.vmware.mangle.plugin.helpers.faults.HelloMangleFault"`

        `            },`

        `            "faultName": "mangle-plugin-skeleton-HelloMangleFault",`

        `            "supportedEndpoints": [`

        `                "MACHINE"`

        `            ],`

        `            "pluginId": "mangle-plugin-skeleton"`

        `        }`

        `    ]`

        `}`


10. The mangle plugin supposed to be built using the maven-assembly -plugin and the required configuration is already available at assembly.xml file in src/main/resources directory. The plugin developers are requested to not change the default configuration of maven-assembly -plugin provided in pom.xml and assembly.xml without knowledge on how the assembly plugin works.&#x20;


11. Post the successful building of the plugin, User can use mangle plugin-controller API/mangle-ui to deploy or perform management of the plugin.&#x20;


12. The successful registration of the plugin into mangle will enable seamless invocation of the custom Faults as per the regular flow of OOTB mangle Faults in Mangle-ui. However the api flow needs additional steps as described below.&#x20;

    1.  Find all the registered Custom faults at&#x20;

        `GET `[`https://localhost:8443/mangle-services/rest/api/v1/plugins/plugin-details?pluginId=mangle-plugin-skeleton`](https://localhost:8443/mangle-services/rest/api/v1/plugins/plugin-details?pluginId=mangle-plugin-skeleton)
    2.  Find the sample request data for any registered custom Fault at&#x20;

        `GET `[`https://localhost:8443/mangle-services/rest/api/v1/plugins/request-json?faultName=`](https://localhost:8443/mangle-services/rest/api/v1/plugins/request-json?faultName=)`  mangle-plugin-skeleton-HelloMangleFault&pluginId=mangle-plugin-skeleton  `
    3.  Invoke Custom fault by providing the request as per the sample received in last step at&#x20;

        `POST `[`https://localhost:8443/mangle-services/rest/api/v1/plugins/custom-fault`](https://localhost:8443/mangle-services/rest/api/v1/plugins/custom-fault)` `

        Sample Request:&#x20;

        \
        `{`

        `  "faultName": "mangle-plugin-skeleton-HelloMangleFault",`

        `  "endpointName": "testEndpoint",`

        `  "faultParameters": {`

        `    "field1": "Hi",`

        `    "field2": "Mangle"`

        `  },`

        `  "pluginId": "mangle-plugin-skeleton"`

        `}`


13. Model-Extension: An example is available as `HelloMangleFaultSpec `at package com.vmware.mangle.plugin.model.faults.specs of mangle-plugin-skeleton. Plugin developer is expected to provide only the parameters he is expecting from the user of his fault while executing in his environment. The plugin developer can conveniently Ignore the fields that are inherited from the base class `CommandExecutionFaultSpec` which are designed for the Management of Faults as Asynchronous tasks in Mangle.&#x20;


14. Task-Extension: An example is available as `HelloManglePluginTaskHelper` at package com.vmware.mangle.test.plugin.helpers of mangle-plugin-skeleton. This task Helper is an implementation of `AbstractRemoteCommandExecutionTaskHelper`. The implementation of `AbstractRemoteCommandExecutionTaskHelper `is only expected to provide the implementation for below methods:



    **`public Task init(T faultSpec) throws MangleException;`**&#x20;

    Should provide the Implementation to initialize the Task Helper for executing the Fault. And the commands required for injection/remediation of the Fault are expected to be provided here. More details on the model for providing the Command Information is explained later.&#x20;



    **`public Task init(T taskData, String injectedTaskId) throws MangleException; `**

    Should provide the Implementation to initialize the Task Helper for executing the Fault, if the existing Task id also provided. This method will be used for executing the Remediation on a Task if the Remediation is available. This initialization is not used for task rerun or the Re-trigger.&#x20;



    **`public void executeTask(Task task) throws MangleException; `**

    Provide the Implementation for execution steps required in addition to Implementation available in `AbstractRemoteCommandExecutionTaskHelper`. Plugin developer can use this interface to invoke his own implementation of Helpers for supporting his Fault across multiple endpoints supported in mangle.&#x20;



    **`protected ICommandExecutor getExecutor(Task task) throws MangleException; `**

    Provide the Implementation for defining the Executor required for the Fault Execution. Mangle provide a default implementation of a executor for each Supported Endpoint. The Plugin user is free to use his own executor as long as he is implementing the resource as per the interface `ICommandExecutor `available at package com.vmware.mangle.utils;&#x20;



    **`protected void checkTaskSpecificPrerequisites(Task task) throws MangleException; `**

    Provide the Implementation if the Fault being developed expect the test machine to be satisfying a condition for the execution. This step is separated from the Fault execution as Mangle wants to make sure the Fault execution or Remediation will not leave the user environment in a irrecoverable state due to execution of them in a non-perquisite satisfying machine.&#x20;



    **`protected void prepareEndpoint(Task task, List listOfFaultInjectionScripts) throws MangleException; `**Provide the Implementation if the Fault execution needs certain changes to the Test Machine before execution. Examples are Copying a binary file required to execute a fault. This step is optional for user as the predefined implementation already copies the files returned by `listFaultInjectionScripts() `to the remote machine.&#x20;



    **`public String getDescription(Task task); `**

    Provide Implementation to generate description for Fault based on user inputs to help him to identify the task in future through the description. A generic implementation is already available at TaskDescriptionUtils.getDescription(task).&#x20;



    **`public List listFaultInjectionScripts(Task task);`**&#x20;

    Provide a implementation that return details of the support scrips to be copied to test machine required for executing the fault getting implemented.&#x20;


15. Fault-Extension: Fault Extension helps developer to define the supported endpoints of mangle to execute the fault being developed using the annotation `@SupportedEndpoints(endPoints = { EndpointType})`. The endpoints currently supported are MACHINE, K8S\_CLUSTER, DOCKER, VCENTER, WAVEFRONT, AWS. With the specification of supported Endpoints Mangle will automatically restrict user to execute Fault only on the supported endpoints. Fault Developer is not required to implement any custom implementation for achieving the same.&#x20;


16. Mangle does not support the inclusion of Custom Endpoints through Plugin. The requirement of addition of endpoint can be gone through the Mange contributions flow as defined in Mangle repository.&#x20;


17. Task-Extension Deep Dive: An example is available as `HelloManglePluginTaskHelper `at package com.vmware.mangle.plugin.tasks.impl of mangle-plugin-skeleton. This task Helper is an implementation of `AbstractRemoteCommandExecutionTaskHelper`. The implementation of `AbstractRemoteCommandExecutionTaskHelper `is only expected to provide the implementation for below methods:&#x20;



    **`public Task init(T faultSpec) throws MangleException ;`**&#x20;

    Should provide the Implementation to initialize the Task Helper for executing the Fault. And the commands required for injection/remediation of the Fault are expected to be provided here. More details on the model for providing the Command Information is explained later.&#x20;



    **`public Task init(T taskData, String injectedTaskId) throws MangleException;`**&#x20;

    Should provide the Implementation to initialize the Task Helper for executing the Fault, if the existing Task id also provided. This method will be used for executing the Remediation on a Task if the Remediation is available. This initialization is not used for task rerun or the Re-trigger.&#x20;



    **`public void executeTask(Task task) throws MangleException; `**

    Provide the Implementation for execution steps required in addition to Implementation available in `AbstractRemoteCommandExecutionTaskHelper`. Plugin developer can use this interface to invoke his own implementation of Helpers for supporting his Fault across multiple endpoints supported in mangle.&#x20;



    **`protected ICommandExecutor getExecutor(Task task) throws MangleException; `**

    Provide the Implementation for defining the Executor required for the Fault Execution. Mangle provide a default implementation of a executor for each Supported Endpoint. The Plugin user should use appropriate executor as per the endpoint provided as the target. Below is the Mapping of Executors to their Endpoint Types.&#x20;

    1. REMOTE\_MACHINE – SSHUtils&#x20;
    2. DOCKER - DockerCommandUtils&#x20;
    3. AWS - AWSCommandExecutor&#x20;
    4. K8s - KubernetesCommandLineClient&#x20;
    5. vCENTER - VCenterCommandExecutor&#x20;

    EndpointClientFactory class of mangle-task-framework can be used for initializing the appropriate Executor for Injecting the Fault as per user request.&#x20;

    All these executors expect the user to provide a command to be executed on the target machine with associated meta data to mark if it is executed successfully.&#x20;



    **`protected void checkTaskSpecificPrerequisites(Task task) throws MangleException; `**

    Provide the Implementation if the Fault being developed expect the test machine to be satisfying a condition for the execution. This step is separated from the Fault execution as Mangle wants to make sure the Fault execution or Remediation will not leave the user environment in a irrecoverable state due to execution of them in a non-perquisite satisfying machine.&#x20;



    **`protected void prepareEndpoint(Task task, List listOfFaultInjectionScripts) throws MangleException; `**Provide the Implementation if the Fault execution needs certain changes to the Test Machine before execution. Examples are Copying a binary file required to execute a fault. This step is optional for user as the predefined implementation already copies the files returned by `listFaultInjectionScripts()` to the remote machine.&#x20;



    **`public String getDescription(Task task); `**

    Provide Implementation to generate description for Fault based on user inputs to help him to identify the task in future through the description. A generic implementation is already available at `TaskDescriptionUtils.getDescription(task)`.&#x20;



    **`public List listFaultInjectionScripts(Task task); `**

    Provide an implementation that return details of the support scrips to be copied to test machine required for executing the fault getting implemented. The support files can be any file required to be placed in the target in order to execute the developed fault. All the out of the box executors is capable of copying files to the corresponding targeted endpoint and the process completes automatically by default implementation of the `AbstractRemoteCommandExecutionTaskHelper`, provide that the names of the files are returned through `listFaultInjectionScripts()` implementation.&#x20;



    **`private List getInjectionCommandInfoList(T faultSpec) {} `**

    Provide the commands to be executed for the Fault to be Injected. The commands should be provided as List. The fields and descriptions for the CommandInfo Fields.&#x20;

    1. `private String command;` String value of the actual command with references to members in pool of variables will be available to executor during command execution. The types and the accessing mechanism are explained in below section.&#x20;
    2. `private boolean ignoreExitValueCheck;` Boolean value to find if a command execution result should consider the return value of the command execution. Can be given false where there can be possibility that the command execution need not be resulted in only success return value, but it will be based on the command output.&#x20;
    3. `private List expectedCommandOutputList;` List of patterns to be provided to validate a command execution output to consider if the execution is success. The relation among the patterns verification is defaulted to logical ‘or’.&#x20;
    4. `private int noOfRetries;` Retries to be attempted by the executor before marking the command execution as a Failure.&#x20;
    5. `private int retryInterval;` Interval in seconds between any two attempts of a command execution incase of execution failures and opted for retry attempts.&#x20;
    6. `private int timeout;` Timeout interval in milliseconds to consider a command execution failure if the response was not received by the executor from the target.&#x20;
    7. `private Map knownFailureMap;` Mapping of Patterns to be looked for in the command execution output, to provide easier troubleshooting messages to user by masking stack traces in the result.&#x20;
    8.  `private List commandOutputProcessingInfoList;` Explained in detailed below.&#x20;



        `public class CommandOutputProcessingInfo `

        Fields are&#x20;

        1.  `private String regExpression;`&#x20;

            Regular Expression Pattern to be used to collect an crucial information from current command’s execution to make it available throughout the Fault execution.&#x20;
        2.  `private String extractedPropertyName; `

            Name should be given to the collected information using the pattern given as regExpression&#x20;



        **Types of Variables and Their Usage: **

        The information provided by the user or collected during the runtime of Fault are made available to command executor as below types of Variables.&#x20;

        1. `TaskTroubleShootingInfo `of the Task holds the extracted information from the command execution Output.&#x20;
        2. args field of `CommandExecutionFaultSpec `available as `taskData `in Task holds the data received from the user as args.&#x20;
        3. `$FI_ADD_INFO_FieldName` can be used to refer to variables from `TaskTroubleShootingInfo `
        4. `$FI_ARG_Fieldname` can be used to refer to variables from args.&#x20;
        5. `$FI_STACK` can be used to refer to the output of the previous command.&#x20;


    9.  `private List getRemediationCommandInfoList(T faultSpec) {} `

        Provide the commands to for remediating the fault already Injected. The semantics of `CommandInfo `is same as it described in the previous section. The args and `TaskTroubleShootingInfo `collected during the injection will be available during the execution of remediation as well. Hence the dependency data from injection task can be passed to remediation by using the References in the commands.
