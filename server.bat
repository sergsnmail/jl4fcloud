@echo off

 SET JAVA_HOME=c:\java\corretto-11.0.10
 SET SERVER_JAR=d:\JAVA\WORKSPACE\IdeaProjects\Netty\netty_test_1\netty-server\target\netty-server-1.0-SNAPSHOT-jar-with-dependencies.jar
 %JAVA_HOME%\bin\java.exe -jar %SERVER_JAR% -port=8989 -storage=e:\temp\store -db=e:\temp\db\cloud.db