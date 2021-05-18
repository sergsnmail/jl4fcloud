@echo off

 SET JAVA_HOME=c:\java\corretto-11.0.10
 SET CLIENT_JAR=d:\JAVA\WORKSPACE\IdeaProjects\Netty\netty_test_1\netty-client\target\netty-client-1.0-SNAPSHOT-spring-boot.jar
 %JAVA_HOME%\bin\java.exe -jar %CLIENT_JAR% -port=8989 -host=localhost