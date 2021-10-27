@echo off
rem Copyright 2021-2021 Monkey Group.
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
rem added double quotation marks to avoid the issue caused by the folder names containing spaces.
rem removed the last 5 chars(which means \bin\) to get the base DIR.
set BASE_DIR="%BASE_DIR:~0,-5%"

set CUSTOM_SEARCH_LOCATIONS=file:%BASE_DIR%/conf/

set MODE="cluster"
set FUNCTION_MODE="all"
set SERVER=mmq-server
set MODE_INDEX=-1
set FUNCTION_MODE_INDEX=-1
set SERVER_INDEX=-1
set EMBEDDED_STORAGE_INDEX=-1
set EMBEDDED_STORAGE=""


set i=0
for %%a in (%*) do (
    if "%%a" == "-m" ( set /a MODE_INDEX=!i!+1 )
    if "%%a" == "-f" ( set /a FUNCTION_MODE_INDEX=!i!+1 )
    if "%%a" == "-s" ( set /a SERVER_INDEX=!i!+1 )
    if "%%a" == "-p" ( set /a EMBEDDED_STORAGE_INDEX=!i!+1 )
    set /a i+=1
)

set i=0
for %%a in (%*) do (
    if %MODE_INDEX% == !i! ( set MODE="%%a" )
    if %FUNCTION_MODE_INDEX% == !i! ( set FUNCTION_MODE="%%a" )
    if %SERVER_INDEX% == !i! (set SERVER="%%a")
    if %EMBEDDED_STORAGE_INDEX% == !i! (set EMBEDDED_STORAGE="%%a")
    set /a i+=1
)

rem if mmq startup mode is standalone
if %MODE% == "standalone" (
    echo "mmq is starting with standalone"
	  set "MMQ_OPTS=-Dmmq.standalone=true"
    set "MMQ_JVM_OPTS=-Xms512m -Xmx512m -Xmn256m"
)

rem if mmq startup mode is cluster
if %MODE% == "cluster" (
    echo "mmq is starting with cluster"
	  if %EMBEDDED_STORAGE% == "embedded" (
	      set "MMQ_OPTS=-DembeddedStorage=true"
	  )

    set "MMQ_JVM_OPTS=-server -Xms2g -Xmx2g -Xmn1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%BASE_DIR%\logs\java_heapdump.hprof -XX:-UseLargePages"
)

rem set mmq options
set "MMQ_OPTS=%MMQ_OPTS% -Dmmq.home=%BASE_DIR% -Duser.timezone=GMT+08"
set "MMQ_OPTS=%MMQ_OPTS% -jar %BASE_DIR%\target\%SERVER%.jar"

rem set mmq spring config location
set "MMQ_CONFIG_OPTS=--spring.config.additional-location=%CUSTOM_SEARCH_LOCATIONS%"

rem set mmq log4j file location
set "MMQ_LOG4J_OPTS=--logging.config=%BASE_DIR%/conf/mmq-logback.xml"


set COMMAND="%JAVA%" %MMQ_JVM_OPTS% %MMQ_OPTS% %MMQ_CONFIG_OPTS% %MMQ_LOG4J_OPTS% mmq.mmq %*

rem start mmq command
%COMMAND%
