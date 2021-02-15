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
    isSudoPresent
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
        if [ -z $processIdentifier ] && [ -z $processId ]
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
    echo "$0 --operation=inject --processIdentifier=<processDescriptor> --killAll=<killAll> --processId=<processId>"
    exit $errorexitcode
}

preRequisitescheck()
{
    isAwkPresent
    running_in_docker
    isPgrepPresent
    isKillPresent
    checkWritePermissionOfInjectionDir
    if [ ! -z "$precheckmessage" ]
    then
        precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
        length=$(echo $precheckmessage |wc -c)
        echo ${precheckmessage} | cut -c 1-$(($length - 2))
        cleanup
        exit $errorexitcode
    fi
    echo "Precheck:Success"
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage pgrep is required,"
    fi
}

isKillPresent(){
    kill > /dev/null 2>&1
    killRetVal=$?
    if [ $killRetVal -ne 0 -a $killRetVal -ne 1 -a $killRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage kill is required,"
    fi
}

isAwkPresent(){
   awk > /dev/null 2>&1
   awkRetVal=$?
   if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage awk is required,"
   fi
}

checkWritePermissionOfInjectionDir()
{
    if [ ! -w "$basedir" ]
    then
        precheckmessage="$precheckmessage Write permission on ${basedir} is required,"
    fi
}

isSudoPresent()
{
    sudo -nv > /dev/null 2>&1
    sudoRetVal=$?
    if [ $sudoRetVal -eq 0 ]; then
        sudoCommand="sudo"
    fi
}

#Function to check whether euid is null or not, If it is null then assigned it to 1 (other than 0)
euidCheck(){
    euid=$EUID
    echo "EUID value from euidCheck function: $euid"
    if [ -z "$euid" ]
    then
        euid=1
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
    rm -rf $basedir/killprocess.sh $basedir/killprocess.log > /dev/null 2>&1
}

running_in_docker() {
  awk -F/ '$2 == "docker"' /proc/self/cgroup | read continue
  isDockerRetVal=$?
  if [ $isDockerRetVal -eq 0 ]; then
     IS_CONTAINER=true
  fi
}

injectFault(){
    if [ ! -z $processId ]
    then
        actualProcessIdsToKill=$processId
    else
        currentProcessId=$(pgrep -f "killprocess")
        euidCheck
        if [ $euid -eq 0 -o $sudoCommand = "sudo" ]
            then
                allProcessIds=$(pgrep -f $processIdentifier)
            else
                allProcessIds=$(pgrep -u `whoami` -f $processIdentifier)
        fi
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
            if [ $killAll = "false" ]
            then
                echo "Found more than one process with same process identifier: $processIdentifier ("$(echo $actualProcessIdsToKill|xargs)"). Please set killAll=true inorder to kill all of them"
                cleanup
                exit $errorexitcode
            else
                echo "Found more than one process ("$(echo $actualProcessIdsToKill|xargs)") killing all of them" >> $basedir/killprocess.log
            fi
        fi
    fi

    if [ ! -z "$IS_CONTAINER" ]; then
    echo '#!/bin/sh
    #sleeping the process so that if pid is the entry point process container may exit and task will fail
    echo "Process ID to kill: ($1)" >> $basedir/killprocess.log
    sleep 5s
    for process in $1; do
        killOutput=$($sudoCommand kill -9 $process >&1 2>&1)
        retCode=$?
        if [ $retCode -eq 0 ]
        then
            echo "Killed processID: $process" >> $basedir/killprocess.log
        fi
        if [ $retCode -eq 1 ]
        then
            if [ ! -z $(echo $killOutput | grep "No such process") && [ -z $2 ] ]
            then
                echo "Process already killed." >> $basedir/killprocess.log
            else
                echo $killOutput
                exit $errorexitcode
            fi
        fi
        if [ $retCode -gt 1 ]
        then
            echo $killOutput
            exit $errorexitcode
        fi
    done
    rm -rf $basedir/killprocessscript.sh $basedir/killProcessFault.log > /dev/null 2>&1' > $basedir/killprocessscript.sh
    chmod 777 $basedir/killprocessscript.sh
    $basedir/killprocessscript.sh "$actualProcessIdsToKill" "$processId" > $basedir/killProcessFault.log 2>&1 &
    else
    sleep 5s
    for process in $actualProcessIdsToKill; do
        killOutput=$($sudoCommand kill -9 $process >&1 2>&1)
        retCode=$?
        if [ $retCode -eq 0 ]
        then
            echo "Killed processID: $process" >> $basedir/killprocess.log
        fi
        if [ $retCode -eq 1 ]
        then
            if [ ! -z $(echo $killOutput | grep "No such process") && [ -z $processId ] ]
            then
                echo "Process already killed." >> $basedir/killprocess.log
            else
                echo $killOutput
                exit $errorexitcode
            fi
        fi
        if [ $retCode -gt 1 ]
        then
            echo $killOutput
            exit $errorexitcode
        fi
    done
    fi
}

#calling main function
main "$@"
