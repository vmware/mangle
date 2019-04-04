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
        if [ -z $targetDir ] || [ -z $timeout ] || [ -z $blockSize ]
        then
            help
        fi
    fi
    
}

validateInputs(){

    if [ ! -d "$targetDir" ]; then
        validationMessage="targetdir ${targetDir} is not present\n"
    else
        touch $targetDir/mangle.txt > /dev/null 2>&1
        touchRetVal=$?
        if [ $touchRetVal -ne 0 -a $touchRetVal -ne 1 -a $touchRetVal -ne 2 ]; then
            validationMessage="targetdir ${targetDir} does not have write permission"
        else
            rm -f $targetDir/mangle.txt > /dev/null 2>&1
        fi
    fi
    if [ -z $(echo $blockSize|grep -E "^[0-9]+$") ]
    then
        validationMessage="${validationMessage}blockSize value should be an integer\n"
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
        echo "Status: Inprogress"
        exit $errorExitCode
    else
        echo "Status: Completed/NotRunning"
    fi
}
remediate(){
    pgrep -f "loopburnio.sh" | xargs kill -9 > /dev/null 2>&1
    if [ -f $basedir/.ioburnOutput.properties ]
    then
        targetDir=$(echo $(cat $basedir/.ioburnOutput.properties | grep -iE "^targetDir" |xargs) |cut -d ':' -f2)
    fi
    cleanup
    echo "Remediated: ioburn injection"
}

cleanup(){
  rm -rf $targetDir/burn > /dev/null 2>&1
  rm -rf $basedir/loopburnio.sh > /dev/null 2>&1
  rm -rf $basedir/ioburn.sh > /dev/null 2>&1
  rm -rf $basedir/.ioburnOutput.properties > /dev/null 2>&1
}

preRequisitescheck()
{
    isDDPresent
    isExprPresent
    isPgrepPresent
    isStatPresent
    if [ ! -z "$precheckmessage" ]
    then
        echo "Precheck:Failed $precheckmessage required to proceed with injection"
        cleanup
        exit $errorExitCode
    fi
    echo "Precheck:Success"
}

isDDPresent(){
    dd --version > /dev/null 2>&1
    ddRetVal=$?
    if [ $ddRetVal -ne 0 -a $ddRetVal -ne 1 -a $ddRetVal -ne 2 ]; then
        precheckmessage="dd"
    fi
}

isExprPresent(){
    expr > /dev/null 2>&1
    exprRetVal=$?
    if [ $exprRetVal -ne 0 -a $exprRetVal -ne 1 -a $exprRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,expr"
    fi
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,pgrep"
    fi
}

isStatPresent(){
    stat  > /dev/null 2>&1
    statRetVal=$?
    if [ $statRetVal -ne 0 -a $statRetVal -ne 1 -a $statRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,stat"
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
    while [ \$startTime -gt \$check ]
    do
        dd if=/dev/zero of=\$targetDir/burn bs=\$blockSize count=1024 > /dev/null 2>&1
        currentTime=\$((\$(date +%s)))
        check=\`expr \$currentTime - \$timelimit\`
    done
    rm -rf \$targetDir/burn \$basedir/loopburnio.sh \$basedir/.ioburnOutput.properties \$basedir/ioburn.sh
EOF
  chmod 777 $basedir/loopburnio.sh
  sh $basedir/loopburnio.sh $targetDir $blockSize $timeout $basedir > $basedir/diskioFault.log 2>&1 &
  echo "Triggered: ioburn injection"
  exit 0
}
main $@