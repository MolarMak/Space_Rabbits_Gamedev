package apiVersions.v1

import akka.http.scaladsl.server.Route
import apiVersions.v2.OnlineGameView
import main.QuizApiTrait
import slick.jdbc.PostgresProfile.backend.Database


class QuizApiV1(private val db: Database) extends QuizApiTrait {

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

  override def onlineGameRoomInfo: Route = onlineGameView.onlineGameRoomInfo

  override def wsEcho: Route = onlineGameView.wsEcho
}
