package models.v2

import entities.Fact
import io.circe.generic.JsonCodec

@JsonCodec
case class FactTrueFalse(fact: Fact, useTrueQuestion: Boolean)

@JsonCodec
case class OnlineRoomIdData(roomId: String)

@JsonCodec
case class StartGameResponse(result: Boolean = true, data: OnlineRoomIdData)

/**
  * @param gameRoomId - unique generated String value
  * @param player1Name - User1 nickname
  * @param player2Name - User2 nickname
  * @param questionsList - Array of questions
  * @param answersPlayer1List - Json to String object of array of booleans (0 - unfinished, 1 - right, 2 - wrong)
  * @param answersPlayer2List - Json to String object of array of booleans (0 - unfinished, 1 - right, 2 - wrong)
  */
@JsonCodec
case class OnlineGameData(gameRoomId: String, player1Name: Option[String], player2Name: Option[String], questionsList: List[FactTrueFalse], answersPlayer1List: List[Int], answersPlayer2List: List[Int])

@JsonCodec
case class OnlineGameResponse(result: Boolean = true, data: OnlineGameData)

@JsonCodec
case class PutAnswerRequest(gameRoomId: String, answerNumber: Int, answer: Int)
