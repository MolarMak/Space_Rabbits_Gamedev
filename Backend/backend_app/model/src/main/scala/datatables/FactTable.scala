package datatables

import entities.Fact
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class FactTable(tag: Tag) extends Table[Fact](tag, "fact") with BaseTable[Fact] {
  val id = column[Int]("fact_id", O.PrimaryKey)
  val fact = column[String]("fact")
  val trueFact = column[String]("true_fact")
  val falseFact = column[String]("false_fact")
  val factVersion = column[Int]("fact_version")

  def * = (id, fact, trueFact, falseFact, factVersion)<> ((Fact.apply _).tupled, Fact.unapply)
}

object FactTable {
  val table = TableQuery[FactTable]
}
