package controllers.v2

import akka.http.scaladsl.server.Route
import controllers.ViewTrait
import slick.jdbc.PostgresProfile.backend.Database

trait OnlineGameControllerTrait {
  def startOnlineGameRoute(token: String) : Route
}

trait OnlineGameViewTrait extends ViewTrait {
  def onStartOnlineGame(gameRoomId: String) : Route
}

class OnlineGameController(private val db: Database) {

}
