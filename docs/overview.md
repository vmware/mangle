# Overview

Mangle enables you to run chaos engineering experiments seamlessly against applications and infrastructure components to assess resiliency and fault tolerance. It is designed to introduce faults with very little pre-configuration and can support any infrastructure that you might have including K8S, Docker, vCenter or any Remote Machine with ssh enabled. With it's powerful plugin model, you can define a custom fault of your choice and run it without actually building your code from scratch.

The solution is delivered both as an appliance and as containers. It has the following major components:

**Mangle Cassandra Database Container** forms the persistence layer for the application and is shared across all the nodes in a multi node instance of Mangle. It is preferable to use an external mount point/persistent volumes for the Cassandra DB so that there is no data loss if the container restarts.

**Mangle Application Container** runs the core fault injection and scheduling engine. It retrieves and stores information from the database, controls the connections to an endpoint and runs faults using a robust task framework.

**Mangle vSphere Adapter** is a separate container that manages connections and run faults against the vCenter endpoints. It is not included as part of the core application in-order to make it as light weight as possible.

Copyright \(c\) 2019 VMware, Inc. All rights reserved. [Copyright and trademark information](http://pubs.vmware.com/copyright-trademark.html). Any feedback you provide to VMware is subject to the terms at [www.vmware.com/community\_terms.html](http://www.vmware.com/community_terms.html).

**VMware, Inc.**  
3401 Hillview Ave.  
Palo Alto, CA 94304

[www.vmware.com](http://www.vmware.com/)

