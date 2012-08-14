## CoRepository in a nutshell
![concept](http://good-samples.googlecode.com/files/corepository-concept.gif)

CoRepository is a assistance repository that handles UPDATE/SELECT requests instead of DB. With the CoRepository, you can expect to reduce the load of DB and to improve the response time of transaction. Also, you don`t need to change DB or migrate old data to new storage. The CoRepository ensures synchronization with DB by write-back mechanism.

In fact, the CoRepository is a simple client library. So you need to ready your physical repository. The CoRepository enable to use well known NoSQL storages as rapid repository. Now, the CoRepository provides Redis, Tokyo Tryant, LocalMemory. Hazelcast is considering as new repository. To use this, all that you have to is to choose your physical repository and pass information about it when creating your CoRepositoryClient object. 

Following situations would be good for applying the CoRepository.

**1) You need to reduce the load of your operating DB**

You can reduce the load of your operating DB by delegating frequent UPDATE/SELECT requests to the CoRepository.

**2) You need to optimize the response time of transaction. For example, you are developing web-game server api(s) that interacts with app in real-time even though you uses DB as major repository.**

The reponse time can be optimized with the CoRepository. Following picture is a graph that shows total response time when many requests are given to Tokyo Tyrant CoRepository, Redis Corepository and MySQL DB. For testing, assume there are 1000 concurrency users that increase K1 and select K1 value again.

![ttvsdb](http://good-samples.googlecode.com/files/tt-vs-db4.gif)

## Working example
https://github.com/MinCha/corepository-example 
