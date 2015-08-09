#!/bin/bash

TASKS="clean update test"
MULLIGANS=5

run_tests() {
  run_tests_rec $1 $2 $MULLIGANS
  status=$?
  echo "$1/$2 testing complete"
  if [ $status -ne 0 ]
  then
    exit $status
  fi
}

run_tests_rec () {
  lift=$1
  scala=$2
  mulligans=$3

  sbt -Dlift.version=$lift -Dscala.version=$scala $TASKS
  status=$?

  if [ $status -ne 0 -a $mulligans -gt 0 ]
  then
    mulligans=`expr $mulligans - 1`
    run_tests_rec $lift $scala $mulligans
    status=$?
  fi

  return $status
}

run_tests "3.0-M4-1" "2.11.7"
run_tests "2.6.2" "2.11.7"
run_tests "2.6.2" "2.10.5"
run_tests "2.5.3" "2.10.5"

exit $?