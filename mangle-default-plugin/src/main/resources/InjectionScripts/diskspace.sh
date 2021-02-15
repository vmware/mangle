#!/bin/sh

main() {
  errorExitCode=127
  readAndParseArgs $@
  basedirectory=$(dirname "$0")
  if [ "$operation" = "remediate" ]; then
    preRequisitescheck
    isDirectoryExist
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
    isDirectoryExist
    isSudoPresent
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
    if [ -z $directoryPath ] || [ -z $timeout ]; then
      help
      fi
    fi
  if [ "$operation" = "remediate" ]; then
    if [ -z $directoryPath ]; then
      help
    fi
  fi
}

help() {
  echo "$0 --operation=<precheck | status>"
  echo " or "
  echo "$0 --operation=remediate --directoryPath=<directoryPath>"
  echo " or "
  echo "$0 --operation=inject --directoryPath=<directoryPath> --timeout=<timeoutinMilliseconds> --diskFillSize=<diskFillSize>"
  exit $errorexitcode
}

preRequisitescheck() {
  isDfPresent
  isDdPresent
  isPgrepPresent
  isAwkPresent
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

isDfPresent() {
  df >/dev/null 2>&1
  dfRetVal=$?
  if [ $dfRetVal -ne 0 ]; then
    precheckmessage="df required,"
  fi
}

isDdPresent() {
  dd --version>/dev/null 2>&1
  ddRetVal=$?
  if [ $ddRetVal -ne 0 ]; then
    precheckmessage="dd required,"
  fi
}

isSudoPresent(){
    sudo -nv > /dev/null 2>&1
    sudoRetVal=$?
    if [ $sudoRetVal -eq 0 ]; then
        sudoCommand="sudo"
    fi
}

isPgrepPresent() {
  pgrep >/dev/null 2>&1
  pgrepRetVal=$?
  if [ $pgrepRetVal -ne 0 -a $pgrepRetVal -ne 1 -a $pgrepRetVal -ne 2 ]; then
    precheckmessage="$precheckmessage pgrep required,"
  fi
}

isAwkPresent() {
  awk >/dev/null 2>&1
  awkRetVal=$?
  if [ $awkRetVal -ne 0 -a $awkRetVal -ne 1 -a $awkRetVal -ne 2 ]; then
    precheckmessage="$precheckmessage awk required,"
  fi
}

checkWritePermissionOfInjectionDir() {
  if [ ! -w "$basedirectory" ]; then
    precheckmessage="$precheckmessage Write permission on ${basedirectory} required,"
  fi
}

status() {
  pgrep -f "diskspacescript.sh" >/dev/null 2>&1
  statusCheckRetVal=$?
  if [ $statusCheckRetVal -eq 0 ]; then
    echo "Disk Space fault is already running.Wait until it completes if you want to inject again."
    exit $errorExitCode
  else
  echo "Status: Completed/NotRunning"
  fi
}

remediate() {
  parentProcessID=$(pgrep -f "diskspacescript.sh")
  if [ ! -z "$parentProcessID" ]; then
    pgrep -f "diskspacescript.sh" | xargs pkill -TERM -P
    rm -rf "$directoryPath/mangleDumpFile.txt"
    #Pausing the parent process
    pgrep -f "diskspacescript.sh" | xargs -L 1 kill -STOP
    #Killing all the child processes created by parent process
    pgrep -f "diskspacescript.sh" | xargs -L 1 pgrep -P | xargs kill -9 >/dev/null 2>&1
    #Killing the parent process
    kill -9 $parentProcessID >/dev/null 2>&1
  else
    echo "Disk Space Fault already remediated"
    cleanup
    exit $errorExitCode
  fi
  cleanup
  echo "Remediated: Disk Space injection"
  exit 0
}

cleanup() {
  $sudoCommand rm -rf $directoryPath/mangleDumpFile.txt >/dev/null 2>&1
  rm -rf $basedirectory/diskspacescript.sh >/dev/null 2>&1
  rm -rf $basedirectory/diskspace.sh >/dev/null 2>&1
  rm -rf $basedirectory/diskSpaceFault.log >/dev/null 2>&1
}

isDirectoryExist() {
  if [ ! -d "$directoryPath" ]; then
    echo "The Provided directory path not found : $directoryPath"
    exit $errorExitCode
  fi
}

validateInputs() {
  if [ ! -z "$diskFillSize" ]; then
    usedDiskSize=$(df -h $directoryPath | awk ' NR==2 { print $5 } ')
    usedDiskSize=$(echo $usedDiskSize | cut -d% -f1)
    if [ $usedDiskSize -gt $diskFillSize ]; then
      echo "Used Disk percentage : $usedDiskSize%"
      validationMessage="The Provided diskFill percentage should be greater than used disk percentage\n"
    fi
  fi

  if [ ! -d "$directoryPath" ]; then
    validationMessage="The Provided directory path not found : ${directoryPath}\n"
  else
    if [ ! -w "$directoryPath" ]; then
      validationMessage="The Provided user does not have permission on ${directoryPath}"
      $sudoCommand touch $directoryPath/mangle.txt > /dev/null 2>&1
      touchRetValWithSudo=$?
      if [ $touchRetValWithSudo -ne 0 ]; then
      	validationMessage="${validationMessage} and sudo also not exists in this machine"
      else
        $sudoCommand rm -f $directoryPath/mangle.txt > /dev/null 2>&1
        validationMessage=""
      fi
    fi
  fi

  if [ ! -z "$validationMessage" ]; then
    printf "Validation Failed : $validationMessage"
    cleanup
    exit $errorExitCode
  fi
}

injectFault() {
  validateInputs
  cat <<EOF >$basedirectory/diskspacescript.sh
  #!/bin/sh
  directoryPath=\$1
  timeout=\$2
  diskFillSize=\$3
  timeout=\$((\$timeout / 1000))

  getDiskInfo()
  {
    diskInfo=\$1
    fileSystem=\$(echo \$diskInfo | awk ' { print \$1 } ')
    totalSize=\$(echo \$diskInfo | awk ' { print \$2 } ')
    usedSize=\$(echo \$diskInfo | awk ' { print \$3 } ')
    availSize=\$(echo \$diskInfo | awk ' { print \$4 } ')
    usePercentage=\$(echo \$diskInfo | awk ' { print \$5 } ')
    usePercentage=\$(echo \$usePercentage | cut -d% -f1)
    mountedOn=\$(echo \$diskInfo | awk ' { print \$6 } ')
    return 0
  }

  findDiskInfoText()
  {
    var=\$(df -P \$directoryPath | tail -1)
    if [ -z "\$var" ]
    then
      echo \$var
      retval=2
    else
      echo \$var
      retval=0
    fi
    return \$retval
  }

  getDiskSize()
  {
    diskInfo=\$(findDiskInfoText)
    if [ -n "\$diskInfo" ]
    then
      getDiskInfo "\$diskInfo"
      retval=0
    else
      retval=2
    fi
    return \$retval
  }

  remediateAndClean(){
    $sudoCommand rm -rf "\$directoryPath/mangleDumpFile.txt" >/dev/null 2>&1
    rm -rf $basedirectory/diskspacescript.sh >/dev/null 2>&1
    rm -rf $basedirectory/diskSpaceFault.log >/dev/null 2>&1
    rm -rf $basedirectory/diskspace.sh >/dev/null 2>&1
  }

  #calling function to get disk info
  getDiskSize
  retval=\$?
  if [ \$retval -eq 0 ]
  then
    echo "Free Disk Space is : \${availSize}"
    if [ -z \$availSize ]
    then
      echo "The user specified disk is already Full"
      exit $errorExitCode
    else
      echo "Injecting Disk Space fault"
      if [ ! -z \$diskFillSize -a \$diskFillSize -ne 100 ]; then
        diskFillSize=\`expr \$diskFillSize - \$usePercentage\`
        diskFill=\$(awk "BEGIN { diskSize=\${diskFillSize}*\${totalSize}/100; i=int(diskSize); print (diskSize-i<0.5)?i:i+1 }")
        echo "diskFill: \${diskFill}"
        count=\$(awk "BEGIN { ct=\${diskFill}/1024; i=int(ct); print (ct-i<0.5)?i:i+1 }")
        echo "count: \${count}"
        $sudoCommand dd if=/dev/zero of="\$directoryPath/mangleDumpFile.txt" oflag=append bs=1MB count=\$count conv=notrunc
        sleep \$timeout
        remediateAndClean
      else
        $sudoCommand dd if=/dev/zero of="\$directoryPath/mangleDumpFile.txt" oflag=append bs=1GB conv=notrunc
        sleep \$timeout
        remediateAndClean
      fi
      exit 0
    fi
  else
    echo "The Provided directory is not found : \${directoryPath}"
    exit $errorExitCode
  fi
EOF
  chmod 777 $basedirectory/diskspacescript.sh
  sh $basedirectory/diskspacescript.sh $directoryPath $timeout $diskFillSize >$basedirectory/diskSpaceFault.log 2>&1 &
  echo "Triggered Disk Space fault"
  exit 0
}

#calling main function
main $@
