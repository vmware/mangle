#!/bin/sh
main(){
errorexitcode=127
readAndParseArgs "$@"
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

if [ "$operation" = "inject" ]
then
    preRequisitescheck
    injectFault
    exit 0
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
      if [ -z $processIdentifier ]
	  then
       help
	  fi
  fi

  if [ $operation = "remediate" ]
  then
      if [ -z "$remediationCommand" ]
	  then
       help
	  fi
  fi
}

help(){
    echo "$0 --operation=<precheck | status>"
    echo " or "
    echo "$0 --operation=remediate --remediationCommand=<remediationCommand>"
    echo " or "
    echo "$0 --operation=inject --processIdentifier=<processIdentifier>"
    exit $errorexitcode
}

preRequisitescheck()
{
    isPgrepPresent
    isKillPresent
    if [ ! -z "$precheckmessage" ]
   then
      echo "Precheck:Failed $precheckmessage required to proceed with injection"
      exit $errorexitcode
   fi
   echo "Precheck:Success"
}

isPgrepPresent(){
    pgrep  > /dev/null 2>&1
    pgrepRetVal=$?
    if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,pgrep"
    fi
}

isKillPresent(){
    kill > /dev/null 2>&1
    killRetVal=$?
    if [ $killRetVal -ne 0 -a $killRetVal -ne 1 -a $killRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage,kill"
    fi
}

status(){
    pgrep -f "killprocessscript.sh" > /dev/null 2>&1
    statusCheckRetVal=$?
    if [ $statusCheckRetVal -eq 0 ]
    then
        echo "Status: Inprogress"
    else
        echo "Status: Completed/NotRunning"
    fi
}

remediate(){
    $remediationCommand &
    cleanup
    echo "Remediated: Kill Service injection"
}

cleanup(){
    rm -rf $basedir/killprocessscript.sh  > /dev/null 2>&1
}

injectFault(){
processcount=$(pgrep -f $processIdentifier | wc -l)
echo "Found:"${processcount} "in endpoint"
#Three default processes related to this script
if [ "$processcount" -ne 4 ]
then
    echo "identifier is wrong,cant kill service"
    exit $errorexitcode
fi
cat << EOF > $basedir/killprocessscript.sh
#!/bin/sh
#sleeping the process so that if pid is the entry point process container may exit and task will fail
identifier=\$1
sleep 5s
pgrep -f \$identifier |xargs kill -9
EOF
chmod 777 $basedir/killprocessscript.sh
$basedir/killprocessscript.sh $processIdentifier &
echo "Triggered: Kill Process injection"
}

#calling main function
main "$@"