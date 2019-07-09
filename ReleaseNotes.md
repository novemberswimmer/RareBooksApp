# RareBooksApp

## 4.4 Increasing elasticity
* Added pool router (programmatically) to RareBooks when it's creating Librarian Actor
* Number of routees is property driven
* Updated private method that create librarian actors
## 4.3.4
* Copy implementation of Customr actor form [here](https://github.com/ironfish/reactive-application-development-scala)
* Customer research requests to RareBooks, and the requests are forwarded to individual Librarians 
* Customer have states but since the implementaion CustomerModel is a case class which is immutable a pattern
of using copy method to return a new instance of CustomerModel (with fields updated accordingly) is returned.                                    
                                         
## 4.3.3
* Completed thru copy and pasting orginal implementation of RareBooksApp as found 
[here](https://github.com/ironfish/reactive-application-development-scala)

* Demonstrate the use of Either class to elegantly write a method with two possible 
outcome
* Implemented a pattern of Actor stackable states by combining the use of:
    * context.become with discardOld=false attribute and context.unbecome
    * stashing and unstashing of message
* Demonstrate the use of stackable state as an solution to a scenario where an actor
is executing a blocking call

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