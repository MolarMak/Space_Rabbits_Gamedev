package datatables

import entities.Statistic
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class StatisticTable(tag: Tag) extends Table[Statistic](tag, "statistic") with BaseTable[Statistic] {
  val id = column[Int]("statistic_id", O.PrimaryKey, O.AutoInc)
  val online = column[Int]("online")
  val offline = column[Int]("offline")
  val userId = column[Int]("user_id")

  val userIdForeignKey = foreignKey(
    "user_id_fk", userId, UserTable.table)(
    _.id, ForeignKeyAction.Cascade, ForeignKeyAction.Cascade
  )

  def * = (id, online, offline, userId)<> ((Statistic.apply _).tupled, Statistic.unapply)
}

object StatisticTable {
  val table = TableQuery[StatisticTable]
}



