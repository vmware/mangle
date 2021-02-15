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
    formMongoCommand
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
    if [ -z $dbName ] || [ -z $userName ] || [ -z $password ] || [ -z $port ] || [ -z $timeout ] || [ -z $sslEnabled ]; then
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
  echo "$0 --operation=inject --dbName=<dbName> --userName=<userName> --password=<password> --port=<port> --sslEnabled=<sslEnabled> --timeout=<timeoutinMilliseconds>"
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
  pgrep -f "mongodbconnectionleakscript.sh" >/dev/null 2>&1
  statusCheckRetVal=$?
  if [ $statusCheckRetVal -eq 0 ]; then
    echo "Mongo Db connection leak fault is already running.Wait until it completes if you want to inject again."
    exit $errorExitCode
  else
  echo "Status: Completed/NotRunning"
  fi
}

remediate() {
  parentProcessID=$(pgrep -f "mongodbconnectionleakscript.sh")
  if [ ! -z "$parentProcessID" ]; then
    #Pausing the parent process
    pgrep -f "mongodbconnectionleakscript.sh" | xargs -L 1 kill -STOP
    pgrep -f ".*mongo.* --host localhost --port $port --username $userName .*--password.* --authenticationDatabase $dbName" | xargs kill -9 >/dev/null 2>&1
    #Killing all the child processes created by parent process
    pgrep -f "mongodbconnectionleakscript.sh" | xargs -L 1 pgrep -P | xargs kill -9 > /dev/null 2>&1
    #Killing the parent process
    kill -9 $parentProcessID >/dev/null 2>&1
  else
    echo "Mongo Db connection leak Fault already remediated"
    cleanup
    exit $errorExitCode
  fi
  cleanup
  echo "Remediated: Mongo Db connection leak injection"
  exit 0
}

cleanup(){
    rm -rf $basedirectory/mongodbconnectionleakscript.sh > /dev/null 2>&1
    rm -rf $basedirectory/mongodbconnectionleak.sh > /dev/null 2>&1
    rm -rf $basedirectory/mongoDbConnectionLeakFault.log > /dev/null 2>&1
}

getCurrentConnections() {
   var=$($MONGO_COMMAND --host localhost --port $port --username $userName --password $password --authenticationDatabase $dbName) << EOS
db.serverStatus().connections
EOS
   mongo_exit_status=$?
   if [ $mongo_exit_status -ne 0 ]; then
    echo "mongo failed to get active connections"
    exit $mongo_exit_status
   fi
   activeconnections=$(echo $var | grep -Po '"current" : .*?[^\\],' | cut -d':' -f2 | tr -d $',')
   echo "No. of active connections :"$activeconnections
}

formMongoCommand() {
  MONGO_COMMAND="mongo"
  if [ "$sslEnabled" = "true" ]; then
    MONGO_COMMAND="$MONGO_COMMAND --ssl --sslAllowInvalidCertificates"
  fi
}

injectFault() {
   getCurrentConnections
   cat << EOF > $basedirectory/mongodbconnectionleakscript.sh
   #!/bin/sh
   dbName=\$1
   userName=\$2
   password=\$3
   port=\$4
   timelimit=\$5
   startTime=\$((\$(date +%s)))
   currentTime=\$((\$(date +%s)))
   timelimit=\$((\$timelimit / 1000))
   check=\`expr \$currentTime - \$timelimit\`
   while [ \$startTime -gt \$check ]
   do
       yes "" | $MONGO_COMMAND --host localhost --port \$port --username \$userName --password \$password --authenticationDatabase \$dbName &
       currentTime=\$((\$(date +%s)))
       check=\`expr \$currentTime - \$timelimit\`
   done
  pgrep -f ".*mongo.* --host localhost --port \$port --username \$userName .*--password.* --authenticationDatabase \$dbName" | xargs kill -9 > /dev/null 2>&1
  rm -rf $basedirectory/mongodbconnectionleakscript.sh > /dev/null 2>&1
  rm -rf $basedirectory/mongodbconnectionleak.sh > /dev/null 2>&1
  rm -rf $basedirectory/mongoDbConnectionLeakFault.log > /dev/null 2>&1
EOF
  chmod 777 $basedirectory/mongodbconnectionleakscript.sh
  sh $basedirectory/mongodbconnectionleakscript.sh $dbName $userName $password $port $timeout > $basedirectory/mongoDbConnectionLeakFault.log 2>&1 &
  echo "Triggered Mongo Db connection leak fault"
  exit 0
}

#calling main function
main $@