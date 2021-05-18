# jl4fcloud
Cетевое хранилище.

### Клиент 
Написан на JavaFX. Синхронизирует содержимое локального каталога на компьютере пользователя, с файлами находящимися на сервере.
### Сервер
Хранит файлы пользователя в папках на сервере. Осуществляет аутентификацию и регистрацию пользователей на сервере

### Сборка проекта
Выполнить в панели Maven -> j4cloud-all -> Lifecycle-> package. После успешного завершения задания maven, в папках target в каждом модуле появятся следующие jar архивы:<br/> 
<br/>***Клиент***<br/>
- netty-client\target\netty-client-1.0-SNAPSHOT-spring-boot.jar<br/>

<br/>***Сервер***<br/>  
- netty-server\target\netty-server-1.0-SNAPSHOT-jar-with-dependencies.jar<br/>

### Запуск сервера
При запуске сервера необходимо задать следующие параметры:  
- -port=<номер порта>  
- -storage=<путь до каталога в котором будут находится файлы>  
        Если указанная папка не существует, она создается автоматически.  
- -db=<полный путь до файла *.db>  
        Если файл базы данных не существует, то он создается автоматически. 
> Пример:
>    
>-port=8989 -storage=e:\temp\store -db=e:\temp\db\cloud.db

### Запуск клиента
При запуске клиента необходимо задать следующие параметры:  
- -port=<номер порта>  
- -host=<имя сервера>
> Пример:
>    
>-port=8989 -host=localhost

Примеры скриптов запуска клиента и сервера можно посмотреть в корне проекта: client.bat, server.bat

