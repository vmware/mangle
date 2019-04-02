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
    if [ ! -z "$precheckmessage" ]
    then
        echo "Precheck Failed: $precheckmessage required to proceed with injection"
        cleanup
        exit $errorExitCode
    fi
    echo "Precheck is successful"
}

isAwkPresent(){
    awk > /dev/null 2>&1
    awkRetVal=$?
    if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
        precheckmessage="awk"
    fi
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,pgrep"
    fi
}

isExprPresent(){
    expr > /dev/null 2>&1
    exprRetVal=$?
    if [ $exprRetVal -ne 0 -a $exprRetVal -ne 1 -a $exprRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,expr"
    fi
}

status(){
    pgrep -f "cpuspikescript.sh" > /dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]
    then
        echo "Status: Inprogress"
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}

remediate(){
    parentProcessIDs=$(pgrep -f "cpuspikescript.sh")
    if [ ! -z "$parentProcessIDs" ]
    then
        #Pausing the parent process
        pgrep -f "cpuspikescript.sh" |xargs -L 1 kill -STOP
        #Killing all the child processes created by parent process
        pgrep -f "cpuspikescript.sh" |xargs -L 1 pgrep -P |xargs kill -9 > /dev/null 2>&1
        #Killing the parent process
        kill -9 $parentProcessIDs > /dev/null 2>&1
    fi
    cleanup
    echo "Remediated: Cpu injection"
}

cleanup(){
    rm -rf $basedirectory/cpuspikescript.sh  > /dev/null 2>&1
    rm -rf $basedirectory/command.sh  > /dev/null 2>&1
    rm -rf $basedirectory/$0  > /dev/null 2>&1
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