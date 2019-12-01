package repositories

import datatables.FactTable
import entities.Fact
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class FactRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[Fact](FactTable.table) {

  def getAllGreaterThanVersionLimitBy(version: Int, offset: Int, limit: Int): Future[Vector[Fact]] =
    db.run(
      FactTable.table
        .filter(_.factVersion > version)
        .drop(offset)
        .take(limit)
        .to[Vector]
        .result
    )

}
