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
    if [ -z $dbName ] || [ -z $userName ] || [ -z $password ] || [ -z $port ] || [ -z $tableName ] || [ -z $errorCode ] || [ -z $percentage ] || [ -z $timeout ]; then
      help
    fi
  fi
  if [ "$operation" = "remediate" ]; then
    if [ -z $dbName ] || [ -z $userName ] || [ -z $password ] || [ -z $port ] || [ -z $tableName ]; then
      help
    fi
  fi
  if [ "$operation" = "status" ]; then
    if [ -z $dbName ] || [ -z $userName ] || [ -z $password ] || [ -z $port ] || [ -z $tableName ]; then
      help
    fi
  fi
}

help() {
  echo "$0 --operation=precheck"
  echo " or "
  echo "$0 --operation=status --dbName=<dbName> --userName=<userName> --password=<password> --port=<port> --tableName=<tableName>"
  echo " or "
  echo "$0 --operation=remediate --dbName=<dbName> --userName=<userName> --password=<password> --port=<port> --tableName=<tableName>"
  echo " or "
  echo "$0 --operation=inject --dbName=<dbName> --userName=<userName> --password=<password> --port=<port> --tableName=<tableName> --errorCode=<errorCode> --percentage=<percentage> --timeout=<timeoutinMilliseconds>"
  exit $errorexitcode
}

preRequisitescheck() {
  getCurrentConnections
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
  pgrep -f "pgdbtransactionerrorscript_$tableName.sh" >/dev/null 2>&1
  statusCheckRetVal=$?
  if [ $statusCheckRetVal -eq 0 ]; then
    export PGPASSWORD=$password
    triggerRes=$(psql -h localhost -p $port -d $dbName -U $userName) << EOS
select count(*) from pg_trigger where not tgisinternal and tgrelid = '$tableName'::regclass;
EOS
    trigger_exit_status=$?
    if [ $trigger_exit_status -ne 0 ]; then
      echo "psql failed while getting current connections"
      exit $trigger_exit_status
    fi
    triggercount=$(echo $triggerRes | cut -d' ' -f3 | tr -d $'\n')
    echo "No. of triggers : "$triggercount
    if [ $triggercount -gt 0 ]; then
      echo "Postgres Db transaction error fault is already running on the $tableName.Wait until it completes if you want to inject again."
      exit $errorExitCode
    else
      echo "Status: Completed/NotRunning"
    fi
  else
  echo "Status: Completed/NotRunning"
  fi
}

getCurrentConnections() {
   export PGPASSWORD=$password
   var=$(psql -h localhost -p $port -d $dbName -U $userName) << EOS
select count(*) from pg_stat_activity;
EOS
   psql_exit_status=$?
   if [ $psql_exit_status -ne 0 ]; then
    echo "psql failed while getting current connections"
    exit $psql_exit_status
   fi
   activeconnections=$(echo $var | cut -d' ' -f3 | tr -d $'\n')
   echo "No. of active connections : "$activeconnections
}

remediate() {
  parentProcessID=$(pgrep -f "pgdbtransactionerrorscript_$tableName.sh")
  if [ ! -z "$parentProcessID" ]; then
    #Pausing the parent process
    pgrep -f "pgdbtransactionerrorscript_$tableName.sh" | xargs -L 1 kill -STOP
    export PGPASSWORD=$password
    psql -h localhost -p $port -d $dbName -U $userName << EOS
DROP TRIGGER fiaascoExceptiontrigger on $tableName;
DROP FUNCTION triggerException();
EOS
    #Killing all the child processes created by parent process
    pgrep -f "pgdbtransactionerrorscript_$tableName.sh" | xargs -L 1 pgrep -P | xargs kill -9 > /dev/null 2>&1
    #Killing the parent process
    kill -9 $parentProcessID >/dev/null 2>&1
  else
    echo "Postgres Db transaction error Fault already remediated"
    cleanup
    exit $errorExitCode
  fi
  cleanup
  echo "Remediated: Postgres Db transaction error injection"
  exit 0
}

cleanup(){
    rm -rf $basedirectory/createExceptionTriggerFunction_$tableName.txt > /dev/null 2>&1
    rm -rf $basedirectory/createExceptionTrigger_$tableName.txt > /dev/null 2>&1
    rm -rf $basedirectory/pgdbtransactionerrorscript_$tableName.sh > /dev/null 2>&1
    rm -rf $basedirectory/pgdbtransactionerror.sh > /dev/null 2>&1
    rm -rf $basedirectory/pgDbTransactionErrorFault_$tableName.log > /dev/null 2>&1
}

