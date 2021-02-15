#!/bin/sh

main(){
	errorExitCode=127
	basedir=$(dirname "$0")
	readAndParseArgs $@

	if [ "$operation" = "remediate" ]
		then
   		prerequisitescheck
   		remediate
   		exit 0
	fi

	if [ "$operation" = "precheck" ]
	then
	   prerequisitescheck
	   echo "Precheck:Success"
	   exit 0
	fi

	if [ "$operation" = "status" ]
	then
	   status
	   exit 0
	fi

	prerequisitescheck
	status
	echo "Clock Skew arguments: --operation= $operation, --timeout= $timeout, --seconds= $seconds, --minutes= $minutes, --hours= $hours, --days= $days and --type= $type" >> $basedir/clockskew.log
	injectfault
	exit 0
}

#Method which reads and parse the arguments, it fails if the required arguments are not provided. 
readAndParseArgs(){
    if [ $# -eq 0 -o $# -gt 7 ]
    then
        echo "required number of arguments doesn't match" >> $basedir/clockskew.log
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
        echo "operation is not set" >> $basedir/clockskew.log
        help
    else
        if [ $operation != "inject" -a $operation != "precheck" -a $operation != "status" -a $operation != "remediate" ]
        then
                echo "unkown operation" >> $basedir/clockskew.log
            help
        fi
    fi

    if [ "$operation" = "inject" ]
    then
    	if [ -z $seconds ] && [ -z $minutes ] && [ -z $hours ] && [ -z $days ]; then
       		echo "minimum one clock input is required" >> $basedir/clockskew.log
       		help
    	fi
        if [ -z $type ] || [ -z $timeout ]; then
                echo "Clock skew injection type or timeout is not provided" >> $basedir/clockskew.log
            help
        fi
        if [ -z $seconds ]; then
            seconds=0
        fi
        if [ -z $minutes ]; then
            minutes=0
        fi
        if [ -z $hours ]; then
            hours=0
        fi
        if [ -z $days ]; then
            days=0
        fi
    fi

    if [ "$type" = "FUTURE" ]; then
       type=+
    elif [ "$type" = "PAST" ]; then
       type=-
    fi
}

help(){
   echo "$0 --operation=<remediate | precheck | status >"
   echo " or "
   echo "$0 --operation=inject --timeout=<timeoutinMilliseconds> --seconds=<secnods 0-60> --minutes=<minutes 0-60> --hours=<hours 0-24> --days=<days 0-365> --type=<+ or ->"
   echo "minimum one arguments among seconds, minutes, hours or days needs to be provided"
   exit $errorExitCode
}

prerequisitescheck(){
   findOSName
   getCommands
   status
}

#Method will remediate the fault by restarting the service and will cleanup at the end of execution.
remediate(){
   $REMEDIATE_CMD
   remediateFaultReturnVal=$?
   validateOutput "$remediateFaultReturnVal" "$REMEDIATE_CMD"
   echo "NTP service is restarted, fault is remediated" >> $basedir/clockskew.log
   cleanup
   exit 0
}

#Method to find OS name so the command can be constructed based on OS
findOSName(){
	if [ -f /etc/os-release ]; then
	   # freedesktop.org and systemd
	   . /etc/os-release
	   OS=$NAME
	   VER=$VERSION_ID
	elif type lsb_release >/dev/null 2>&1; then
	   # linuxbase.org
	   OS=$(lsb_release -si)
	   VER=$(lsb_release -sr)
	elif [ -f /etc/lsb-release ]; then
	   # For some versions of Debian/Ubuntu without lsb_release command
	   . /etc/lsb-release
	   OS=$DISTRIB_ID
	   VER=$DISTRIB_RELEASE
	elif [ -f /etc/debian_version ]; then
	   # Older Debian/Ubuntu/etc.
	   OS=Debian
	   VER=$(cat /etc/debian_version)
	else
	   # Fall back to uname, e.g. "Linux <version>", also works for BSD, etc.
	   OS=$(uname -s)
	   VER=$(uname -r)
	fi
	if [ -z "$OS" ]; then
	   echo "Failed to extract OS name and version" >> $basedir/clockskew.log
	   exit $errorExitCode
	fi
	echo $OS $VER
}

#Set injection, status and remediate command based on the OS
getCommands(){
	if [ ! -z "$(echo $OS | grep -E "*Ubuntu*")" ]; then
	   STOP_CMD="service ntp stop"
	   REMEDIATE_CMD="service ntp restart"
	   STATUS_CMD="service ntp status"

	elif [ ! -z "$(echo $OS | grep -E "*CentOS*")" ] || [ ! -z "$(echo $OS | grep -E "*Red Hat*")" ]; then
	   STOP_CMD="service ntpd stop"
	   REMEDIATE_CMD="service ntpd restart"
	   STATUS_CMD="service ntpd status"

	elif [ ! -z "$(echo $OS | grep -E "*Fedora*")" ] || [ ! -z "$(echo $OS | grep -E "*SLES*")" ]; then
	   STOP_CMD="systemctl stop ntpd"
	   REMEDIATE_CMD="systemctl restart ntpd"
	   STATUS_CMD="systemctl status ntpd"

	elif [ ! -z "$(echo $OS | grep -E "*Photon*")" ]; then
	   STOP_CMD="systemctl stop systemd-timesyncd"
	   REMEDIATE_CMD="systemctl restart systemd-timesyncd"
	   STATUS_CMD="systemctl status systemd-timesyncd"

	else
	   echo "Mangle doesn't support TimesSkew on the provided OS." >> $basedir/clockskew.log
	   exit $errorExitCode
	fi

	echo "Command to stop ntp service is: $STOP_CMD, and command to remediate the fault is: $REMEDIATE_CMD" >> $basedir/clockskew.log
}

status(){
   $STATUS_CMD  >/dev/null 2>&1
   statusCheckReturnVal=$?
   if [ $statusCheckReturnVal -eq 0 ]
   then
      echo "Status: Completed/NotRunning"
   else
      echo "Status: Inprogress or NTP is not configured on the system"
   fi
}

cleanup(){
	rm -rf $basedir/clockskew.sh  > /dev/null 2>&1
	rm -rf $basedir/clockskew.log  > /dev/null 2>&1
	exit 0
}

#Method will validate the ExitCode of a command
validateOutput(){
   if [ $1 -eq 0 ]
   then
      echo "Command: $2 executed successfully" >> $basedir/clockskew.log
   else
      echo "Command: $2 execution failed" >> $basedir/clockskew.log
      exit $errorExitCode
   fi
}

#Method will stop the service, change the date, sleep till timeout and then remediate the fault
injectfault(){
	timeout=$(($timeout / 1000))
	cat << EOF > $basedir/trigger_clockskew.sh
	#!/bin/sh

	echo "Stoping ntp service" >> $basedir/clockskew.log
	$STOP_CMD
	if [ \$? -ne 0 ];then
		echo "ntp stop command execution failed" >> $basedir/clockskew.log
		exit $errorExitCode
	fi

	echo "Injecting fault" >> $basedir/clockskew.log
	d="$type$days days"
	h="$type$hours hours"
	m="$type$minutes minutes"
	s="$type$seconds seconds"
	date=\$(date -d "\$d \$h \$m \$s")
	date -s "\$date"
	if [ \$? -ne 0 ];then
		echo "failed to set new date and time" >> $basedir/clockskew.log
		exit $errorExitCode
	fi

	echo "sleeping for $timeout seconds" >> $basedir/clockskew.log
	sleep $timeout

	echo "remediating fault" >> $basedir/clockskew.log
	$REMEDIATE_CMD
	if [ \$? -ne 0 ];then
		echo "failed to remediate the fault" >> $basedir/clockskew.log
		exit $errorExitCode
	fi

	echo "NTP service is restarted, fault is remediated" >> $basedir/clockskew.log 2>&1
	rm -rf $basedir/trigger_clockskew.sh  > /dev/null 2>&1
	rm -rf $basedir/clockskew.sh  > /dev/null 2>&1
	rm -rf $basedir/clockskew.log  > /dev/null 2>&1
	exit 0
EOF

	chmod +x $basedir/trigger_clockskew.sh
	/bin/sh $basedir/trigger_clockskew.sh > $basedir/clockskew.log 2>&1 &
	echo "Triggered Clock Skew fault"
	exit 0
}

main $@