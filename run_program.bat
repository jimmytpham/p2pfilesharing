@echo off
setlocal

:: Step 1: Start CORBA Naming Service on default port
start "Name Service" cmd /k tnameserv
echo CORBA Naming Service Started

:: Wait for Naming Service to Start
timeout /t 5

:: Step 2: Start the File Sharing Server
echo Starting File Sharing Server...
start cmd /k java -cp "target/classes;lib/mysql-connector-j-8.2.0.jar" Server
timeout /t 3

:: Step 3: Start the Client GUI
echo Starting Client GUI...
start cmd /k java -cp "target/classes;lib/mysql-connector-j-8.2.0.jar" ClientGUI

echo All services started successfully!
pause
exit
