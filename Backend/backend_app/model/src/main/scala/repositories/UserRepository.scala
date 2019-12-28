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

  def logoutByToken(oldToken: String, newToken: String): Future[Int] =
    db.run(
      UserTable.table
        .filter(_.token === oldToken)
        .map(_.token)
        .update(newToken)
    )

  def getUserIdByToken(token: String): Future[Option[Int]] =
    db.run(
      UserTable.table
        .filter(_.token ===token)
        .map(_.id)
        .result
        .headOption
    )

  def getUserProfileDataByToken(token: String): Future[Option[String]] =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .map(_.login)
        .result
        .headOption
    )

  def checkToken(token: String): Future[Boolean] =
    db.run(
      UserTable.table
        .filter(_.token === token)
        .exists
        .result
    )

  def getUserNameById(id: Option[Int]) : Future[Option[String]] =
    db.run(
      UserTable.table
        .filter(_.id === id)
        .map(_.login)
        .result
        .headOption
    )
}
