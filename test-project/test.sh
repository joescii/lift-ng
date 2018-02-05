#!/bin/bash

ARGS="-Dnet.liftmodules.ng.test.browser=chrome -Dwebdriver.chrome.driver=$HOME/tools/selenium/chromedriver"
TASKS="clean update test"
MULLIGANS=3

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

  sbt -Dlift.version=$lift -Dscala.version=$scala $ARGS $TASKS
  status=$?

  # Kill any zombie chromedrivers
  ps -ef | grep chromedriver | awk '{print $2}' | xargs kill -9

  if [ $status -ne 0 -a $mulligans -gt 0 ]
  then
    mulligans=`expr $mulligans - 1`
    run_tests_rec $lift $scala $mulligans
    status=$?
  fi

  return $status
}

run_tests "3.2.0" "2.12.4"
run_tests "3.2.0" "2.11.12"
run_tests "3.1.0" "2.12.4"
run_tests "3.1.0" "2.11.12"
run_tests "3.0.1" "2.12.4"
run_tests "3.0.1" "2.11.12"
run_tests "2.6.3" "2.11.12"

exit $?
