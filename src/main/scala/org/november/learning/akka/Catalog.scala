package org.november.learning.akka

/**
  * Mock up a persistent datasource of card catalogs, an in-memory representation of the card catalog
  * as a singleton Map
  */
object Catalog {

  import org.november.learning.akka.RareBooksProtocol._

  val phaedrus = BookCard(
    "0872202208",
    "Plato",
    "Phaedrus",
    "Plato's enigmatic text that treats a range of important philosophical issues.",
    "370 BC",
    Set(Greece, Philosophy),
    "Hackett Publishing Company, Inc.",
    "English",
    144)

  val theEpicOfGilgamesh = BookCard(
    "0141026286",
    "unknown",
    "The Epic of Gilgamesh",
    "A hero is created by the gods to challenge the arrogant King Gilgamesh.",
    "2700 BC",
    Set(Gilgamesh, Persia, Royalty),
    "Penguin Classics",
    "English",
    80)


  val theHistories = BookCard(
    "0140449086",
    "Herodotus",
    "The Histories",
    "A record of ancient traditions of Western Asia, Northern Africa and Greece.",
    "450 to 420 BC",
    Set(Africa, Asia, Greece, Tradition),
    "Penguin Classics",
    "English",
    771)

  /**
    * Map containing book cards.
    */
  val books: Map[String, BookCard] = Map(
    phaedrus.isbn -> phaedrus,
    theEpicOfGilgamesh.isbn -> theEpicOfGilgamesh,
    theHistories.isbn -> theHistories)
}
