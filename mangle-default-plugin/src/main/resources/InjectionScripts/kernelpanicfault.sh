#!/bin/sh

main() {

    errorExitCode=127
    readAndParseArgs $@
    basedirectory=$(dirname "$0")
    if [ "$operation" = "remediate" ]; then
        remediate
        exit 0
    fi

    if [ "$operation" = "status" ]; then
        status
        exit 0
    fi

    preRequisitescheck
    status
    injectFault
}

readAndParseArgs() {
    if [ $# -eq 0 ]; then
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
    if [ -z $operation ]; then
        help
    else
        if [ $operation != "inject" -a $operation != "status" -a $operation != "remediate" ]; then
            help
        fi
    fi
}

help() {
    echo "$0 --operation=status"
    echo " or "
    echo "$0 --operation=remediate"
    echo " or "
    echo "$0 --operation=inject"
    exit $errorexitcode
}

preRequisitescheck() {
    isSysrqFileExist
    isUserHavingWritePermission
    checkWritePermissionOfInjectionDir
    if [ ! -z "$precheckmessage" ]; then
        precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
        length=$(echo $precheckmessage |wc -c)
        echo ${precheckmessage} | cut -c 1-$(($length - 2))
        cleanup
        exit $errorExitCode
    fi
    echo "Precheck is successful"
}

isSysrqFileExist() {
    if [ ! -f /proc/sys/kernel/sysrq ]; then
        precheckmessage="Unsupported Kernel Configuration, sysrq is not configured,"
    fi
}

isUserHavingWritePermission() {
    if [ ! -w /proc/sysrq-trigger ]; then
        precheckmessage="$precheckmessage Write permission on /proc/sysrq-trigger is required,"
    fi
}

checkWritePermissionOfInjectionDir()
{
    if [ ! -w "$basedirectory" ]
    then
        precheckmessage="$precheckmessage Write permission on ${basedirectory} is required,"
    fi
}

status() {
    pgrep -f "kernelpanicscript.sh" >/dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]; then
        echo "Kernel Panic fault is already running.Wait until it completes if you want to inject again."
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}

remediate() {
    echo "Kernel Panic fault does not support remediation"
    cleanup
    exit 0
}

cleanup() {
    rm -rf $basedirectory/kernelpanicscript.sh >/dev/null 2>&1
    rm -rf $basedirectory/kernelpanicfault.sh >/dev/null 2>&1
    rm -rf $basedirectory/kernelPanicFault.log >/dev/null 2>&1
}

injectFault() {
    cat <<EOF >$basedirectory/kernelpanicscript.sh
    #!/bin/sh
    echo "Injecting Kernel Panic fault"
    echo c > /proc/sysrq-trigger &
    rm -rf $basedirectory/kernelpanicscript.sh $basedirectory/kernelPanicFault.log >/dev/null 2>&1
    exit 0
EOF
    chmod 777 $basedirectory/kernelpanicscript.sh
    sh $basedirectory/kernelpanicscript.sh >$basedirectory/kernelPanicFault.log 2>&1 &
    echo "Triggered Kernel Panic fault"
    exit 0
}

#calling main function
main $@
