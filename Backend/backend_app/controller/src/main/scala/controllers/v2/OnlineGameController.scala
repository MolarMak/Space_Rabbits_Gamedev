package controllers.v2

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Flow
import controllers.{Errors, ViewTrait, errorLog, log, _}
import entities.{Fact, OnlineGame}
import repositories.{FactRepository, OnlineGameRepository, UserRepository}
import slick.jdbc.PostgresProfile.backend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import io.circe.syntax._

trait OnlineGameControllerTrait {
  def startOnlineGameRoute(token: String) : Route
  def echoService: Flow[Message, Message, _]
}

trait OnlineGameViewTrait extends ViewTrait {
  def onStartOnlineGame(gameRoomId: String) : Route
}

class OnlineGameController(private val db: Database, private val view: OnlineGameViewTrait) extends Directives with OnlineGameControllerTrait {

  private val userRepository = new UserRepository(db)
  private val factRepository = new FactRepository(db)
  private val onlineGameRepository = new OnlineGameRepository(db)
  val errors = new Errors("en")


  /** Create game room START **/
  override def startOnlineGameRoute(token: String): Route = {
    val tokenValidateFuture = userRepository.checkToken(token)
    onComplete(tokenValidateFuture) {
      case Success(true) => getFreeSpotOrCreateNewOne
      case Failure(exception) =>
        errorLog("startOnlineGameRoute", s"${exception.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
      case other =>
        log("startOnlineGameRoute", s"${other.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
    }
  }

  private def getFreeSpotOrCreateNewOne: Route = {
    val getExistGameRoomId = onlineGameRepository.searchFreeGameSpot
    onComplete(getExistGameRoomId) {
      case Success(Some(gameRoom)) => view.onStartOnlineGame(gameRoom.gameRoomId)
      case Success(None) => pickRandomQuestions
      case Failure(exception) =>
        errorLog("getFreeSpot", s"${exception.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
      case other =>
        log("getFreeSpot", s"${other.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
    }
  }

  private def pickRandomQuestions : Route = {
    val random = new scala.util.Random
    val getAllFacts = factRepository.all
    onComplete(getAllFacts) {
      case Success(facts) =>
        val randomFacts = Stream.continually(random.nextInt(facts.length)).map(facts).take(5).toList
        createNewOne(randomFacts)
      case Failure(exception) =>
        errorLog("pickRandomQuestions", s"${exception.toString}")
        view.onError(List(errors.ERROR_LOAD_FACTS))
      case other =>
        log("pickRandomQuestions", s"${other.toString}")
        view.onError(List(errors.ERROR_LOAD_FACTS))
    }
  }

  private def createNewOne(questions: List[Fact]): Route = {
    val roomId = generateGameRoomId()
    val onlineGame = OnlineGame(
      1,
      roomId,
      None,
      None,
      questions.asJson.noSpaces,
      List(0, 0, 0, 0, 0).asJson.noSpaces,
      List(0, 0, 0, 0, 0).asJson.noSpaces,
      isGameStarted = false,
      0
    )
    val insertOnlineGameFuture = onlineGameRepository.insert(onlineGame)
    onComplete(insertOnlineGameFuture) {
      case Success(1) => view.onStartOnlineGame(roomId)
      case Failure(exception) =>
        errorLog("getSpotOrCreateNewOne", s"onlineGame=$onlineGame ${exception.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
      case other =>
        log("getSpotOrCreateNewOne", s"onlineGame=$onlineGame ${other.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
    }
  }
  /** Create game room END **/


  /** WS echo START **/
  def echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }
  /** WS echo END **/

}
