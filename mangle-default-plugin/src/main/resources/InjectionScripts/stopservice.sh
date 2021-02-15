#!/bin/sh
main(){
errorexitcode=127
readAndParseArgs "$@"
basedir=$(dirname "$0")
if [ "$operation" = "remediate" ]
then
    isSudoPresent
    getCommands
    remediate
    exit 0
fi

if [ "$operation" = "precheck" ]
then
    preRequisitescheck
   cleanup
fi

if [ "$operation" = "status" ]
then
    status
fi

if [ "$operation" = "inject" ]
then
    preRequisitescheck
    injectFault
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
        if [ -z $serviceName ] || [ -z $timeout ];
        then
            help
        fi
    fi

    if [ $operation = "remediate" ]
    then
        if [ -z $serviceName ]
        then
            help
        fi
    fi
  }

help(){
    echo "$0 --operation=<precheck | status>"
    echo " or "
    echo "$0 --operation=remediate --serviceName=<serviceName>"
    echo " or "
    echo "$0 --operation=inject --serviceName=<serviceName> --timeout=<timeoutInMilliseconds>"
    exit $errorexitcode
}

preRequisitescheck(){
   checkWritePermissionOfInjectionDir
   isSudoPresent
   getCommands
   isServicePresent
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


#Set injection, status and remediate command based on the OS
getCommands(){
	if [ -d /lib/systemd ]; then
     STOP_CMD="$sudoCommand systemctl stop $serviceName"
     REMEDIATE_CMD="$sudoCommand systemctl start $serviceName"
     STATUS_CMD="$sudoCommand systemctl status $serviceName"

	elif [ -d /etc/init.d ]; then
     STOP_CMD="$sudoCommand service $serviceName stop"
     REMEDIATE_CMD="$sudoCommand service $serviceName start"
     STATUS_CMD="$sudoCommand service $serviceName status"
   else
	    echo "Mangle doesn't support Stop service fault." >> $basedir/stopservice.log
	    exit $errorExitCode
	fi
	echo "Command to stop the service is: $STOP_CMD, and command to remediate the fault is: $REMEDIATE_CMD" >> $basedir/stopservice.log
}

isServicePresent(){
  $STATUS_CMD  >> $basedir/stopservice.log
  statusCheckReturnVal=$?
  if [ $statusCheckReturnVal -eq 0 ]
  then
     echo "Provided Service: $serviceName is Present and Active"  >> $basedir/stopservice.log
  else
     echo "Provided service: $serviceName is not present in the system or InActive"  >> $basedir/stopservice.log
     cleanup
     exit $statusCheckReturnVal
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

status(){
   $STATUS_CMD  >/dev/null 2>&1
   statusCheckReturnVal=$?
   if [ $statusCheckReturnVal -eq 0 ]
   then
      echo "Status: Completed/NotRunning"  >> $basedir/stopservice.log
   else
      echo "Status: In-progress or the provided service is not present on the system" >> $basedir/stopservice.log
   fi
}

#Method will remediate the fault by restarting the service and will cleanup at the end of execution.
remediate(){
   $REMEDIATE_CMD
   remediateFaultReturnVal=$?
   validateOutput "$remediateFaultReturnVal" "$REMEDIATE_CMD"
   echo "Service is restarted, fault is remediated" >> $basedir/stopservice.log
   cleanup
   exit 0
}

#Method will validate the ExitCode of a command
validateOutput(){
   if [ $1 -eq 0 ]
   then
      echo "Command: $2 executed successfully" >> $basedir/stopservice.log
   else
      echo "Command: $2 execution failed" >> $basedir/stopservice.log
      exit $errorExitCode
   fi
}

cleanup(){
	rm -rf $basedir/stopservice.sh  > /dev/null 2>&1
	rm -rf $basedir/stopservice.log  > /dev/null 2>&1
}

#Method will stop the service, sleep till timeout and then remediate the fault
injectFault(){
  timeout=$(($timeout / 1000))
  cat << EOF > $basedir/trigger_stopservice.sh
  #!/bin/sh

  echo "Injecting fault" >> $basedir/stopservice.log
  echo "Stopping given service" >> $basedir/stopservice.log
  $STOP_CMD
  if [ \$? -ne 0 ];then
    echo "stop service command execution failed" >> $basedir/stopservice.log
    exit $errorExitCode
  fi

  echo "sleeping for $timeout seconds" >> $basedir/stopservice.log
  sleep $timeout

  echo "remediating fault" >> $basedir/stopservice.log
  $REMEDIATE_CMD
  if [ \$? -ne 0 ];then
    echo "failed to remediate the fault" >> $basedir/stopservice.log
    exit $errorExitCode
  fi

  echo "Service is restarted, fault is remediated" >> $basedir/stopservice.log 2>&1
#  rm -rf $basedir/trigger_stopservice.sh  > /dev/null 2>&1
#	rm -rf $basedir/stopservice.sh  > /dev/null 2>&1
#	rm -rf $basedir/stopservice.log  > /dev/null 2>&1
  exit 0
EOF

  chmod +x $basedir/trigger_stopservice.sh
  /bin/sh $basedir/trigger_stopservice.sh > $basedir/stopservice.log 2>&1 &
  echo "Triggered Stop Service fault"
  exit 0
}

#calling main function
main "$@"
