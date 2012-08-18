## CoRepository in a nutshell
![concept](http://good-samples.googlecode.com/files/corepository-concept-20120731.gif)

CoRepository is a assistance repository that handles UPDATE/SELECT requests instead of DB. With CoRepository, you can expect reducing the load of DB and improving the response time of transaction. Also, you don`t need to modify DB schema and migrate old data to CoRepository because the CoRepository ensures synchronization with DB by write-back mechanism.

In fact, CoRepository is a simple client library. So you need to ready your repository. CoRepository enables to use well known NoSQL storages. Now, CoRepository provides Redis, Tokyo Tryant, LocalMemory. Hazelcast is considering as new repository. To use CoRepository, all that you have to is to choose your repository and pass information about it when creating your CoRepositoryClient object. 

Following situations would be good for applying CoRepository.

**1) You need to reduce the load of your operating DB**

You can reduce the load of your operating DB by delegating frequent UPDATE/SELECT requests to CoRepository.

**2) You need to optimize the response time of transaction. For example, you are developing web-game server api(s) that interacts with app in real-time even though you already uses DB as major repository.**

The reponse time can be optimized with CoRepository. Following picture is a graph that shows total response time when many requests are given to Tokyo Tyrant CoRepository, Redis Corepository and MySQL DB. For testing, assume there are 1000 concurrency users that increase K1 and select K1 value again.

![ttvsdb](http://good-samples.googlecode.com/files/tt-vs-db4.gif)

## Working example
https://github.com/MinCha/corepository-example 
