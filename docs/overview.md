# Overview

## What is Mangle?

Mangle enables you to run chaos engineering experiments seamlessly against applications and infrastructure components to assess resiliency and fault tolerance. It is designed to introduce faults with very little pre-configuration and can support any infrastructure that you might have including K8S, Docker, vCenter or any Remote Machine with ssh enabled. With it's powerful plugin model, you can define a custom fault of your choice and run it with out actually building your code from scratch.

It contains two major components: the web application Mangle and the persistence layer Cassandra.

## Advantages of Mangle

* Highly extensible making it easy to add support for new faults
* Supports a variety of infrastructure endpoints such as k8s, Docker, Any remote machine and vCenter
* Supports code level injection in Java applications
* Does not require an external agent to be installed or configured before use
* Highly available and offers multi-node support



