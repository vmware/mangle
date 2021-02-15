#!/bin/sh

main() {
  errorExitCode=127
  readAndParseArgs $@
  basedirectory=$(dirname "$0")
  if [ "$operation" = "remediate" ]; then
    preRequisitescheck
    remediate
    exit 0
  fi

  if [ "$operation" = "precheck" ]; then
      preRequisitescheck
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
    if [ $operation != "inject" -a $operation != "precheck" -a $operation != "status" -a $operation != "remediate" ]; then
      help
    fi
  fi

  if [ "$operation" = "inject" ]; then
    if [ -z $dbName ] || [ -z $userName ] || [ -z $password ] || [ -z $port ] || [ -z $timeout ]; then
      help
    fi
  fi
  if [ "$operation" = "remediate" ]; then
    if [ -z $dbName ] || [ -z $userName ] || [ -z $port ]; then
      help
    fi
  fi
}

help() {
  echo "$0 --operation=<precheck | status>"
  echo " or "
  echo "$0 --operation=remediate --dbName=<dbName> --userName=<userName> --port=<port>"
  echo " or "
  echo "$0 --operation=inject --dbName=<dbName> --userName=<userName> --password=<password> --port=<port> --timeout=<timeoutinMilliseconds>"
  exit $errorexitcode
}

preRequisitescheck() {
  isPgrepPresent
  isExprPresent
  if [ ! -z "$precheckmessage" ]; then
    precheckmessage="Precheck Failed with pre-requisites : $precheckmessage"
    length=$(echo $precheckmessage |wc -c)
    echo ${precheckmessage} | cut -c 1-$(($length - 2))
    cleanup
    exit $errorExitCode
  fi
  echo "Precheck is successful"
}

isPgrepPresent() {
  pgrep >/dev/null 2>&1
  pgrepRetVal=$?
  if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
    precheckmessage="pgrep required,"
  fi
}

isExprPresent(){
    expr > /dev/null 2>&1
    exprRetVal=$?
    if [ $exprRetVal -ne 0 -a $exprRetVal -ne 1 -a $exprRetVal -ne 2 ]; then
        precheckmessage="$precheckmessage expr required,"
    fi
}

status() {
  pgrep -f "pgdbconnectionleakscript.sh" >/dev/null 2>&1
  statusCheckRetVal=$?
  if [ $statusCheckRetVal -eq 0 ]; then
    echo "Postgres Db connection leak fault is already running.Wait until it completes if you want to inject again."
    exit $errorExitCode
  else
  echo "Status: Completed/NotRunning"
  fi
}

remediate() {
  parentProcessID=$(pgrep -f "pgdbconnectionleakscript.sh")
  if [ ! -z "$parentProcessID" ]; then
    #Pausing the parent process
    pgrep -f "pgdbconnectionleakscript.sh" | xargs -L 1 kill -STOP
    pgrep -f ".*psql.* -h localhost -p $port -d $dbName -U $userName" | xargs kill -9 >/dev/null 2>&1
    #Killing all the child processes created by parent process
    pgrep -f "pgdbconnectionleakscript.sh" | xargs -L 1 pgrep -P | xargs kill -9 > /dev/null 2>&1
    #Killing the parent process
    kill -9 $parentProcessID >/dev/null 2>&1
  else
    echo "Postgres Db connection leak Fault already remediated"
    cleanup
    exit $errorExitCode
  fi
  cleanup
  echo "Remediated: Postgres Db connection leak injection"
  exit 0
}

cleanup(){
    rm -rf $basedirectory/pgdbconnectionleakscript.sh > /dev/null 2>&1
    rm -rf $basedirectory/pgdbconnectionleak.sh > /dev/null 2>&1
    rm -rf $basedirectory/pgDbConnectionLeakFault.log > /dev/null 2>&1
}

getCurrentConnections() {
   var=$(psql -h localhost -p $port -d $dbName -U $userName) << EOS
select count(*) from pg_stat_activity;
EOS
   psql_exit_status=$?
   if [ $psql_exit_status -ne 0 ]; then
    echo "psql failed to get active connections"
    exit $psql_exit_status
   fi
   activeconnections=$(echo $var | cut -d' ' -f3 | tr -d $'\n')
   echo "No. of active connections : "$activeconnections
}

injectFault() {
   export PGPASSWORD=$password
   getCurrentConnections
   cat << EOF > $basedirectory/pgdbconnectionleakscript.sh
   #!/bin/sh
   dbName=\$1
   userName=\$2
   password=\$3
   port=\$4
   timelimit=\$5
   export PGPASSWORD=\$password
   startTime=\$((\$(date +%s)))
   currentTime=\$((\$(date +%s)))
   timelimit=\$((\$timelimit / 1000))
   check=\`expr \$currentTime - \$timelimit\`
   while [ \$startTime -gt \$check ]
   do
       yes "" | psql -h localhost -p \$port -d \$dbName -U \$userName &
       currentTime=\$((\$(date +%s)))
       check=\`expr \$currentTime - \$timelimit\`
   done
  pgrep -f "[^ ]*psql[^ ]* -h localhost -p \$port -d \$dbName -U \$userName" | xargs kill -9 > /dev/null 2>&1
  rm -rf $basedirectory/pgdbconnectionleakscript.sh > /dev/null 2>&1
  rm -rf $basedirectory/pgdbconnectionleak.sh > /dev/null 2>&1
  rm -rf $basedirectory/pgDbConnectionLeakFault.log > /dev/null 2>&1
EOF
  chmod 777 $basedirectory/pgdbconnectionleakscript.sh
  sh $basedirectory/pgdbconnectionleakscript.sh $dbName $userName $password $port $timeout > $basedirectory/pgDbConnectionLeakFault.log 2>&1 &
  echo "Triggered Postgres Db connection leak fault"
  exit 0
}

#calling main function
main $@