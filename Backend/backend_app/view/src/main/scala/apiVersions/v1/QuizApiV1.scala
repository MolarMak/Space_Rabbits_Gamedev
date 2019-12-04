package apiVersions.v1

import akka.http.scaladsl.server.Route
import view.QuizApiTrait
import slick.jdbc.PostgresProfile.backend.Database


class QuizApiV1(private val db: Database) extends QuizApiTrait {

  private val authView = new AuthView(db)
  private val factView = new FactView(db)
  private val profileView = new ProfileView(db)

  override def login: Route = authView.login

  override def register: Route = authView.register

  override def logout: Route = authView.logout

  override def synchronizeFacts: Route = factView.synchronizeFacts

  override def loadScores: Route = profileView.loadScores

}
