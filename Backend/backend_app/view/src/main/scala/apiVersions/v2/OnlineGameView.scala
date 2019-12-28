package apiVersions.v2

import akka.http.scaladsl.server.Route
import apiVersions.BaseView
import controllers.log
import controllers.v2.{OnlineGameController, OnlineGameControllerTrait, OnlineGameViewTrait}
import models.v2.{OnlineGameData, OnlineGameResponse, OnlineRoomIdData, StartGameResponse}
import slick.jdbc.PostgresProfile.backend.Database

class OnlineGameView(private val db: Database) extends BaseView with OnlineGameViewTrait {

  private val controller: OnlineGameControllerTrait = new OnlineGameController(db, this)

  def onlineGameRoom: Route = {
    path("api" / apiVersion / "onlineGameRoom") {
      get {
        headerValueByName("Authorization") { token =>
          log("onlineGameRoom", s"input: $token")
          controller.startOnlineGameRoute(token)
        }
      }
    }
  }

  def onlineGameRoomInfo: Route = {
    path("api" / apiVersion / "onlineGameRoomInfo") {
      get {
        headerValueByName("Authorization") { token =>
          parameter('gameRoomId.as[String]) { gameRoomId =>
            log("onlineGameRoomInfo", s"input: $token $gameRoomId")
            controller.getGameRoomInfoRoute(token, gameRoomId)
          }
        }
      }
    }
  }

  def wsEcho: Route = {
    path("api" / apiVersion / "wsEcho") {
      get {
        handleWebSocketMessages(controller.echoService)
      }
    }
  }

  override def onStartOnlineGame(gameRoomId: String) : Route = {
    complete(StartGameResponse(result = true, OnlineRoomIdData(gameRoomId)))
  }

  override def onLoadGameInfo(gameData: OnlineGameData): Route = {
    complete(OnlineGameResponse(result = true, gameData))
  }
}
