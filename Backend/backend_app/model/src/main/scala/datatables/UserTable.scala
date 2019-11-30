package datatables

import entities.User
import repositories.BaseTable
import slick.jdbc.PostgresProfile.api._

class UserTable(tag: Tag) extends Table[User](tag, "user") with BaseTable[User] {
  val id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
  val login = column[String]("login")
  val password = column[String]("password")
  val token = column[String]("token")

  def * = (id, login, password, token).mapTo[User]
}

object UserTable {
  val table = TableQuery[UserTable]
}