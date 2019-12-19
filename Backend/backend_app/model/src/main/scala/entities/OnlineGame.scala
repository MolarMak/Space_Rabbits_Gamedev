package entities

import io.circe.generic.JsonCodec
import repositories.HasId

/**
  *
  * @param id - unique id in database
  * @param gameRoomId - unique generated String value
  * @param player1Id - Relation 1 to user table
  * @param player2Id - Relation 2 to user table (None if free)
  * @param questionsList - Json to String object of Fact (cast of facts)
  * @param answersPlayer1List - Json to String object of array of booleans (0 - unfinished, 1 - right, 2 - wrong)
  * @param answersPlayer2List - Json to String object of array of booleans (0 - unfinished, 1 - right, 2 - wrong)
  * @param winner - 0 is unfinished 1 is first player, 2 is second player, 3 is draw
  */
@JsonCodec
case class OnlineGame(id: Int, gameRoomId: String, player1Id: Option[Int], player2Id: Option[Int], questionsList: String, answersPlayer1List: String, answersPlayer2List: String, isGameStarted: Boolean, winner: Int) extends HasId
