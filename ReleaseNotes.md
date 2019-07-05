# RareBooksApp

## 4.3.2
* Created RareBooksProtocol Scala object that defines all protocols that will
be shared by several actors
* RareBooks AKKA actor that represents the RareBook shop.  
    * Within the actor definition are several features
        * Reading a value from a configuration file ```application.conf```
        * Sending self(RareBooks) a message after certain schedule using 
        ```scala
        context.system.scheduler.scheduleOnce
        ```
        * Change default behavior (definition of received method) depending
        on certain state using 
        ```scala
        context.become
        ```
        * Saves messages for later processing and re-send messages previously save using 
        ```scala
        stash() and unstashAll()
        ```
* Catalog a singleton Scala object that mock-up a DAO/Persistence layer
* ``applicatio.conf`` used to hold application specific properties