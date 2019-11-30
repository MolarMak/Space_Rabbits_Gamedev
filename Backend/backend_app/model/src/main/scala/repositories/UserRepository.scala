package repositories

import datatables.UserTable
import entities.User
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(val db: Database)(implicit ec: ExecutionContext) extends BaseRepository[User](UserTable.table) {

  def checkAccount(login: String, password: String): Future[Option[String]] =
    db.run(
      UserTable.table
        .filter(_.login === login)
        .filter(_.password === password)
        .map(it => it.token)
        .result
        .headOption
    )

  def checkExists(login: String, password: String): Future[Boolean] =
    db.run(
      UserTable.table
        .filter(_.login === login)
        .filter(_.password === password)
        .exists
        .result
    )
}
