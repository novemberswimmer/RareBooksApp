package org.november.learning.akka


/**
  * This file contains the shared data structures and messages for the rare books info service.
  *
  * "A protocol object contains the messages that are used within the actor system.  In computer science, a protocol is defined
  * as the rules to allow two entities to communicate with each other.  Actors can have dependency on the protocol without
  * having direct dependencies on one another."
  */
object RareBooksProtocol {

  sealed trait Topic

  case object Africa extends Topic

  case object Asia extends Topic

  case object Gilgamesh extends Topic

  case object Greece extends Topic

  case object Persia extends Topic

  case object Philosophy extends Topic

  case object Royalty extends Topic

  case object Tradition extends Topic

  case object Unknown extends Topic

  /**
    * Viable topics for book requests
    */
  val viableTopics: List[Topic] =
    List(Africa, Asia, Gilgamesh, Greece, Persia, Philosophy, Royalty, Tradition)

  /**
    * Card trait for book cards.
    */
  sealed trait Card {
    def title: String

    def description: String

    def topic: Set[Topic]
  }

  /**
    * Book card class.
    *
    * @param isbn         the book isbn
    * @param author       the book author
    * @param title        the book title
    * @param description  the book description
    * @param dateOfOrigin the book date of origin
    * @param topic        set of associated tags for the book
    * @param publisher    the book publisher
    * @param language     the language the book is in
    * @param pages        the number of pages in the book
    */
  final case class BookCard(
                             isbn: String,
                             author: String,
                             title: String,
                             description: String,
                             dateOfOrigin: String,
                             topic: Set[Topic],
                             publisher: String,
                             language: String,
                             pages: Int) extends Card

  /** trait for all messages. */
  trait Msg {
    def dateInMillis: Long
  }

}
