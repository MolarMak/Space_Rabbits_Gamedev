package apiVersions.v2

import akka.http.scaladsl.server.Route
import main.QuizApiTrait
import slick.jdbc.PostgresProfile.backend.Database

class QuizApiV2(private val db: Database) extends QuizApiTrait {

  private val authView = new AuthView(db)
  private val factView = new FactView(db)
  private val profileView = new ProfileView(db)
  private val onlineGameView = new OnlineGameView(db)

  override def login: Route = authView.login

  override def register: Route = authView.register

  override def logout: Route = authView.logout

  override def synchroniseFacts: Route = factView.synchroniseFacts

  override def loadScores: Route = profileView.loadScores

  override def onlineGameRoom: Route = onlineGameView.onlineGameRoom
}
