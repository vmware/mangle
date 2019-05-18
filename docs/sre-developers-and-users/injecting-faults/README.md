# Injecting Faults

## Supported Faults

Mangle supports two broad category of faults:

1. Infrastructure Faults
2. Application Faults

**Infrastructure Faults** are a set of faults that target IAAS components where developers host and run their applications.  For eg: this might be a virtual machine or an AWS EC2 instance where the application runs as a service or a Docker host where the application containers are hosted or a K8s cluster where the pods host the application. These components are usually shared with multiple applications running on the same infrastructure and are referred to as **endpoints** in Mangle. So faults against these components will impact multiple applications unless they have different levels of fault tolerance.

**Application Faults** are a set of faults that target specific applications running within a given infrastructure component or endpoint.  For eg: this could be a specific tomcat application running within a virtual machine or an AWS EC2 instance or JAVA applications running within containers on a Docker host or K8s pods. Faults against applications typically will impact just that application and ideally should not bring down any other applications running on the same infrastructure or is dependent on the affected service. If it does, your system is prone to cascading failures and should be examined in great detail to improve fault tolerance levels.