isTableExist() {
   var=$(psql -h localhost -p $port -d $dbName -U $userName) << EOS
SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = '$tableName');
EOS
   psql_exit_status=$?
   if [ $psql_exit_status -ne 0 ]; then
    echo "psql failed while getting table information"
    exit $psql_exit_status
   fi
   existFlag=$(echo $var | cut -d' ' -f3 | tr -d $'\n')
   echo "Table exist : "$existFlag
   if [ "$existFlag" = "f" ]; then
    echo "Provided table doesn't exist in db"
    exit $errorExitCode
   fi
}

injectFault() {
   export PGPASSWORD=$password
   isTableExist
   cat << EOF > $basedirectory/createExceptionTriggerFunction_$tableName.txt
   CREATE OR REPLACE FUNCTION triggerException()
   RETURNS trigger AS
   \$BODY\$
   declare
      enable integer :=$percentage;
      config integer[] := array[enable,100-enable];
      rn integer;
   BEGIN
     rn := (SELECT floor(random() * 100 + 1)::int);
     FOR i in 1 .. 2 LOOP
        rn := rn-config[i];
        IF rn < 0 THEN
          IF config[i] = enable THEN
              RAISE SQLSTATE '$errorCode' ;
          END IF;
          EXIT;
        END IF;
     END LOOP;
     RETURN NEW;
  END
  \$BODY\$
  LANGUAGE plpgsql VOLATILE;
EOF

   cat << EOF > $basedirectory/createExceptionTrigger_$tableName.txt
   CREATE TRIGGER fiaascoExceptiontrigger
   BEFORE INSERT OR UPDATE OR DELETE
   ON $tableName
   FOR EACH ROW
   EXECUTE PROCEDURE triggerException();
EOF

   cat << EOF > $basedirectory/pgdbtransactionerrorscript_$tableName.sh
   #!/bin/sh
   dbName=\$1
   userName=\$2
   password=\$3
   port=\$4
   timelimit=\$5
   timelimit=\$((\$timelimit / 1000))
   tableName=\$6
   export PGPASSWORD=\$password

   getexitstatus() {
     psql_exit_status=\$1
     message=\$2
     if [ \$psql_exit_status -ne 0 ]; then
       echo \$message \$psql_exit_status
       exit \$psql_exit_status
     fi
   }

   createtriggerfunction() {
     psql -h localhost -p \$port -d \$dbName -U \$userName -f $basedirectory/createExceptionTriggerFunction_\$tableName.txt
     psql_exit_status=\$?
     getexitstatus \$psql_exit_status "Exception trigger function creation failed"
   }

   createtrigger(){
     psql -h localhost -p \$port -d \$dbName -U \$userName -f $basedirectory/createExceptionTrigger_\$tableName.txt
     psql_exit_status=\$?
     getexitstatus \$psql_exit_status "Exception trigger creation failed"
   }

   createtriggerfunction
   createtrigger
   echo "sleeping for duration"
   sleep \$timelimit
   echo "exit from sleep"
   psql -h localhost -p \$port -d \$dbName -U \$userName << EOS
DROP TRIGGER fiaascoExceptiontrigger on \$tableName;
DROP FUNCTION triggerException();
EOS
   rm -rf $basedirectory/createExceptionTriggerFunction_\$tableName.txt > /dev/null 2>&1
   rm -rf $basedirectory/createExceptionTrigger_\$tableName.txt > /dev/null 2>&1
   rm -rf $basedirectory/pgdbtransactionerrorscript_\$tableName.sh > /dev/null 2>&1
   rm -rf $basedirectory/pgdbtransactionerror.sh > /dev/null 2>&1
   rm -rf $basedirectory/pgDbTransactionErrorFault_\$tableName.log > /dev/null 2>&1
EOF
  chmod 777 $basedirectory/pgdbtransactionerrorscript_$tableName.sh
  sh $basedirectory/pgdbtransactionerrorscript_$tableName.sh $dbName $userName $password $port $timeout $tableName > $basedirectory/pgDbTransactionErrorFault_$tableName.log 2>&1 &
  echo "Triggered Postgres Db transaction error fault"
  exit 0
}

#calling main function
main $@