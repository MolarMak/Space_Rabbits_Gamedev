package repositories

import datatables.OnlineGameTable
import entities.OnlineGame
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class OnlineGameRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[OnlineGame](OnlineGameTable.table) {

  def searchFreeGameSpot: Future[Option[OnlineGame]] =
    db.run(
      OnlineGameTable.table
        .filter(_.player2Id.isEmpty)
        .result
        .headOption
    )

  def joinToGame(gameId: Int, userId: Int): Future[Int] =
    db.run(
      OnlineGameTable.table
        .filter(_.id === gameId)
        .filter(_.player2Id.isEmpty)
        .map(_.player2Id)
        .update(Some(userId))
    )

}
