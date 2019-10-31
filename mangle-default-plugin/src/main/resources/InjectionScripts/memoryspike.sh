#!/bin/sh

main(){
CONTAINER_MEM_LIMIT_FILE="/sys/fs/cgroup/memory/memory.limit_in_bytes"
CONTAINER_MEM_USAGE_FILE="/sys/fs/cgroup/memory/memory.usage_in_bytes"
MEM_PROC_FILE="/proc/meminfo"
errorExitCode=127
readAndParseArgs $@
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
   echo "Precheck:Success"
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
    if [ $# -eq 0 -o $# -gt 3 ]
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
        printf "Validation Failed:$validationMessage"
        cleanup
        exit $errorExitCode
    fi
}

help(){
   echo "$0 --operation=<remediate | precheck | status >"
   echo " or "
   echo "$0 --operation=inject --load=<load> --timeout=<timeoutinMilliseconds>"
   exit $errorExitCode
}

status(){
   pgrep -f "trigger_memory_spike.sh" > /dev/null 2>&1
   statusCheckRetVal=$?
   if [ $statusCheckRetVal -eq 0 ]
   then
      echo "Memory fault is already running.Wait until it completes if you want to inject again."
      exit $errorExitCode
   else
      echo "Status: Completed/NotRunning"
   fi
}

preRequisitescheck()
{
   isAwkPresent
   isPgrepPresent
   isExprPresent
   isFreePresent
   isEvalPresent
   running_in_docker
   checkWritePermissionOfInjectionDir
   if [ ! -z "$precheckmessage" ]
   then
        precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
        length=$(echo $precheckmessage |wc -c)
        echo ${precheckmessage} | cut -c 1-$(($length - 2))
      cleanup
      exit $errorExitCode
   fi
}

checkWritePermissionOfInjectionDir()
{
    if [ ! -w "$basedir" ]
    then
        precheckmessage="$precheckmessage Write permission on ${basedir} is required,"
    fi
}

running_in_docker() {
  awk -F/ '$2 == "docker"' /proc/self/cgroup > /dev/null 2>&1
  isDockerRetVal=$?
  if [ $isDockerRetVal -eq 0 ]; then
     IS_CONTAINER=true
  fi
}

isAwkPresent(){
   awk > /dev/null 2>&1
   awkRetVal=$?
   if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage awk is required,"
   fi
}

isPgrepPresent(){
   pgrep  > /dev/null 2>&1
   pgrepRetVal=$?
   if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage pgrep is required,"
   fi
}

isExprPresent(){
   expr > /dev/null 2>&1
   exprRetVal=$?
   if [ $exprRetVal -ne 0 -a $exprRetVal -ne 1 -a $exprRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage,expr"
   fi
}

isFreePresent(){
   free > /dev/null 2>&1
   freeRetVal=$?
   if [ $freeRetVal -ne 0 -a $freeRetVal -ne 1 -a $freeRetVal -ne 2 ]; then
	if [ ! -z "$IS_CONTAINER" ]; then
           if [ ! -f $CONTAINER_MEM_LIMIT_FILE -o ! -f $CONTAINER_MEM_USAGE_FILE ]; then
              precheckmessage="${precheckmessage}, ${CONTAINER_MEM_LIMIT_FILE} or ${CONTAINER_MEM_USAGE_FILE} is not present"
           fi
        else
           precheckmessage="$precheckmessage free is required,"
        fi
   fi
}

isEvalPresent(){
   eval > /dev/null 2>&1
   evalRetVal=$?
   if [ $evalRetVal -ne 0 -a $evalRetVal -ne 1 -a $evalRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage eval is required,"
   fi
}

remediate(){
   processId=$(pgrep -f "trigger_memory_spike")
   if [ ! -z $processId ]; then
    pgrep -f "trigger_memory_spike"|xargs -L 1 pgrep -P|xargs kill -9 > /dev/null 2>&1
    sleep 1
    cleanup
    echo "Remediated: Memory spike fault"
    exit 0
   else
    echo "Memory Fault already remediated"
    cleanup
    exit $errorExitCode
   fi
}

cleanup(){
    rm -rf $basedir/spikemem.sh
    rm -rf $basedir/trigger_memory_spike.sh  > /dev/null 2>&1
    rm -rf $basedir/memoryspike.sh  > /dev/null 2>&1
    rm -rf $basedir/memoryFault.log  > /dev/null 2>&1
}

calculateMemoryParameters(){
    if [ ! -z "$IS_CONTAINER" ]; then
	    systemMemoryLimit=$(cat /proc/meminfo  | grep "MemTotal" |tr -s '' ' ' |cut -d ' ' -f2)
        systemMemoryLimitInBytes=$(printf '%s' $(($systemMemoryLimit*1024)))
        containerMemoryLimit=$(cat $CONTAINER_MEM_LIMIT_FILE |head -1)
        memoryLimitInTarget=$containerMemoryLimit
        if [ $containerMemoryLimit -gt $systemMemoryLimitInBytes ]; then
            memoryLimitInTarget=$systemMemoryLimitInBytes
        fi
        initialMemoryUsedInTarget=$(cat $CONTAINER_MEM_USAGE_FILE |head -1)
    else
	    memoryLimitInTarget=$(free -b | awk 'FNR == 2 {print $2'})
        initialMemoryUsedInTarget=$( free -b | awk 'FNR == 2 {print $3'})
    fi
    timeout=$(($timeout / 1000))
    echo "memoryLimitInTarget:"$memoryLimitInTarget >> $basedir/memoryFault.log
    echo "initialMemoryUsedInTarget:"$initialMemoryUsedInTarget >> $basedir/memoryFault.log
    requestedMemoryToFillInBytes=$(printf '%s' $((($load*$memoryLimitInTarget)/100)))
    requestedMemoryToFillInBytes=$(printf '%s' $(($requestedMemoryToFillInBytes-$initialMemoryUsedInTarget)))
    echo "requestedMemoryToFillInBytes:"$requestedMemoryToFillInBytes >> $basedir/memoryFault.log
    if [ $requestedMemoryToFillInBytes -lt 0 ]; then
        echo "Current memory usage in target is greater than the requested memory to fill"
        cleanup
        exit $errorExitCode
    fi
    echo "Begin allocating memory..."
}

injectFault(){
   perl -v > /dev/null 2>&1
   perlRetVal=$?
   if [ $perlRetVal -ne 0 -a $perlRetVal -ne 1 -a $perlRetVal -ne 2 ]; then
      injectFaultWithEval
   else
      injectFaultWithPerl
   fi
}

injectFaultWithPerl(){
    validateInputs
    calculateMemoryParameters
    echo "Injecting "$load "percentage load for ":$timeout" sec" >> $basedir/memoryFault.log
    echo "#!/bin/sh" > $basedir/trigger_memory_spike.sh
    memoryChunk=$(perl -E "say 0.1/100*$requestedMemoryToFillInBytes/2")
    memoryChunk=$(awk "BEGIN { i=int($memoryChunk); print ($memoryChunk-i<0.5)?i:i+1 }")
    echo "memorychunk :"$memoryChunk >> $basedir/memoryFault.log
    i=1
    while [ $i -le 1000 ]; do
        echo "perl -e '\$m = \"x\" x $memoryChunk; sleep $timeout' &" >> $basedir/trigger_memory_spike.sh
        i=$(( $i + 1 ))
    done
    echo "sleep $timeout" >> $basedir/trigger_memory_spike.sh
    echo "echo "autoremediation started"" >> $basedir/trigger_memory_spike.sh
    echo "rm -rf $basedir/trigger_memory_spike.sh" >> $basedir/trigger_memory_spike.sh
    echo "rm -rf $basedir/memoryspike.sh" >> $basedir/trigger_memory_spike.sh
    echo "rm -rf $basedir/memoryFault.log" >> $basedir/trigger_memory_spike.sh
    chmod 777 $basedir/trigger_memory_spike.sh
    /bin/sh $basedir/trigger_memory_spike.sh >> $basedir/memoryFault.log 2>&1 &
    echo "Triggered: memory injection"
    exit 0
}

injectFaultWithEval(){
    validateInputs
    calculateMemoryParameters
    echo "Injecting "$load "percentage load for ":$timeout" sec" >> $basedir/memoryFault.log
    echo $requestedMemoryToFillInBytes
    cat << EOF > $basedir/trigger_memory_spike.sh
    #!/bin/sh
    startTime=\$((\$(date +%s)))
    currentTime=\$((\$(date +%s)))
    check=\`expr \$currentTime - \$timeout\`
    requestedMemoryToFill=\$(printf '%s' \$(($requestedMemoryToFillInBytes*1024)))
    echo "requestedMemoryToFill:"\$requestedMemoryToFill >> $basedir/memoryFault.log
    echo "Begin allocating memory..." >> $basedir/memoryFault.log

    scriptName=spikemem.sh
    echo "#!/bin/sh" > $basedir/\$scriptName
    echo "value=\"\\\$(seq -w -s '' 0 10)\"" >> $basedir/\$scriptName
    echo "eval array0=\\\$value" >> $basedir/\$scriptName
    echo "sleep \$timeout" >> $basedir/\$scriptName
    chmod 777 $basedir/\$scriptName

    while [ \$startTime -gt \$check ]
    do
        currentMemoryUsed=\$( free -m | awk 'FNR == 2 {print \$3'})
        echo "currentMemoryUsed:"\$currentMemoryUsed >> $basedir/memoryFault.log
        if [ \$currentMemoryUsed -lt \$requestedMemoryToFill ]
        then
            $basedir/\$scriptName &
        fi
        currentTime=\$((\$(date +%s)))
        check=\`expr \$currentTime - \$timeout\`
    done
    rm -rf $basedir/spikemem.sh
    rm -rf $basedir/trigger_memory_spike.sh
    rm -rf $basedir/memoryspike.sh
    rm -rf $basedir/memoryFault.log
EOF

    chmod 777 $basedir/trigger_memory_spike.sh
    /bin/sh $basedir/trigger_memory_spike.sh $load $timeout >> $basedir/memoryFault.log 2>&1 &
    echo "Triggered: memory injection"
    exit 0
}

main $@