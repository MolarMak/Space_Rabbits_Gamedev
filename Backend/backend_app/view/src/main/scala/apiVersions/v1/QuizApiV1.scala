package apiVersions.v1

import akka.http.scaladsl.server.Route
import apiVersions.UpdateVersionView
import main.QuizApiTrait
import slick.jdbc.PostgresProfile.backend.Database


class QuizApiV1(private val db: Database) extends QuizApiTrait {

  private val authView = new AuthView(db)
  private val factView = new FactView(db)
  private val profileView = new ProfileView(db)

  private val updateVersionView = new UpdateVersionView()

  override def login: Route = authView.login

  override def register: Route = authView.register

  override def logout: Route = authView.logout

  override def synchroniseFacts: Route = factView.synchroniseFacts

  override def loadScores: Route = profileView.loadScores

  override def onlineGameRoom: Route = updateVersionView.updateVersion(apiVersions.v2.apiVersion)
}
