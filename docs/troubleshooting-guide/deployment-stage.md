# Deployment Stage

Deployment involves deploying the Mangle appliance OVA or the containers.

During Deployment, the user provides customization such as configuring the root password and other optional configurations such as providing TLS certificates.

## **Deployment Failures**

We have not experienced many failures during Deployment. If any issues occur, provide the Support Information from below.

### **Support Information**

Provide the following information to support if encountering Deployment Stage failures:

* Hash (MD5, SHA-1, or SHA-256) of the OVA/container images you deployed
* Deployment method:
* Deployment environment
  * Verify that the targeted datastore has enough space
  * Provide details about the targeted vCenter compute, storage, and networking

## **Known Issues**

#### Mangle fails to start with error "org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean..." and generates huge number of lines in the log file for Mangle web container

_**Workaround:**_

* Stop both WEB and DB container for Mangle.
* Start DB container.
* Once DB is up start WEB container.

_**To free up the space if the log partition will full:**_

* If the log partition shows 100% utilization, navigate to location /var/lib/docker/containers/_\<mangle-container-id>/_.
* Confirm if a log file of format _\<mangle-container-id>_-json.log exists and is of large size.
* If yes, remove the log file of the format _\<mangle-container-id>_-json.log.&#x20;
