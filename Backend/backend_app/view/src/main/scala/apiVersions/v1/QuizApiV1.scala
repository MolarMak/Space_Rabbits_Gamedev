package apiVersions.v1

import akka.http.scaladsl.server.Route
import view.QuizApiTrait
import slick.jdbc.PostgresProfile.backend.Database


class QuizApiV1(private val db: Database) extends QuizApiTrait {

  val authView = new AuthView(db)

  def login: Route = authView.login

  def register: Route = authView.register

}
