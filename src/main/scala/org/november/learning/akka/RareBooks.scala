package org.november.learning.akka

import akka.actor.{Actor, ActorLogging, Props, Stash}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import org.november.learning.akka.RareBooks.{Close, Open, Report}
import org.november.learning.akka.RareBooksProtocol.Msg

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS => Millis}

/**
  * Stash trait allows the Actor save message that it can't handle because of its current state.  When the actor
  * switches state again , calling unstashAll causes all the previously stashed messages to be submitted again.
  *
  * Top-level actor representing the owner of Rarebooks business
  */
object RareBooks {

  //Protocol that use to change the state of RareBooks actor.
  case object Open

  case object Close

  case object Report

  def props: Props = Props(new RareBooks)
}

class RareBooks extends Actor with ActorLogging with Stash {

  implicit val ec: ExecutionContext = context.dispatcher

  //context.system.settings.config gives access to properties defined in applicationl.conf
  private val openDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.open-duration", Millis), Millis)

  private val closeDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.close-duration", Millis), Millis)

  private val findBookDuration: FiniteDuration =
    Duration(context.system.settings.config.getDuration("rare-books.librarian.find-book-duration", Millis), Millis)

  //Read integer value from property for the number of routees
  private val nbrOfLibrarians =
    context.system.settings.config.getInt("rare-books.nbr-of-librarians")

  private val router: Router = createLibrarian()

  //Running total use for generating report
  var requestsToday: Int = 0
  var totalRequests: Int = 0

  //Schedules to send the "Close"-message to the RareBooks actor after time specified in openDuration variable
  context.system.scheduler.scheduleOnce(openDuration, self, Close)

  //default receive method of Actor Trait
  override def receive: Receive = open //Begins in the open state

  //Function received that defines the behavior of RareBook if the current state is Open
  private def open: Receive = {
    case m: Msg =>
      requestsToday += 1
      //instead of the method forward router uses route method to "forward" a message to routee
      router.route(m, sender())
    case Close =>
      //when message to change state to Close schedule when to re-open
      context.system.scheduler.scheduleOnce(closeDuration, self, Open)

      //context.become replaces the actor's receive function this is use to change what the actor d
      // does when it receives a message
      context.become(closed)

      //RareBooks tell itself to run the report
      self ! Report
  }

  private def closed: Receive = {
    case Report =>
      totalRequests += requestsToday
      log.info(s"$requestsToday requests processed today. Total requests processed = $totalRequests")
      requestsToday = 0
    case Open =>
      //On Open state schedule when to close
      context.system.scheduler.scheduleOnce(openDuration, self, Close)
      //Unstash messages that arrived while the shop was closed
      unstashAll()

      //Set default behavior of actor to Open method
      context.become(open)
    case _ =>
      //Stashes other messages that arrive while the shop is closed
      stash()

  }

  /**
    * Updated from version version 4.3 this method now returns a Router rather than ActorRef
    * Pool Router in this example is created programmatically instead of configuration.
    *
    * @return
    */
  protected def createLibrarian(): Router = {
    var cnt: Int = 0
    val routees: Vector[ActorRefRoutee] = Vector.fill(nbrOfLibrarians) {
      var r = context.actorOf(Librarian.props(findBookDuration), s"librarian-$cnt")
      cnt += 1
      ActorRefRoutee(r)
    }

    Router(RoundRobinRoutingLogic(), routees)
  }
}
