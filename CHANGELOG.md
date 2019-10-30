

# Change Log

## Version 2.0 (Released 4th November 2019)

Key Features Included are

- Support for endpoint AWS (Amazon Web Services)
- Support for new infrastructure faults:
	- File Handler Leak Fault
	- Disk Space Fault
	- Kernel Panic Fault
	- Network Faults: Packet Delay, Packet Duplication, Packet Loss, Packet Corruption
	- Kubernetes  Service Unavailable Fault
	- AWS EC2 State Change Fault
	- AWS EC2 Network Fault
- Support for new application faults:
	- File Handler Leak Fault
	- Thread Leak Fault
	- Java Method Latency Fault
	- Spring Service Latency Fault
	- Spring Service Exception Fault
	- Simulate Java Exception
	- Kill JVM Fault

## Version 1.0 (Released 21st May 2019)

Initial release, key features included are

- Support for endpoints for fault injection
	- Kubernetes
	- Docker
	- VMware vCenter
	- Remote Machine
- Support for infrastructure faults:
	- CPU Fault
	- Memory Fault
	- Disk IO Fault
	- Kill Process Fault
	- Docker State Change Faults
	- Kubernetes Delete Resource Fault
	- Kubernetes Resource Not Ready Fault
	- vCenter Disk Fault
	- vCenter NIC Fault
	- vCenter VM State Change Fault
- Support for application faults:
	- CPU Fault
	- Memory Fault
- Publishing of Mangle metrics and fault injection events to metric providers Wavefront and Datadog
- Well definited Swagger API interface for endpoint addition and fault injection