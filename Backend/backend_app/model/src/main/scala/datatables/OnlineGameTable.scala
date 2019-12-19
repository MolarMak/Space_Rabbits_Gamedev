package datatables

import entities.OnlineGame
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class OnlineGameTable(tag: Tag) extends Table[OnlineGame](tag, "online_game") with BaseTable[OnlineGame] {
  val id = column[Int]("online_game_id", O.PrimaryKey)
  val gameRoomId = column[String]("game_room_id")
  val player1Id = column[Option[Int]]("player_one_id")
  val player2Id = column[Option[Int]]("player_two_id")
  val questionsList = column[String]("questions_list_object")
  val answersPlayer1List = column[String]("answers_player_one_list_object")
  val answersPlayer2List = column[String]("answers_player_two_list_object")
  val isGameStarted = column[Boolean]("is_game_started")
  val winner = column[Int]("winner")

  val player1IdForeignKey = foreignKey(
    "player_one_id_fk", player1Id, UserTable.table)(
    _.id.?, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  val player2IdForeignKey = foreignKey(
    "player_two_id_fk", player2Id, UserTable.table)(
    _.id.?, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  def * = (id, gameRoomId, player1Id, player2Id, questionsList, answersPlayer1List, answersPlayer2List, isGameStarted, winner) <> ((OnlineGame.apply _).tupled, OnlineGame.unapply)

}

object OnlineGameTable {
  val table = TableQuery[OnlineGameTable]
}
