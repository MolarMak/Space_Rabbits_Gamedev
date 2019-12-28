package models.v2

import entities.Fact
import io.circe.generic.JsonCodec

@JsonCodec
case class FactTrueFalse(fact: Fact, useTrueQuestion: Boolean)

@JsonCodec
case class StartGameResponse(result: Boolean = true, roomId: String)

/**
  * @param gameRoomId - unique generated String value
  * @param player1Name - User1 nickname
  * @param player2Name - User2 nickname
  * @param questionsList - Array of questions
  * @param answersPlayer1List - Json to String object of array of booleans (0 - unfinished, 1 - right, 2 - wrong)
  * @param answersPlayer2List - Json to String object of array of booleans (0 - unfinished, 1 - right, 2 - wrong)
  */
@JsonCodec
case class OnlineGameResponse(gameRoomId: String, player1Name: Option[String], player2Name: Option[String], questionsList: List[FactTrueFalse], answersPlayer1List: List[Int], answersPlayer2List: List[Int])