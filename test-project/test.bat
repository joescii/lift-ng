@echo off

set SBT=sbt
set tasks=update
set el=0

call %SBT% -Dlift.version=2.5.1 -Dscala.version=2.10.4 %tasks%
set el=%errorlevel%
if %el% NEQ 0 goto EXIT

call %SBT% -Dlift.version=2.6-RC1 -Dscala.version=2.10.4 %tasks%
set el=%errorlevel%
if %el% NEQ 0 goto EXIT

call %SBT% -Dlift.version=2.6-RC1 -Dscala.version=2.11.2 %tasks%
set el=%errorlevel%
if %el% NEQ 0 goto EXIT

call %SBT% -Dlift.version=3.0-SNAPSHOT -Dscala.version=2.11.2 %tasks%
set el=%errorlevel%
if %el% NEQ 0 goto EXIT

:EXIT
exit /b %el%