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
}

validateInputs(){
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
    echo "$0 --operation=inject --timeout=<timeoutinMilliseconds>"
    exit $errorExitCode
}

preRequisitescheck(){
    isPgrepPresent
    isUlimitPresent
    isOptimizedShell
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
        precheckmessage="$precheckmessage Write permission on ${basedirectory}  required,"
    fi
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="pgrep required,"
    fi
}

isUlimitPresent(){
   ulimit > /dev/null 2>&1
   ulimitRetVal=$?
   if [ $ulimitRetVal -ne 0 -a $ulimitRetVal -ne 1 -a $ulimitRetVal -ne 2 ]; then
      precheckmessage="$precheckmessage ulimit is required,"
   fi
}

isOptimizedShell(){
   25>> $basedirectory/fdstest.txt 2>&1
   optimizedShellRetVal=$?
   if [ $optimizedShellRetVal -ne 0 ]; then
      precheckmessage="$precheckmessage Optimized shell is not supported, Use /bin/bash as default shell."
   fi
}

status(){
    pgrep -f "triggerFileHandlerFault.sh" > /dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]
    then
        echo "Filehandler leak fault is already running.Wait until it completes if you want to inject again."
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}

remediate(){
    parentProcessID=$(pgrep -f "triggerFileHandlerFault.sh")
    if [ ! -z "$parentProcessID" ]
    then
       echo "Remediating filehandlerleak fault"
       pgrep -f "triggerFileHandlerFault.sh" | xargs kill -9
       pgrep -f "createFD.sh" | xargs kill -9
       pgrep -f "sleep" | xargs kill -9
       #Killing the parent process
       kill -9 $parentProcessID > /dev/null 2>&1
       cleanup
       exit 0
    else
       echo "Filehandler leak Fault already remediated"
       cleanup
       exit $errorExitCode
    fi
    cleanup
    echo "Remediated: Filehandler leak fault injection"
}

cleanup(){
    rm -rf $basedirectory/createFD.sh  > /dev/null 2>&1
    rm -rf $basedirectory/triggerFileHandlerFault.sh  > /dev/null 2>&1
    rm -rf $basedirectory/fdstest*.txt > /dev/null 2>&1
    rm -rf $basedirectory/fileHandlerFault.log  > /dev/null 2>&1
    rm -rf $basedirectory/filehandlerleak.sh  > /dev/null 2>&1
}

injectFault(){
  validateInputs
  echo "Running filehandlerleak script"
  cat << EOF > $basedirectory/createFD.sh
  #!/bin/sh
  x=25
  availableLimit=\$(ulimit -n)
  while [ \$x -lt \$availableLimit ]
  do
    eval 'exec '"\$x"'>> '"\$3"/fdstest"\$1".txt''
    x=\$((\$x + 1 ))
  done
  echo "script executed"
  sleep \$2
EOF

  chmod 777 $basedirectory/createFD.sh

  echo "Injecting Filehandler leak fault"
  cat << EOF > $basedirectory/triggerFileHandlerFault.sh
  timeout=\$1
  x=25
  startTime=\$((\$(date +%s)))
  currentTime=\$((\$(date +%s)))
  echo \$currentTime
  echo \$timeout
  timelimit=\$((\$timeout / 1000))
  check=\`expr \$currentTime - \$timelimit\`
  while [ \$startTime -gt \$check ]
  do
     echo \$x
     /bin/sh $basedirectory/createFD.sh \$x \$timelimit $basedirectory &
     x=\$((\$x + 1 ))
     currentTime=\$((\$(date +%s)))
     check=\`expr \$currentTime - \$timelimit\`
  done
  pgrep -f "createFD.sh" | xargs kill -9
  rm -rf $basedirectory/createFD.sh  > /dev/null 2>&1
  rm -rf $basedirectory/triggerFileHandlerFault.sh  > /dev/null 2>&1
  rm -rf $basedirectory/fileHandlerFault.log  > /dev/null 2>&1
  rm -rf $basedirectory/fdstest*.txt > /dev/null 2>&1
  rm -rf $basedirectory/filehandlerleak.sh  > /dev/null 2>&1
EOF
  chmod 777 $basedirectory/triggerFileHandlerFault.sh
  sh $basedirectory/triggerFileHandlerFault.sh  $timeout > $basedirectory/fileHandlerFault.log 2>&1 &
  echo "Triggered File handler leak fault"
  exit 0
}

#calling main function
main $@
