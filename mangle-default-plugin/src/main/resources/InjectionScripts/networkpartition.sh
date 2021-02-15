#!/bin/sh

main() {

    errorExitCode=127
    readAndParseArgs $@
    basedirectory=$(dirname "$0")
    if [ "$operation" = "remediate" ]; then
        remediate
        exit 0
    fi

    if [ "$operation" = "precheck" ]; then
      preRequisitescheck
      exit 0
    fi

    if [ "$operation" = "status" ]; then
        status
        exit 0
    fi

    preRequisitescheck
    status
    injectFault
}

readAndParseArgs() {
    if [ $# -eq 0 ]; then
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
    if [ -z $operation ]; then
        help
    else
        if [ $operation != "inject" -a $operation != "precheck" -a $operation != "status" -a $operation != "remediate" ]; then
            help
        fi
    fi

    if [ "$operation" = "inject" ]; then
        if [ -z $hosts ] || [ -z $timeout ]; then
            help
        fi
    fi
    if [ "$operation" = "remediate" ]; then
        if [ -z $hosts ]; then
            help
        fi
    fi
}

help() {
    echo "$0 --operation=<precheck | status>"
    echo " or "
    echo "$0 --operation=remediate --hosts=<list of host with comma separated>"
    echo " or "
    echo "$0 --operation=inject --hosts=<list of host with comma separated> --timeout=<timeoutinMilliseconds>"
    exit $errorexitcode
}

preRequisitescheck() {
    isPgrepPresent
    isRoutePresent
    isReachableHost
    if [ ! -z "$precheckmessage" ]; then
        precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
        length=$(echo $precheckmessage |wc -c)
        echo ${precheckmessage} | cut -c 1-$(($length - 2))
        cleanup
        exit $errorExitCode
    fi
    echo "Precheck is successful"
}

isPgrepPresent() {
  pgrep >/dev/null 2>&1
  pgrepRetVal=$?
  if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
    precheckmessage="pgrep required,"
  fi
}

isRoutePresent() {
  route >/dev/null 2>&1
  routeRetVal=$?
  if [ $routeRetVal -ne 0 -a $routeRetVal -ne 1 -a $routeRetVal -ne 2 ]; then
    precheckmessage="$precheckmessage route required,"
  fi
}

isReachableHost() {
  if [ ! -z $hosts ]; then
     pingMsg="Host communication failed :"
     for ip in $(echo $hosts | tr "," "\n")
     do
         ping -q -c 1 -W 1 $ip >/dev/null 2>&1
         pingRetVal=$?
         if [ $pingRetVal -ne 0 ]; then
             listOfIPs="$listOfIPs $ip,"
         fi
     done
     if [ ! -z "$listOfIPs" ]; then
         precheckmessage="$precheckmessage $pingMsg $listOfIPs"
     fi
  fi
}

status() {
    pgrep -f "networkpartitionscript.sh" >/dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]; then
        echo "Network partition fault is already running.Wait until it completes if you want to inject again."
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}

remediate() {
  parentProcessID=$(pgrep -f "networkpartitionscript.sh")
  if [ ! -z "$parentProcessID" ]; then
    #Pausing the parent process
    pgrep -f "networkpartitionscript.sh" | xargs -L 1 kill -STOP
    for node in $(echo $hosts | tr "," "\n")
    do
      route delete -host $node reject
    done
    #Killing all the child processes created by parent process
    pgrep -f "networkpartitionscript.sh" | xargs -L 1 pgrep -P | xargs kill -9 > /dev/null 2>&1
    #Killing the parent process
    kill -9 $parentProcessID >/dev/null 2>&1
  else
    echo "Network partition Fault already remediated"
    cleanup
    exit $errorExitCode
  fi
  cleanup
  echo "Remediated: Network partition injection"
  exit 0
}

cleanup() {
    rm -rf $basedirectory/networkpartitionscript.sh >/dev/null 2>&1
    rm -rf $basedirectory/networkpartition.sh >/dev/null 2>&1
    rm -rf $basedirectory/networkPartitionFault.log >/dev/null 2>&1
}

injectFault() {
    cat <<EOF >$basedirectory/networkpartitionscript.sh
    #!/bin/sh
    hosts=\$1
    timelimit=\$2
    timelimit=\$((\$timelimit / 1000))
    injectionTimeMsg="Fault Injection Time"
    remediationTimeMsg="Fault Remediation Time"

    performNetworkPartition() {
       nodes=\$1
       routeOperation=\$2
       for node in \$(echo \$nodes | tr "," "\n")
       do
         route \$routeOperation -host \$node reject
         echo "Route \$routeOperation on host: \$node"
       done
    }

    echo "Injecting Network partition fault"
    performNetworkPartition \$hosts "add"
    echo "\$injectionTimeMsg : \$(date)"
    sleep \$timelimit
    echo "Autoremedition started"
    echo "\$remediationTimeMsg : \$(date)"
    performNetworkPartition \$hosts "delete"
    rm -rf $basedirectory/networkpartitionscript.sh >/dev/null 2>&1
    rm -rf $basedirectory/networkpartition.sh >/dev/null 2>&1
    rm -rf $basedirectory/networkPartitionFault.log >/dev/null 2>&1
    exit 0
EOF
    chmod 777 $basedirectory/networkpartitionscript.sh
    sh $basedirectory/networkpartitionscript.sh $hosts $timeout >$basedirectory/networkPartitionFault.log 2>&1 &
    echo "Triggered Network partition fault"
    exit 0
}

#calling main function
main $@
