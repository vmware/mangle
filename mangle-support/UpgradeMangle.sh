#!/bin/bash

validate_input_prerequisites()
{
    if [ $# -eq 0 -o $# -gt 7 ]
    then
        help
    fi
    while [ $# -gt 0 ]; do
        if [ ! -z $(echo $1 | grep -E "^--") ]; then
            key=$(echo $1 | cut -d= -f1)
            v=$(echo $key | cut -d- -f3)
            export $v=$(echo $1 | cut -d= -f2)
        fi
            shift
    done
    if [ -z $MANGLE_ADMIN_USERNAME ] || [ -z $MANGLE_ADMIN_PASSWORD ] || [ -z $MANGLE_BUILD_NUMBER ]
    then
        help
    fi
}

help(){
    echo "$0 --MANGLE_ADMIN_USERNAME=<Magnle admin username> \\"
    echo "--MANGLE_ADMIN_PASSWORD=<Mangle Admin Password> \\"
    echo "--MANGLE_BUILD_NUMBER=<mangle docker container tag. ex: 1.0.0.15> \\"
    echo "[--MANGLE_NODE_SSH_KEY_PATH=<ssh public key path for mangle nodes> \\"
    echo "--MANGLE_NODE_SSH_USER=<ssh user (should have admin rights) to connect mangle nodes> ]\\"
    echo "[--MANGLE_CONTAINER_NAME=<name of the mangle container>]  [--MANGLE_APP_PORT=<Mangle application port>] \\"
    echo "[--MANGLE_DOCKER_ARTIFACTORY=<artificatory server> ex: es-fault-injection-docker-local.artifactory.eng.vmware.com] \\"
    echo "[--NIC_NAME=<nic name of machine's ipaddress assigned>]"
    exit $errorExitCode
}

init()
{
    TASK_STATUS=""
    errorExitCode=127
    LOG_LOCATION="mangle_upgrade.log"

    if [ -z $NIC_NAME ]; then
        NIC_NAME="eth0"
    fi
    
    CurrentMangleNodeIP=$(ifconfig $NIC_NAME | grep "inet addr" | cut -d ':' -f 2 | cut -d ' ' -f 1)

    if [ -z $CurrentMangleNodeIP ]; then
        echo "could not find ip address of current node on $NIC_NAME. Please provide valid ip address"
        read CurrentMangleNodeIP
        echo $CurrentMangleNodeIP
    fi
    if [ -z $MANGLE_CONTAINER_NAME ]; then
        echo "--MANGLE_CONTAINER_NAME option not provided, so defaulting container name to mangle"
        MANGLE_CONTAINER_NAME="mangle"
    fi

    if [ -z $MANGLE_APP_PORT ]; then
        echo "--MANGLE_APP_PORT option not provided, so defaulting mangle app port to 443"
        MANGLE_APP_PORT="443"
    fi

    if [ -z $MANGLE_DOCKER_ARTIFACTORY ]; then
        echo "--MANGLE_DOCKER_ARTIFACTORY option not provided, so defaulting mangle to mangleuser/mangle:2.0"
        MANGLE_DOCKER_ARTIFACTORY="mangleuser/mangle:2.0"
    fi

    # Initializing docker related variables
    initializeDockerConstants

    # validating existing container
    validateExistingMangleContainer

    MANGLE_BACKUP_IMAGE="${MANGLE_DOCKER_ARTIFACTORY}:${CURRENT_MANGLE_IMAGE_TAG}"

    #Mangle constants including api uris
    MANGLE_BASE_CURL_CMD="curl -o - --max-time 10 --silent --insecure -u ${MANGLE_ADMIN_USERNAME}:${MANGLE_ADMIN_PASSWORD} -H Content-Type:application/json "

    MANGLE_BASE_URI="https://${CurrentMangleNodeIP}:${MANGLE_APP_PORT}/mangle-services"
    MANGLE_NODE_STATUS_CHANGE_URI="${MANGLE_BASE_URI}/rest/api/v1/administration/node-status"

    MANGLE_CHANGE_NODE_STATUS_URI_TEMPLATE="${MANGLE_BASE_CURL_CMD} -X POST -d {\"nodeStatus\":\"<status>\"} ${MANGLE_NODE_STATUS_CHANGE_URI}"

    GET_MANGLE_TASK_STATUS="${MANGLE_BASE_CURL_CMD}${MANGLE_BASE_URI}/rest/api/v1/tasks/"
    GET_MANGLE_CLUSTER_MEMBERS_URI="${MANGLE_BASE_CURL_CMD} ${MANGLE_BASE_URI}/rest/api/v1/cluster-config"

    MANGLE_NODE_HEALTH_CHECK_URI_TEMPLATE="${MANGLE_BASE_CURL_CMD} https://<MANGLE_IP>:${MANGLE_APP_PORT}/mangle-services/application/health"

    #Getting all the nodeips in case of clustered setup
    getAllNodeIpsInTheCluster
}

initializeDockerConstants(){
    DOCKER="docker"
    if [ ! -z "$1" ]
    then
        DOCKER="ssh -i ${MANGLE_NODE_SSH_KEY_PATH}  -o StrictHostKeyChecking=no  $MANGLE_NODE_SSH_USER@${1} docker"    
    fi
    #Docker related constants
    DOCKER_PULL_IMAGE="${DOCKER} pull "
    CONTAINER_BACKUP="${DOCKER} commit -p "
    DOCKER_INSPECT_IMAGE="${DOCKER} inspect --format={{.Config.Image}} "
    DELETE_CONTAINER="${DOCKER} rm -f "
}

extractDBOptionsAndClusterOptions(){
    EXPOSED_PORTS=$(${DOCKER} inspect $MANGLE_CONTAINER_NAME --format='{{range $p, $conf := .NetworkSettings.Ports}} -p {{(index $conf 0).HostPort}}:{{$p}} {{end}}' | sed -E 's/\/([a-zA-Z]+)//g')
    CLUSTER_OPTIONS=$(docker inspect $MANGLE_CONTAINER_NAME --format='{{range $index, $value := .Config.Env}}{{if eq (index (split $value "=") 0) "CLUSTER_OPTIONS" }}{{println (index (split $value "CLUSTER_OPTIONS=") 1)}}{{end}}{{end}}')
    DB_OPTIONS=$(docker inspect $MANGLE_CONTAINER_NAME --format='{{range $index, $value := .Config.Env}}{{if eq (index (split $value "=") 0) "DB_OPTIONS" }}{{println (index (split $value "DB_OPTIONS=") 1)}}{{end}}{{end}}')
    VOLUMES_MOUNTED=$(${DOCKER} inspect $MANGLE_CONTAINER_NAME --format '{{ range .Mounts }} -v {{ .Source }}:{{.Destination}}{{ end }}')
}
validateExistingMangleContainer(){

    verify_mangle_containers

    #Finding current mangle image
    CURRENT_MANGLE_IMAGE_TAG=$(${DOCKER_INSPECT_IMAGE}${MANGLE_CONTAINER_NAME} |cut -d':' -f2)
    CURRENT_BUILD_NUMBER=$(echo $CURRENT_MANGLE_IMAGE_TAG |sed "s/\.//g")
    REQUESTED_BUILD_NUMBER=$(echo $MANGLE_BUILD_NUMBER |sed "s/\.//g")

    if [ $CURRENT_BUILD_NUMBER -eq $REQUESTED_BUILD_NUMBER ]
    then
        echo "Mangle with same BuildNumber: ${MANGLE_BUILD_NUMBER} is already running, hence exiting from upgrade"
        exit $errorExitCode
    fi
    if [ $CURRENT_BUILD_NUMBER -gt $REQUESTED_BUILD_NUMBER ]
    then
        echo "Mangle with Higher build number : ${CURRENT_MANGLE_IMAGE_TAG} is already running, hence exiting from upgrade"
        exit $errorExitCode
    fi
}

verify_mangle_containers()
{
    verify_if_container_is_running ${MANGLE_CONTAINER_NAME}
    if [ $? = 1 ]; then
        echo " ${MANGLE_CONTAINER_NAME} container is NOT running. For upgrade, ${MANGLE_CONTAINER_NAME} must be in running condition. "
        echo " Cannot proceed with the upgrade"
        exit 1
    fi
}

getAllNodeIpsInTheCluster(){
    echo "Getting all the node ips in the mangle cluster"
    nodesResponse=$(${GET_MANGLE_CLUSTER_MEMBERS_URI})
    members=$(echo $nodesResponse | grep -o "\"members\":\[.*\]" |grep -o "\[.*\]"|sed -E "s/\"|\]|\[//g")
    i=0
    for memberIp in $(echo $members | sed "s/,/ /g")
    do
        NODE_IPS[$i]=$memberIp
        i=$(($i + 1))
    done
    echo "NodesIps: [${NODE_IPS[@]}]"

    if [ ${#NODE_IPS[@]} -gt 1 ]
    then
        echo "current mangle setup is clustered. Currently upgrade script not supporting clustered mangle upgrade"
        exit $errorExitCode
    fi
    if [ ${#NODE_IPS[@]} -gt 1 ] && [ -z "$MANGLE_NODE_SSH_KEY_PATH" ]
    then
        echo "Cluster has more than one node[${NODE_IPS[@]}]."
        echo "Upgrade script expects ssh access to other nodes in the cluster. Please configure ssh access to nodes using same private key"
        echo "And provide --MANGLE_NODE_SSH_KEY_PATH and --MANGLE_NODE_SSH_USER"
        exit $errorExitCode
    fi
}

check_command_output()
{
    if [ $1 -eq 0 ]; then
        echo "Executing '$2' was successful"
        return 0
    else
        echo "Status of the executing command: '$2' is '$1' and it is FAILURE"
        return 1
    fi
}

exit_if_command_failed()
{
    if [ $1 -eq 0 ]; then
	    echo "$2"
        return 0
    else
        echo "$3"
        exit $errorExitCode
    fi
}

print_date_time()
{
   now=`date`
   echo "TimeStamp: ${now}"
}


print_debug_message()
{
    echo "$1"
    print_date_time
}

verify_if_container_is_running()
{
    echo "Verifying if the container $1 is running"
    CONTAINER_STATUS=$(docker inspect $1 --format='{{.State.Status}}')

    if [ "${CONTAINER_STATUS}" = "running" ]; then
        echo "${1} container is running"
    else
        return 1
    fi
}

verify_mangle_health()
{
    MANGLE_NODE_HEALTH_CHECK_URI=$( echo $MANGLE_NODE_HEALTH_CHECK_URI_TEMPLATE|sed "s/<MANGLE_IP>/$1/")
    echo "verifying the mangle health using: ${MANGLE_NODE_HEALTH_CHECK_URI}"
    i=0
    while [ $i -lt 10 ]
    do
        response=$(${MANGLE_NODE_HEALTH_CHECK_URI})
        status=$(echo $response | grep -o "\"status\":\"[a-zA-Z]*"|head -1 |cut -f4 -d "\"")
        if [ "$status" = "UP" ]
        then
            echo "Mangle $1 node is UP"
            return 0
        fi
        echo "Mangle still not up .. sleeping for 30s before trying again"
        sleep 30s
        i=$(($i + 1))
    done
    return 1
}

create_container_backup()
{
    echo "Creating the backup of container $1"
    ${CONTAINER_BACKUP} $1 $2
    exit_if_command_failed $?  "backup of container $1 is successful" "Creating backup of $1 has failed. Cannot continue with upgrade. Exiting "
}

create_mangle_backup()
{
    create_container_backup ${MANGLE_CONTAINER_NAME} ${MANGLE_BACKUP_IMAGE}
}

delete_container()
{
    echo "Deleting the container $1"
    DELETE="${DELETE_CONTAINER} $1"
    ${DELETE}
    if [ $? != 0 ]
    then
        echo "Deleting of container ${1} failed. Can't proceed with upgrade. Exiting."
        exit 1
    fi
    echo "Container $1 deleted successfully"
}

create_mangle_web()
{
    echo "Creating the mangle container using the image: $1"
    ${DOCKER} run --name ${MANGLE_CONTAINER_NAME} -d $VOLUMES_MOUNTED -e DB_OPTIONS="${DB_OPTIONS}" -e CLUSTER_OPTIONS="${CLUSTER_OPTIONS}"  $EXPOSED_PORTS $1
    if [ $? != 0 ]; then
        echo "Creating of Mangle container failed."
        return 1
    fi
    echo "Creating of ${MANGLE_CONTAINER_NAME} container succeeded"
    return 0
}

restore_from_backup()
{
    create_mangle_web ${MANGLE_BACKUP_IMAGE}
}

cleanup_mangle_containers()
{
    verify_if_container_is_running ${MANGLE_CONTAINER_NAME}
    if [ $? != 1 ]; then
        delete_container ${MANGLE_CONTAINER_NAME}
    fi
}

restore_mangle()
{
    cleanup_mangle_containers
    restore_db_snapshot
    echo " Restoring the Mangle to the last Backup taken."
    restore_from_backup
    changeMangleNodesStatus "ACTIVE"
    echo "Mangle successfully restored to the last backup"
}


get_mangle_task_status()
{
    task_id=$1
    echo "Getting the status for mangle task ID: ${task_id}"
    echo "${GET_MANGLE_TASK_STATUS}${task_id}"
    response=$(${GET_MANGLE_TASK_STATUS}${task_id})
    echo "API Response for the task Status: ${response}"
    TASK_STATUS=$(echo "${response}" | grep -o "\"taskStatus\":\"[a-zA-Z]*\"" |head -1|cut -f4 -d'"')
    echo "Task Status : ${TASK_STATUS}"
}



changeMangleNodesStatus()
{
    MANGLE_CHANGE_NODE_STATUS_URI=$(echo ${MANGLE_CHANGE_NODE_STATUS_URI_TEMPLATE}|sed "s/<status>/$1/")
    echo "Put Mangle in ${1} using the command: ${MANGLE_CHANGE_NODE_STATUS_URI}"
    response=$($MANGLE_CHANGE_NODE_STATUS_URI)
    echo " Task Response: ${response}"
    task_id=`echo ${response} | grep -o "\"id\":\"[0-9a-z\-]*" | cut -f 4 -d "\""`
    echo "Task ID corresponding to chande mangle node status to ${1} is ${task_id} "
    if [ `echo "${task_id}" | grep -o "^[0-9a-z]*"` ]; then
        wait_for_mangle_task_to_complete ${task_id}
        exit_if_command_failed $?  "Changed mangle status to ${1} successfully" "Chaning Mangle to ${1} is failed. Upgrade cannot be continued. Exiting"
    else
        echo "Response is: ${response} "
        echo "Unable to retrieve the Task ID from the response received. Unable to change mangle node status to ${1}. Exiting"
        exit 1
    fi
    echo "Mangle node status changed to ${1} successfully"
}

wait_for_mangle_task_to_complete()
{
    max_timeout=300
    echo "Waiting for mangle Task ${1} to complete with max timeout of ${max_timeout} seconds"
    time_interval=5
    time_elapsed=0
    while [ ${time_elapsed} -lt ${max_timeout} ]
    do
        get_mangle_task_status ${1}
        if [ "${TASK_STATUS}" = "COMPLETED" ]; then
            echo "Task ${1} is completed"
            return 0
        fi
        if [ "${TASK_STATUS}" = "FAILED" ]; then
            echo "Task ${1} has FAILED. "
            return 1
        fi
        sleep ${time_interval}
        ((time_elapsed=time_elapsed+time_interval))
    done
    echo "Timed out. The task ${1} did NOT complete in the time specified ${max_timeout} "
    return 1
}

take_db_snapshot()
{
    #place holder for taking cassandra db backup
    echo "Please take mangle db backup using <nodetool -u cassandra -pw cassandra snapshot mangledb>"
    echo "For reference to take db snapshot: https://docs.datastax.com/en/cassandra/3.0/cassandra/operations/opsBackupTakesSnapshot.html"
    echo "Is snapshot taken (Y/N):"
    read backupConfirmation
    if [ $backupConfirmation != "Y" ]
    then
        echo "Please take a mangle db snapshot before proceeding with the upgrade"
        echo "Moving mangle out of maintanance mode"
        changeMangleNodesStatus "ACTIVE"
        exit $errorExitCode
    fi
}

restore_db_snapshot()
{
    #place holder for restoring cassandra db backup
    echo "Please restore db to the previously taken snapshot"
    echo "For reference to restore snapshot: https://docs.datastax.com/en/cassandra/3.0/cassandra/operations/opsBackupSnapshotRestore.html#opsBackupSnapshotRestore__local-backup"
    echo "Restored the snaphot (Y/N):"
    read restoreConfirmation
    if [ $restoreConfirmation != "Y" ]
    then
        restore_mangle
    fi
}

restore_if_failure()
{
    if [ $1 -ne 0 ]; then
        echo " ${2} "
        echo "Restoring the mangle to earlier state"
        restore_mangle
        print_debug_message "************* upgrade is FAILURE . Restoring to previous state succeeded **"
        exit 1
    fi
}


upgrade_mangle_web_container()
{
    NodeIP=$1
    extractDBOptionsAndClusterOptions
    create_mangle_backup
    echo "Delete the Mangle container"
    delete_container ${MANGLE_CONTAINER_NAME}
    echo "Create new mangle container"
    WEB_IMAGE="${MANGLE_DOCKER_ARTIFACTORY}:${MANGLE_BUILD_NUMBER}"
    create_mangle_web ${WEB_IMAGE}
    restore_if_failure $? "Creating of mangle container has failed hence, restoring to previous state"
    verify_mangle_health $NodeIP
    restore_if_failure $? " Creating of mangle Web container has failed its health check hence, restoring to previous state"
    echo "Successfully Upgraded mangle node $1"
}

udateSubsequentbootscript()
{
	echo "Updating the subsequentboot script..."
	file=subsequentboot
	if [[ -x "$file" ]]
	then
    	echo "File '$file' exists and has executable permissions."
    	rm -rf /opt/vmware/etc/isv/subsequentboot
		cp subsequentboot /opt/vmware/etc/isv/
		chmod 755 /opt/vmware/etc/isv/subsequentboot
	else
    	echo "File '$file' is not executable or found, please put the file with executable permission."
	fi
}

upgrade_mangle()
{
    echo "Put mangle in maintenance mode"
    changeMangleNodesStatus "MAINTENANCE_MODE"
    take_db_snapshot
    echo "Create backup of containers"
    upgrade_mangle_web_container "$CurrentMangleNodeIP"

    if [ ${#NODE_IPS[@]} -gt 1 ] && [ ! -z "$MANGLE_NODE_SSH_KEY_PATH" ]
    then
        for node_ip in "${NODE_IPS[@]}"; do
            if [ "$node_ip" != "$CurrentMangleNodeIP" ]
            then
                initializeDockerConstants "$node_ip"
                upgrade_mangle_web_container "$node_ip"
            fi
        done
    fi
    udateSubsequentbootscript
}

main(){
    validate_input_prerequisites "$@"
    init
    print_debug_message "********* Starting upgrade of Mangle *************** "
    upgrade_mangle
    print_debug_message " ************ Completed upgrading of Mangle and upgrade was SUCCESSFUL ********* "
}

main "$@"
