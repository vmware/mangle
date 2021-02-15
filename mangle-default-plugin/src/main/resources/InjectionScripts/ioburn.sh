#!/bin/sh

main(){
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
    exit 0
fi

if [ "$operation" = "status" ]
then
    status
    exit 0
fi
preRequisitescheck
status
isSudoPresent
injectFault
}

isSudoPresent(){
    sudo -nv > /dev/null 2>&1
    sudoRetVal=$?
    if [ $sudoRetVal -eq 0 ]; then
        sudoCommand="sudo"
    fi
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
        if [ -z $targetDir ] || [ -z $timeout ] || [ -z $blockSize ]
        then
            help
        fi
    fi

}

validateInputs(){

    if [ ! -d "$targetDir" ]; then
        validationMessage="${targetDir} is not available"
    else
        $sudoCommand touch $targetDir/mangle.txt > /dev/null 2>&1
        touchRetVal=$?
        if [ $touchRetVal -ne 0 ]; then
            validationMessage="The Provided user does not have permission on ${targetDir} and sudo also not exists in this machine"
        else
            $sudoCommand rm -f $targetDir/mangle.txt > /dev/null 2>&1
        fi
    fi
    if [ -z $(echo $blockSize|grep -E "^[0-9]+$") ]
    then
        validationMessage="${validationMessage} blockSize value should be an integer\n"
    else
        targetDirDiskSize=$(df -B1 $targetDir | awk ' NR==2 { print $2 } ')
        echo "targetDirDiskSize:"$targetDirDiskSize" bytes"
        if [ $blockSize -gt $targetDirDiskSize ]
        then
           validationMessage="${validationMessage} Provide iosize less than maximum size of disk\n"
        fi
    fi

    if [ -z $(echo $timeout|grep -E "^[0-9]+$") ]
    then
        validationMessage="${validationMessage} timeout value should be an integer\n"
    fi
    if [ ! -z "$validationMessage" ]
    then
        printf "Validation Failed:$validationMessage"
        cleanup
        exit $errorExitCode
    fi
}

help(){
    echo "$0 --operation=<remediate | precheck | status>"
    echo " or "
    echo "$0 --operation=inject --targetDir=<targetDir> --blockSize=<blockSize> --timeout=<timeoutinMilliseconds>"
    exit $errorExitCode
}

status(){
  pgrep -f "loopburnio.sh" > /dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]
    then
        echo "Diskio fault is already running.Wait until it completes if you want to inject again."
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}
remediate(){
    if [ -f $basedir/.ioburnOutput.properties ]
    then
        targetDir=$(echo $(cat $basedir/.ioburnOutput.properties | grep -iE "^targetDir" |xargs) |cut -d ':' -f2)
    fi
    processID=$(pgrep -f "loopburnio.sh")
    if [ ! -z "$processID" ]
    then
        kill -STOP $processID > /dev/null 2>&1
        pgrep -f "loopburnio.sh"|xargs -L 1 pgrep -P |xargs kill -9
        kill -9 $processID > /dev/null 2>&1
        cleanup
        echo "Remediated: ioburn injection"
        exit 0
    else
        echo "Diskio Fault already remediated"
        cleanup
        exit $errorExitCode
    fi
}

cleanup(){
  $sudoCommand rm -rf $targetDir/burn > /dev/null 2>&1
  rm -rf $basedir/loopburnio.sh > /dev/null 2>&1
  rm -rf $basedir/ioburn.sh > /dev/null 2>&1
  rm -rf $basedir/.ioburnOutput.properties > /dev/null 2>&1
  rm -rf $basedir/diskioFault.log > /dev/null 2>&1
  rm -rf $basedir/diskIOFaultWatcher.sh > /dev/null 2>&1
}

preRequisitescheck()
{
    isDDPresent
    isExprPresent
    isPgrepPresent
    isStatPresent
    checkWritePermissionOfInjectionDir
    if [ ! -z "$precheckmessage" ]
    then
        precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
        length=$(echo $precheckmessage |wc -c)
        echo ${precheckmessage} | cut -c 1-$(($length - 2))
        cleanup
        exit $errorExitCode
    fi
    echo "Precheck:Success"
}

