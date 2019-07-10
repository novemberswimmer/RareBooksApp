package org.november.learning.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.util.Random

object Customer {

  import RareBooksProtocol._

  def props(rareBooks: ActorRef, odds: Int, tolerance: Int): Props =
    Props(new Customer(rareBooks, odds, tolerance))

  /**
    * Customer model.
    * @param odds the customer's odds of finding a book
    * @param tolerance the customer's tolerance for BookNotFound
    * @param found the number of books found
    * @param notFound the number of books not found
    */
  case class CustomerModel(
                            odds: Int,
                            tolerance: Int,
                            found: Int,
                            notFound: Int)

  /**
    * Immutable state structure for customer model.  Since customer model is immutable and at the same time holds state
    * that needs to be updated the use of <b>copy</b> method to return a new version/copy of a customer model every time a new
    * a field as part of the status
    *
    * @param model updated customer model
    * @param timeInMillis current time in milliseconds
    */
  private case class State(model: CustomerModel, timeInMillis: Long) {
    //method that produces a new state based on the current state and received message
    def update(m: Msg): State = m match {
        //the copy method in scala lets you make a copy of an object, where "copy" is different than a clone, because the
        //with a copy you can change the fields as desired during the copying process.
        //Adds the number of books that we found
      case BookFound(b, d)    => copy(model.copy(found = model.found + b.size), timeInMillis = d)
        //increments the count of unsuccessful request
      case BookNotFound(_, d) => copy(model.copy(notFound = model.notFound + 1), timeInMillis = d)
        //resets the count to 0
      case Credit(d)          => copy(model.copy(notFound = 0), timeInMillis = d)
    }
  }
}

/**
  * Customer actor.
  *
  * @param rareBooks reference to rareBooks actor
  */
class Customer(rareBooks: ActorRef, odds: Int, tolerance: Int) extends Actor with ActorLogging {

  import Customer._
  import RareBooksProtocol._

  // initialize the customer state
  // because state needs to be updated it is declared as var instead of val
  private var state = State(CustomerModel(odds, tolerance, 0, 0), -1L)

  // bootstrap customer requests
  // Sends initial request to start the message flow
  requestBookInfo()

  override def receive: Receive = {
    //Gate to ensure that only protocol messages are process
    case m: Msg => m match {
      case f: BookFound =>
        state = state.update(f)
        log.info(f"{} Book(s) found!", f.books.size)
        requestBookInfo()
        // demonstrate use of Match Guard.  Match is made only if the pattern matches and the guard condition is true
      case f: BookNotFound if state.model.notFound < state.model.tolerance =>
        state = state.update(f)
        log.info(f"{} Book(s) not found! My tolerance is {}.", state.model.notFound, state.model.tolerance)
        requestBookInfo()
      case f: BookNotFound =>
        state = state.update(f)
        sender ! Complain()
        log.info(f"{} Book(s) not found! Reached my tolerance of {}. Sent complaint!",
          state.model.notFound, state.model.tolerance)
      case c: Credit =>
        state = state.update(c)
        log.info("Credit received, will start requesting again!")
        requestBookInfo()
      case g: GetCustomer =>
        sender ! state.model
    }
  }

  /**
    * Method for requesting book information by topic.
    */
  private def requestBookInfo(): Unit =
    rareBooks ! FindBookByTopic(Set(pickTopic))

  /**
    * Simulate customer picking topic to request. Based on the odds they will randomly pick a viable topic, otherwise
    * they will request Unknown to represent a topic that does not exist.
    *
    * @return topic for book information request
    */
  private def pickTopic: Topic = {
    val randomInt = Random.nextInt(4)
    val stateModelOdds = state.model.odds
    val randomizedViableTopic = viableTopics(Random.nextInt(viableTopics.size))
    log.info(s"randomInt = ${randomInt} ::: stateModelOdds ${stateModelOdds} ::: randomizedViableTopic ${randomizedViableTopic}")
    if ( randomInt < stateModelOdds) randomizedViableTopic else Unknown
  }

}