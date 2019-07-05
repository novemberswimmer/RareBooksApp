package org.november.learning.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}

import scala.concurrent.duration.FiniteDuration

object Librarian {

  import Catalog._
  import RareBooksProtocol._

  final case class Done(e: Either[BookNotFound, BookFound], customer: ActorRef)

  def props(findBookDuration: FiniteDuration): Props =
    Props(new Librarian(findBookDuration))

  /**
    * Convert option to either function.
    *
    * Uses Either class as return type since there are two possible outcome of calling this function.  The operation
    * either produces a result or doesn't and you want to return a different message in response. Use Either to
    * provide a richer, self-contained message rather than a simple failure or empty message.
    * NOTE: The two possible return type of Either is of the same type that extends the RareBooksProtocol.Msg trait
    *
    * @param v input for function
    * @param f function to match against
    * @tparam T type for Either
    * @return on success return Right[BookFound] otherwise return Left[BookNotFound]
    */
  private def optToEither[T](v: T, f: T => Option[List[BookCard]]): Either[BookNotFound, BookFound] =
    f(v) match {
      case b: Some[List[BookCard]] => Right(BookFound(b.get))
      case _ => Left(BookNotFound(s"Book(s) not found based on $v"))
    }

  /**
    * Convert option to either for validation. Calls the findBookByIsbn method in Catalog singleton object
    *
    * @param fb find book command
    * @return either list of books or error
    */
  private def findByIsbn(fb: FindBookByIsbn) =
    optToEither[String](fb.isbn, findBookByIsbn)

  /**
    * Convert option to either for validation.
    *
    * @param fb find book command
    * @return either list of books or error
    */
  private def findByAuthor(fb: FindBookByAuthor) =
    optToEither[String](fb.author, findBookByAuthor)

  /**
    * Convert option to either for validation.
    *
    * @param fb find book command
    * @return either list of books or error
    */
  private def findByTitle(fb: FindBookByTitle) =
    optToEither[String](fb.title, findBookByTitle)

  /**
    * Convert option to either for validation.
    *
    * @param fb find book command
    * @return either list of books or error
    */
  private def findByTopic(fb: FindBookByTopic) =
    optToEither[Set[Topic]](fb.topic, findBookByTopic)


}

/*
There are are several methods with return type Receive.  This methods are use to defining State/Behavior of the Actor
In the case of Librarian actor there are couple or State
'ready'
'busy'
Actors can only have one State/Behavior at a time use context.become function to switch state
 */
class Librarian(findBookDuration: FiniteDuration) extends Actor with ActorLogging with Stash {

  import Librarian._
  import RareBooksProtocol._
  import context.dispatcher

  /**
    * Set the initial behavior.
    *
    * @return partial function ready
    */
  override def receive: Receive = ready

  /**
    * Behavior when ready to receive a find book request
    *
    * @return partial function for completing the request
    */
  private def ready: Receive = {
    case m: Msg => m match {
      case c: Complain =>
        sender ! Credit()
        log.info(s"Credit issued to customer $sender()")
      case f: FindBookByIsbn =>
        research(Done(findByIsbn(f), sender()))
      case f: FindBookByAuthor =>
        research(Done(findByAuthor(f), sender()))
      case f: FindBookByTitle =>
        research(Done(findByTitle(f), sender()))
      case f: FindBookByTopic =>
        research(Done(findByTopic(f), sender()))
    }
  }

  /**
    * Behavior simulating the need research the customer's request.
    * From busy state the actor goes back to ready state by calling unbecome function. when using unbecome ensure
    * that previous state was not discarded by specifying discardOld=false attribute of context.become
    *
    * NOTE: notice that when the state is busy all other incoming message besides Done is being stash.  This
    * effectively simulates 'blocking' the Librarian from processing any other message until it is done with the current one.
    * x
    *
    * @return partial function for completing request
    */
  private def busy: Receive = {
    case Done(e, s) =>
      process(e, s)
      unstashAll()
      context.unbecome()
    case _ =>
      stash()
  }

  /**
    * Simulate researching for information by scheduling completion of the task for a given duration.  When
    * the librarian starts a research it changes the actor behavior (implmentation of received method) to busy
    * using context.become.
    * By specifying discardOld=false parameter of context.become AKKA then DO NOT discards the previous state,
    * this allows you to switch back to a previous state (ready) by calling unbecome
    *
    * @param d simulation is done
    */
  private def research(d: Done): Unit = {
    context.system.scheduler.scheduleOnce(findBookDuration, self, d)
    context.become(busy, discardOld = false)
  }

  /**
    * Process messages for finding books by folding over either
    *
    * @param r on success return BookFound otherwise return BookNotFound
    */
  private def process(r: Either[BookNotFound, BookFound], sender: ActorRef): Unit = {
    r fold(
      f => {
        sender ! f
        log.info(f.toString)
      },
      s => sender ! s)
  }
}