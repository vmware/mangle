#!/bin/sh
main(){
errorexitcode=127
readAndParseArgs "$@"
basedir=$(dirname "$0")
if [ "$operation" = "remediate" ]
then
    preRequisitescheck
    remediate
    exit 0
fi

if [ "$operation" = "precheck" ]
then
    preRequisitescheck
    cleanup
    exit 0
fi

if [ "$operation" = "status" ]
then
    status
fi

if [ "$operation" = "inject" ]
then
    preRequisitescheck
    injectFault
    exit 0
fi
}

readAndParseArgs(){
    if [ $# -eq 0 ]
    then
        help
    fi

    for arg in "$@"
    do
        if [ ! -z "$(echo $arg | grep -E "^--")" ]; then
            key=$(echo $arg | cut -d= -f1)
            value=$(echo $arg | cut -d= -f2)
            var=$(echo $key | cut -d- -f3)
            export $var="${value}"
        fi
    done

    if [ -z $operation ]
    then
        help
    else
        if [ $operation != "inject" -a $operation != "precheck" -a $operation != "status" -a $operation != "remediate" ]
        then
            help
        fi
    fi

    if [ $operation = "inject" ]
    then
        if [ -z $processIdentifier ]
        then
            help
        fi
    fi

    if [ $operation = "remediate" ]
    then
        if [ -z "$remediationCommand" ]
        then
            help
        fi
    fi
  }

help(){
    echo "$0 --operation=<precheck | status>"
    echo " or "
    echo "$0 --operation=remediate --remediationCommand=<remediationCommand>"
    echo " or "
    echo "$0 --operation=inject --processIdentifier=<processIdentifier>"
    exit $errorexitcode
}

preRequisitescheck()
{
    isAwkPresent
    running_in_docker
    isPgrepPresent
    isKillPresent
    if [ ! -z "$precheckmessage" ]
    then
        echo "Precheck:Failed $precheckmessage required to proceed with injection"
        cleanup
        exit $errorexitcode
    fi
    echo "Precheck:Success"
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,pgrep"
    fi
}

isKillPresent(){
    kill > /dev/null 2>&1
    killRetVal=$?
    if [ $killRetVal -ne 0 -a $killRetVal -ne 1 -a $killRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,kill"
    fi
}

isAwkPresent(){
   awk > /dev/null 2>&1
   awkRetVal=$?
   if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage,awk"
   fi
}

status(){
    pgrep -f "killprocess.sh" > /dev/null 2>&1
    mainProcessRetVal=$?
    if [ $mainProcessRetVal -eq 0 ]
    then
        echo "Status: Inprogress"
        exit 0
    fi
    pgrep -f "killprocessscript.sh" > /dev/null 2>&1
    childProcessRetVal=$?
    if [ $childProcessRetVal -eq 0 ]
    then
        echo "Status: Inprogress"
    else
        echo "Status: Completed/NotRunning"
    fi
    exit 0
}

remediate(){
    $remediationCommand > /dev/null 2>&1 &
    cleanup
    echo "Remediated: Kill Service injection"
}

cleanup(){
    rm -rf $basedir/killprocess.sh > /dev/null 2>&1
}

running_in_docker() {
  awk -F/ '$2 == "docker"' /proc/self/cgroup | read
  isDockerRetVal=$?
  if [ $isDockerRetVal -eq 0 ]; then
     IS_CONTAINER=true
  fi
}

injectFault(){
    currentProcessId=$(pgrep -f "killprocess")
    allProcessIds=$(pgrep -f $processIdentifier)
    actualProcessIdsToKill=$(echo "$allProcessIds"|grep -v $currentProcessId)
    processCount=$(echo $actualProcessIdsToKill|wc -w)
    if [ $processCount -eq 0 ]
    then
        echo "no process found with identifier $processIdentifier, can't kill service"
        cleanup
        exit $errorexitcode
    fi

    if [ $processCount -gt 1 ]
    then
        echo "Found more than one process ("$(echo $actualProcessIdsToKill|xargs)") with same process identifier: $processIdentifier"
        cleanup
        exit $errorexitcode
    fi

   if [ ! -z "$IS_CONTAINER" ]; then
    cat << EOF > $basedir/killprocessscript.sh
    #!/bin/sh
    #sleeping the process so that if pid is the entry point process container may exit and task will fail
    echo "killing processID: $actualProcessIdsToKill"
    sleep 5s
    kill -9 $actualProcessIdsToKill
    rm -rf $basedir/killprocessscript.sh $basedir/killProcessFault.log > /dev/null 2>&1
EOF
    chmod 777 $basedir/killprocessscript.sh
    $basedir/killprocessscript.sh > $basedir/killProcessFault.log 2>&1 &
    echo "Triggered: Kill Process injection"
   else
    sleep 5s
    output=$(kill -9 $actualProcessIdsToKill)
    killProcessExitVal=$?
    if [ $killProcessExitVal -eq 0 ]
    then
       echo "killing processID: $actualProcessIdsToKill"
       echo "Triggered: Kill Process injection"
    else
       echo $output
       exit $errorexitcode
    fi
   fi
}

#calling main function
main "$@"