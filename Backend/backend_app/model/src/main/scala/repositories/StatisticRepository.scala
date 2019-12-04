package repositories

import datatables.{StatisticTable, UserTable}
import entities.Statistic
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class StatisticRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[Statistic](StatisticTable.table) {

  def getUserStatistic(token: String): Future[Option[Statistic]] =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .join(StatisticTable.table)
        .on(_.id === _.userId)
        .map(_._2)
        .result
        .headOption
    )

  def updateOffline(statisticId: Int, offline: Int): Future[Int] =
    db.run(
      StatisticTable.table
        .filter(_.id === statisticId)
        .map(_.offline)
        .update(offline)
    )

}
