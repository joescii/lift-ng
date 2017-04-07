#!/bin/bash

PUBLISH=publishSigned

publish() {
  LIFT_VERSION="set liftVersion in ThisBuild := \"$1\""
  CROSS_SCALA="set crossScalaVersions := Seq($2)"

  sbt "$LIFT_VERSION" "$CROSS_SCALA" "+ update" "+ test" "+ $PUBLISH"
}

sbt clean jasmine

publish "3.1.0-M2" '"2.11.8"'
publish "3.0.1" '"2.11.8"'
publish "2.6.3" '"2.11.8", "2.10.5"'
publish "2.5.4" '"2.10.5"'