isDDPresent(){
    dd --version > /dev/null 2>&1
    ddRetVal=$?
    if [ $ddRetVal -ne 0 -a $ddRetVal -ne 1 -a $ddRetVal -ne 2 ]; then
        precheckmessage="dd is required,"
    fi
}

isExprPresent(){
    expr > /dev/null 2>&1
    exprRetVal=$?
    if [ $exprRetVal -ne 0 -a $exprRetVal -ne 1 -a $exprRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage expr is required,"
    fi
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage pgrep is required,"
    fi
}

isStatPresent(){
    stat  > /dev/null 2>&1
    statRetVal=$?
    if [ $statRetVal -ne 0 -a $statRetVal -ne 1 -a $statRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage stat is required,"
    fi
}

checkWritePermissionOfInjectionDir()
{
    if [ ! -w "$basedir" ]
    then
        precheckmessage="$precheckmessage Write permission on ${basedir} is required,"
    fi
}

injectFault(){
  validateInputs
  echo "Starting ioburn injection"
  cat << EOF > $basedir/loopburnio.sh
    targetDir=\$1
    blockSize=\$2
    timelimit=\$3
    basedir=\$4
    echo "targetDir:"\$targetDir > \$basedir/.ioburnOutput.properties
    startTime=\$((\$(date +%s)))
    currentTime=\$((\$(date +%s)))
    timelimit=\$((\$timelimit / 1000))
    check=\`expr \$currentTime - \$timelimit\`
    echo "ioburn injection starting on "\$targetDir "for ":\$timelimit "seconds"
    while [ \$startTime -gt \$check ]
    do
        $sudoCommand dd if=/dev/zero of=\$targetDir/burn bs=\$blockSize count=1024 > /dev/null 2>&1
        currentTime=\$((\$(date +%s)))
        check=\`expr \$currentTime - \$timelimit\`
    done
    echo "Autoremedition started"
    $sudoCommand rm -rf \$targetDir/burn > /dev/null 2>&1
    rm -rf \$basedir/loopburnio.sh > /dev/null 2>&1
    rm -rf \$basedir/.ioburnOutput.properties > /dev/null 2>&1
    rm -rf \$basedir/ioburn.sh > /dev/null 2>&1
    rm -rf \$basedir/diskioFault.log > /dev/null 2>&1
    rm -rf \$basedir/diskIOFaultWatcher.sh > /dev/null 2>&1
EOF
  cat << EOF > $basedir/diskIOFaultWatcher.sh
    timelimit=\$((\$1 / 1000))
    targetDir=\$2
    basedir=\$3
    echo "sleeping for "\$timelimit "seconds"
    sleep \$timelimit
    processID=\$(pgrep -f "loopburnio.sh")
    if [ ! -z "\$processID" ]
    then
       kill -STOP \$processID > /dev/null 2>&1
       pgrep -f "loopburnio.sh"|xargs -L 1 pgrep -P |xargs kill -9
       kill -9 \$processID > /dev/null 2>&1
       $sudoCommand rm -rf \$targetDir/burn > /dev/null 2>&1
       rm -rf \$basedir/loopburnio.sh > /dev/null 2>&1
       rm -rf \$basedir/.ioburnOutput.properties > /dev/null 2>&1
       rm -rf \$basedir/ioburn.sh > /dev/null 2>&1
       rm -rf \$basedir/diskioFault.log > /dev/null 2>&1
       rm -rf \$basedir/diskIOFaultWatcher.sh > /dev/null 2>&1
       echo "Alternate Remediation triggered"
    fi
EOF
  chmod 777 $basedir/loopburnio.sh
  chmod 777 $basedir/diskIOFaultWatcher.sh
  sh $basedir/loopburnio.sh $targetDir $blockSize $timeout $basedir > $basedir/diskioFault.log 2>&1 &
  sh $basedir/diskIOFaultWatcher.sh $timeout $targetDir $basedir > $basedir/diskioFault.log 2>&1 &
  echo "Triggered: ioburn injection"
  exit 0
}
main $@
