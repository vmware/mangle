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
      echo "Status: Inprogress"
      cleanup
      exit $errorExitCode
   else
      echo "Status: Completed/NotRunning"
   fi
}

preRequisitescheck()
{
   isPerlPresent
   isAwkPresent
   isPgrepPresent
   isFreePresent
   isEvalPresent
   if [ ! -z "$precheckmessage" ]
   then
      echo "Precheck:Failed $precheckmessage required to proceed with injection"
      exit $errorExitCode
   fi
}

running_in_docker() {
  awk -F/ '$2 == "docker"' /proc/self/cgroup | read
  isDockerRetVal=$?
  if [ $perlRetVal -ne 0 ]; then
     IS_CONTAINER=true
  fi
}

isPerlPresent(){
   perl -v > /dev/null 2>&1
   perlRetVal=$?
   if [ $perlRetVal -ne 0 -a $perlRetVal -ne 1 -a $perlRetVal -ne 2 ]; then
      precheckmessage="perl"
   fi
}

isAwkPresent(){
   awk > /dev/null 2>&1 
   awkRetVal=$?
   if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage,awk"
   fi
}

isPgrepPresent(){
   pgrep  > /dev/null 2>&1
   pgrepRetVal=$?
   if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage,pgrep"
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
           precheckmessage="$precheckmessage,free"
        fi
   fi
}

isEvalPresent(){
   eval > /dev/null 2>&1
   evalRetVal=$?
   if [ $evalRetVal -ne 0 -a $evalRetVal -ne 1 -a $evalRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage,eval"
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
    echo "Fault already remediated"
    exit $errorExitCode
   fi
}

cleanup(){
    rm -rf $basedir/trigger_memory_spike.sh  > /dev/null 2>&1
    rm -rf $basedir/$0  > /dev/null 2>&1
}

calculateMemoryParameters(){
   if [ ! -z "$IS_CONTAINER" ]; then
      systemMemoryLimit=$(cat /proc/meminfo  | grep "MemTotal" |tr -s '' ' ' |cut -d ' ' -f2)
	   containerMemoryLimit=$(cat $CONTAINER_MEM_LIMIT_FILE |head -1)
      memoryLimitInTarget=$containerMemoryLimit
      if [ $containerMemoryLimit -gt $systemMemoryLimit ]; then
         memoryLimitInTarget=$systemMemoryLimit
      fi
      initialMemoryUsedInTarget=$(cat $CONTAINER_MEM_USAGE_FILE |head -1)
   else
	   memoryLimitInTarget=$(free -b | awk 'FNR == 2 {print $2'})
      initialMemoryUsedInTarget=$( free -b | awk 'FNR == 2 {print $3'}) 
   fi
   timeout=$(($timeout / 1000))
   echo "memoryLimitInTarget:"$memoryLimitInTarget
   echo "initialMemoryUsedInTarget:"$initialMemoryUsedInTarget
   #requestedMemoryToFillInBytes=$(awk "BEGIN { pc=$load/100*$memoryLimitInTarget; i=int(pc); print (pc-i<0.5)?i:i+1 }")
   requestedMemoryToFillInBytes=$(perl -E "say $load/100*$memoryLimitInTarget")
   requestedMemoryToFillInBytes=$(awk "BEGIN { i=int($requestedMemoryToFillInBytes); print ($requestedMemoryToFillInBytes-i<0.5)?i:i+1 }")
   #requestedMemoryToFillInBytes=$(( $requestedMemoryToFillInBytes - $initialMemoryUsedInTarget ))
   requestedMemoryToFillInBytes=$(perl -E "say $requestedMemoryToFillInBytes-$initialMemoryUsedInTarget")
   echo "requestedMemoryToFillInBytes:"$requestedMemoryToFillInBytes
   if [ $requestedMemoryToFillInBytes -lt 0 ]; then
      echo "Current memory usage in target is greater than the requested memory to fill"
      exit $errorExitCode
   fi
   echo "Begin allocating memory..."
}
injectFault(){
      validateInputs
      calculateMemoryParameters
      echo "#!/bin/sh" > $basedir/trigger_memory_spike.sh
      #memoryChunk=$(awk "BEGIN { pc=0.1/100*$requestedMemoryToFillInBytes; i=int(pc); print (pc-i<0.5)?i:i+1 }")
      memoryChunk=$(perl -E "say 0.1/100*$requestedMemoryToFillInBytes")
      memoryChunk=$(awk "BEGIN { i=int($memoryChunk); print ($memoryChunk-i<0.5)?i:i+1 }")
      echo $memoryChunk
      i=1
      while [ $i -le 1000 ]; do
         echo "perl -e \"x x $memoryChunk; sleep $timeout\" &" >> $basedir/trigger_memory_spike.sh
         i=$(( $i + 1 ))
      done
      echo "sleep $timeout" >> $basedir/trigger_memory_spike.sh
      echo "rm -rf $basedir/trigger_memory_spike.sh" >> $basedir/trigger_memory_spike.sh
      echo "rm -rf $basedir/memoryspike.sh" >> $basedir/trigger_memory_spike.sh
      chmod 777 $basedir/trigger_memory_spike.sh
      /bin/sh $basedir/trigger_memory_spike.sh > memoryFault.log 2>&1 &
      echo "Triggered: memory injection"
      exit 0
}

main $@