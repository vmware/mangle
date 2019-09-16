 #!/bin/sh

main(){

errorExitCode=127
readAndParseArgs $@
basedirectory=$(dirname "$0")
if [ "$operation" = "remediate" ]
then
    preRequisitescheck
    remediate
    exit 0
fi

if [ "$operation" = "precheck" ]
then
    preRequisitescheck
    exit 0
fi

if [ "$operation" = "status" ]
then
    status
    exit 0
fi

preRequisitescheck
status
injectFault
}

readAndParseArgs(){
    if [ $# -eq 0 ]
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
    if [ -z $operation ]
    then
        help
    else
        if [ $operation != "inject" -a $operation != "precheck" -a $operation != "status" -a $operation != "remediate" ]
        then
            help
        fi
    fi

    if [ "$operation" = "inject" ]
    then
        if [ -z $load ] || [ -z $timeout ]
        then
            help
        fi
    fi
}

validateInputs(){
    if [ -z $(echo $load|grep -E "^[0-9]+$") ]
    then
        validationMessage="load value should be an integer\n"
    else
        if [ $load -lt 0 -o $load -gt 100 ]
        then
            validationMessage="load value should be between 1 - 100\n"
        fi
    fi

    if [ -z $(echo $timeout|grep -E "^[0-9]+$") ]
    then
        validationMessage="${validationMessage}timeout value should be an integer\n"
    fi

    if [ ! -z "$validationMessage" ]
    then
        printf "$validationMessage"
        cleanup
        exit $errorExitCode
    fi
}

help(){
    echo "$0 --operation=<remediate | precheck | status>"
    echo " or "
    echo "$0 --operation=inject --load=<load> --timeout=<timeoutinMilliseconds>"
    exit $errorExitCode
}

preRequisitescheck()
{
    isAwkPresent
    isPgrepPresent
    isExprPresent
    checkWritePermissionOfInjectionDir
    if [ ! -z "$precheckmessage" ]
    then
        precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
        length=$(echo $precheckmessage |wc -c)
        echo ${precheckmessage} | cut -c 1-$(($length - 2))
        cleanup
        exit $errorExitCode
    fi
    echo "Precheck is successful"
}

checkWritePermissionOfInjectionDir()
{
    if [ ! -w "$basedirectory" ]
    then
      precheckmessage="$precheckmessage Write permission on ${basedirectory} required,"
    fi
}
isAwkPresent(){
    awk > /dev/null 2>&1
    awkRetVal=$?
    if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
        precheckmessage="awk required,"
    fi
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage pgrep required,"
    fi
}

isExprPresent(){
    expr > /dev/null 2>&1
    exprRetVal=$?
    if [ $exprRetVal -ne 0 -a $exprRetVal -ne 1 -a $exprRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage expr required,"
    fi
}

status(){
    pgrep -f "cpuspikescript.sh" > /dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]
    then
        echo "CPU fault is already running.Wait until it completes if you want to inject again."
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}

remediate(){
    parentProcessID=$(pgrep -f "cpuspikescript.sh")
    if [ ! -z "$parentProcessID" ]
    then
        #Pausing the parent process
        pgrep -f "cpuspikescript.sh" |xargs -L 1 kill -STOP
        #Killing all the child processes created by parent process
        pgrep -f "cpuspikescript.sh" |xargs -L 1 pgrep -P |xargs kill -9 > /dev/null 2>&1
        #Killing the parent process
        kill -9 $parentProcessID > /dev/null 2>&1
        cleanup
        exit 0
    else
        echo "CPU Fault already remediated"
        cleanup
        exit $errorExitCode
    fi
    cleanup
    echo "Remediated: Cpu injection"
}

cleanup(){
    rm -rf $basedirectory/cpuspikescript.sh  > /dev/null 2>&1
    rm -rf $basedirectory/command.sh  > /dev/null 2>&1
    rm -rf $basedirectory/cpuburn.sh  > /dev/null 2>&1
    rm -rf $basedirectory/cpuFault_Core* > /dev/null 2>&1
}

injectFault(){
    validateInputs
    echo "Injecting cpu spike"
    commandToStressCpu="yes > /dev/null 2>&1 &"
    if [ $load -ge 100 ]
    then
        cat << EOF > $basedirectory/cpuspikescript.sh
            #!/bin/sh
            $commandToStressCpu
            echo "Injected 100 percentage cpu"
            sleep $(awk "BEGIN {print $timeout/1000}")
            kill -9 \$!
EOF
    else
        cat << EOF > $basedirectory/cpuspikescript.sh
        #!/bin/sh
        startTime=\$((\$(date +%s)))
        currentTime=\$((\$(date +%s)))
        load=\$1
        timelimit=\$2
        timelimit=\$((\$timelimit / 1000))
        check=\`expr \$currentTime - \$timelimit\`
        echo "Injecting "\$load "percentage load for ":\$timelimit
        while [ \$startTime -gt \$check ]
        do
            $commandToStressCpu
            sleeptime=\$(awk "BEGIN { load=\$load/100; print load }")
            sleep \$sleeptime
            kill -9 \$! > /dev/null 2>&1
            sleep \$(awk "BEGIN {print 1-\$sleeptime}")
            currentTime=\$((\$(date +%s)))
            check=\`expr \$currentTime - \$timelimit\`
        done
EOF
    fi
    cat << EOF >> $basedirectory/cpuspikescript.sh
        echo "Autoremedition started"
        rm -rf $basedirectory/command.sh > /dev/null 2>&1
        rm -rf $basedirectory/cpuburn.sh  > /dev/null 2>&1
        rm -rf $basedirectory/cpuspikescript.sh  > /dev/null 2>&1
        rm -rf $basedirectory/cpuFault_Core* > /dev/null 2>&1
EOF
    chmod 777 $basedirectory/cpuspikescript.sh
    x=0
    cores="$(grep -c ^processor /proc/cpuinfo)"
    rm -rf $basedirectory/command.sh
    while [ $x -lt $(( $cores )) ]
    do
        echo "/bin/sh $basedirectory/cpuspikescript.sh $load $timeout >$basedirectory/cpuFault_Core$x.log 2>&1 &" >> $basedirectory/command.sh
        x=$(( $x + 1 ))
    done
    chmod 777 $basedirectory/command.sh
    $basedirectory/command.sh
    echo "Triggered: cpu injection"
    exit 0
}
#calling main function
main $@