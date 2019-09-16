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
        if [ -z $faultOperation ] || [ -z $nicName ] 
        then
            help
        fi
    fi
}
validateInputs(){
    if [ $faultOperation = "NETWORK_DELAY_MILLISECONDS" ]
    then
        if [ -z $(echo $latency|grep -E "^[0-9]+$") ]
        then
            validationMessage="latency value should be an integer\n"
        else
            if [ $latency -lt 1 ]
            then
                validationMessage="latency value should be greater than 0 \n"
            fi
        fi
    else
        if [ -z $(echo $percentage|grep -E "^[0-9]+$") ]
        then
            validationMessage="percentage value should be an integer\n"
        else
            if [ $percentage -lt 1 ]
            then
                validationMessage="percentage value should be greater than 0 \n"
            fi
        fi
    fi
    if [ -z $(echo $timeout|grep -E "^[0-9]+$") ]
    then
        validationMessage="${validationMessage}timeout value should be an integer\n"
    fi
    machineNicName=$(ip addr show label $nicName)
    if [ -z "$machineNicName" ]
    then
        validationMessage="${validationMessage},Given Nic name not found"
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
    echo "$0 --operation=inject --faultOperation=<NETWORK_DELAY_MILLISECONDS or PACKET_DUPLICATE_PERCENTAGE or PACKET_CORRUPT_PERCENTAGE or PACKET_LOSS_PERCENTAGE> --latency=<Integer value which represents the latency in milliseconds.Only for Network Latency> --percentage=<integer value between 1 to 100 representing % fault on packets.Required incase of duplication,corruption and loss> --nicName=<nic name eg:eth0> --timeout=<timeoutinMilliseconds>"
    exit $errorExitCode
}
preRequisitescheck(){
    isTcPresent
    checkSudoTc
    isIpPresent
    isPgrepPresent
    isAwkPresent
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
        precheckmessage="$precheckmessage Write permission on ${basedirectory} is required,"
    fi
}

checkSudoTc(){
    sudo tc> /dev/null 2>&1
    sudoRetVal=$?
    if [ $sudoRetVal -eq 0 ]; then
        sudoCommand="sudo"
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

isIpPresent(){
    ip address  > /dev/null 2>&1
    ipRetVal=$?
    if [ $ipRetVal -ne 0 ]
    then
        precheckmessage="$precheckmessage ip is required,"
    fi
}
isTcPresent(){
    tc  > /dev/null 2>&1
    tcRetVal=$?
    if [ $tcRetVal -ne 0 ]
    then
        precheckmessage="tc is required,"
    fi
}
status(){
    pgrep -f "networkFaultscript.sh" > /dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]
    then
        echo "Network fault is already running.Wait until it completes if you want to inject again."
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}
remediate(){
    parentProcessID=$(pgrep -f "networkFaultscript.sh")
    if [ ! -z "$parentProcessID" ]
    then
        $sudoCommand tc qdisc del dev "$nicName" root netem
        pgrep -f "networkFaultscript.sh" | xargs pkill -TERM -P
        #Pausing the parent process
        pgrep -f "networkFaultscript.sh" |xargs -L 1 kill -STOP
        #Killing all the child processes created by parent process
        pgrep -f "networkFaultscript.sh" |xargs -L 1 pgrep -P |xargs kill -9 > /dev/null 2>&1
        #Killing the parent process
        kill -9 $parentProcessID > /dev/null 2>&1
        cleanup
        exit 0
    else
        echo "Network Fault already remediated"
        cleanup
        exit $errorExitCode
    fi
    cleanup
    echo "Remediated: Network fault injection"
}
cleanup(){
    rm -rf $basedirectory/networkFaultscript.sh  > /dev/null 2>&1
    rm -rf $basedirectory/networkFault.sh  > /dev/null 2>&1
    rm -rf $basedirectory/networkFault.log  > /dev/null 2>&1
}

injectFault(){
    validateInputs
    echo "Injecting Network latency fault"
    cat << EOF > $basedirectory/networkFaultscript.sh
        #!/bin/sh
        latency=\$1
        timeout=\$2
        timeout=\$((\$timeout / 1000))
        faultOperation=\$3
        nicName=\$4
        percentage=\$5
        remediationCommand="$sudoCommand tc qdisc del dev \$nicName root netem"
        injectionTimeMsg="Fault Injection Time"
        remediationTimeMsg="Fault Remediation Time"
        case "\$faultOperation" in
	"NETWORK_DELAY_MILLISECONDS")
		\$remediationCommand
		$sudoCommand tc qdisc add dev "\$nicName" root netem delay "\$latency"ms
		echo "\$injectionTimeMsg : \$(date)"
		sleep \$timeout
		echo "\$remediationTimeMsg : \$(date)"
		\$remediationCommand
		;;
	"PACKET_LOSS_PERCENTAGE")
		\$remediationCommand
		$sudoCommand tc qdisc add dev "\$nicName" root netem loss "\$percentage"%
		echo "\$injectionTimeMsg : \$(date)"
		sleep \$timeout
		echo "\$remediationTimeMsg : \$(date)"
		\$remediationCommand
		;;
	"PACKET_DUPLICATE_PERCENTAGE")
		\$remediationCommand
		$sudoCommand tc qdisc add dev "\$nicName" root netem duplicate "\$percentage"%
		echo "\$injectionTimeMsg : \$(date)"
		sleep \$timeout
		echo "\$remediationTimeMsg : \$(date)"
		\$remediationCommand
		;;
	"PACKET_CORRUPT_PERCENTAGE")
		\$remediationCommand
		$sudoCommand tc qdisc add dev "\$nicName" root netem corrupt "\$percentage"%
		echo "\$injectionTimeMsg : \$(date)"
		sleep \$timeout
		echo "\$remediationTimeMsg : \$(date)"
		\$remediationCommand
		;;
	*)
		echo "Invalid Network Fault Type"
		exit $errorExitCode
		;;
        esac
        echo "Autoremedition started"
        rm -rf $basedirectory/networkFaultscript.sh > /dev/null 2>&1
        rm -rf $basedirectory/networkFault.sh  > /dev/null 2>&1
        rm -rf $basedirectory/networkFault.log  > /dev/null 2>&1
EOF
    chmod 777 $basedirectory/networkFaultscript.sh
    sh $basedirectory/networkFaultscript.sh $latency $timeout $faultOperation $nicName $percentage > $basedirectory/networkFault.log 2>&1 &
    echo "Triggered Network fault"
    exit 0
}


#calling main function
main $@