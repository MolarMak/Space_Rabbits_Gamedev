package controllers.v2

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Flow
import controllers.{Errors, ViewTrait, errorLog, log, _}
import entities.OnlineGame
import io.circe.parser.decode
import io.circe.syntax._
import models.v2.{FactTrueFalse, OnlineGameData}
import repositories.{FactRepository, OnlineGameRepository, UserRepository}
import slick.jdbc.PostgresProfile.backend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait OnlineGameControllerTrait {
  def startOnlineGameRoute(token: String) : Route
  def getGameRoomInfoRoute(token: String, gameRoomId: String) : Route
  def putAnswersRoute(token: String, gameRoomId: String, answerNumber: Int, answer: Int) : Route
  def echoService: Flow[Message, Message, _]
}

trait OnlineGameViewTrait extends ViewTrait {
  def onStartOnlineGame(gameRoomId: String) : Route
  def onLoadGameInfo(gameData: OnlineGameData) : Route
  def onPutAnswer: Route
}

class OnlineGameController(private val db: Database, private val view: OnlineGameViewTrait) extends Directives with OnlineGameControllerTrait {

  private val userRepository = new UserRepository(db)
  private val factRepository = new FactRepository(db)
  private val onlineGameRepository = new OnlineGameRepository(db)
  val errors = new Errors("en")


  /** Create game room START **/
  override def startOnlineGameRoute(token: String): Route = {
    val tokenValidateFuture = userRepository.getUserIdByToken(token)
    onComplete(tokenValidateFuture) {
      case Success(Some(userId)) => getFreeSpotOrCreateNewOne(userId)
      case Failure(exception) =>
        errorLog("startOnlineGameRoute", s"${exception.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
      case other =>
        log("startOnlineGameRoute", s"${other.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
    }
  }

  private def getFreeSpotOrCreateNewOne(userId: Int): Route = {
    val getExistGameRoomId = onlineGameRepository.searchFreeGameSpot
    onComplete(getExistGameRoomId) {
      case Success(Some(gameRoom)) => joinExistingRoom(gameRoom.id, userId, gameRoom.gameRoomId)
      case Success(None) => pickRandomQuestions(userId)
      case Failure(exception) =>
        errorLog("getFreeSpotOrCreateNewOne", s"${exception.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
      case other =>
        log("getFreeSpotOrCreateNewOne", s"${other.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
    }
  }

  private def joinExistingRoom(roomId: Int, userId: Int, gameRoomId: String): Route = {
    val joinExistsRoom = onlineGameRepository.joinToGame(roomId, userId)
    onComplete(joinExistsRoom) {
      case Success(1) => view.onStartOnlineGame(gameRoomId)
      case Failure(exception) =>
        errorLog("joinExistingRoom", s"$roomId $userId ${exception.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
      case other =>
        log("joinExistingRoom", s"$roomId $userId ${other.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
    }

  }

  private def pickRandomQuestions(userId: Int) : Route = {
    val random = new scala.util.Random
    val getAllFacts = factRepository.all
    onComplete(getAllFacts) {
      case Success(facts) =>
        val randomFacts = Stream.continually(random.nextInt(facts.length)).map(facts).take(5).toList
        val randomTakeTrueFalse = randomFacts.map(it => FactTrueFalse(it, random.nextInt(2) == 0))
        createNewOne(userId, randomTakeTrueFalse)
      case Failure(exception) =>
        errorLog("pickRandomQuestions", s"${exception.toString}")
        view.onError(List(errors.ERROR_LOAD_FACTS))
      case other =>
        log("pickRandomQuestions", s"${other.toString}")
        view.onError(List(errors.ERROR_LOAD_FACTS))
    }
  }

  private def createNewOne(userId: Int, questions: List[FactTrueFalse]): Route = {
    val roomId = generateGameRoomId()
    val onlineGame = OnlineGame(
      1,
      roomId,
      Some(userId),
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
        errorLog("createNewOne", s"onlineGame=$onlineGame ${exception.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
      case other =>
        log("createNewOne", s"onlineGame=$onlineGame ${other.toString}")
        view.onError(List(errors.ERROR_GAME_ROOM_OPEN))
    }
  }
  /** Create game room END **/


  /** Get game room info START **/
  override def getGameRoomInfoRoute(token: String, gameRoomId: String) : Route = {
    val tokenValidateFuture = userRepository.checkToken(token)
    onComplete(tokenValidateFuture) {
      case Success(true) => getGameRoomInfo(gameRoomId)
      case Failure(exception) =>
        errorLog("getGameRoomInfoRoute", s"${exception.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
      case other =>
        log("getGameRoomInfoRoute", s"${other.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
    }
  }

  private def getGameRoomInfo(gameRoomId: String) : Route = {
    val gameRoomInfo = onlineGameRepository.getRoomInfo(gameRoomId)
    onComplete(gameRoomInfo) {
      case Success(Some(gameInfo)) => convertGameInfo(gameInfo)
      case Failure(exception) =>
        errorLog("getGameRoomInfo", s"${exception.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
      case other =>
        log("getGameRoomInfo", s"${other.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
    }
  }

  private def convertGameInfo(gameInfo: OnlineGame) : Route = { //todo put in few functions
    val getFirstUserName = userRepository.getUserNameById(gameInfo.player1Id) //todo 1 func
    onComplete(getFirstUserName) {
      case Success(userName1) =>
        val getSecondUserName = userRepository.getUserNameById(gameInfo.player2Id) //todo 2 func
        onComplete(getSecondUserName) {
          case Success(userName2) =>
            decode[List[FactTrueFalse]](gameInfo.questionsList) match { //todo 3 func
              case Right(facts) =>
                decode[List[Int]](gameInfo.answersPlayer1List) match { //todo 4 func
                  case Right(answers1) =>
                    decode[List[Int]](gameInfo.answersPlayer2List) match { //todo 5 func
                      case Right(answers2) =>
                        view.onLoadGameInfo(
                          OnlineGameData(
                            gameInfo.gameRoomId,
                            userName1,
                            userName2,
                            facts,
                            answers1,
                            answers2
                          )
                        )
                      case _ =>
                        errorLog("convertGameInfo", s"parce error answers2 ${gameInfo.toString}")
                        view.onAuthError(List(errors.ERROR_GAME_ROOM_ANSWERS_FIND))
                    }

                  case _ =>
                    errorLog("convertGameInfo", s"parce error answers1 ${gameInfo.toString}")
                    view.onAuthError(List(errors.ERROR_GAME_ROOM_ANSWERS_FIND))
                }
              case _ =>
                errorLog("convertGameInfo", s"parce error questions ${gameInfo.toString}")
                view.onAuthError(List(errors.ERROR_GAME_ROOM_QUESTIONS_FIND))
            }
          case Failure(exception) =>
            errorLog("convertGameInfo", s"second user ${gameInfo.toString} ${exception.toString}")
            view.onAuthError(List(errors.ERROR_GAME_ROOM_USER_FIND))
          case other =>
            log("convertGameInfo", s"second user ${gameInfo.toString} ${other.toString}")
            view.onAuthError(List(errors.ERROR_GAME_ROOM_USER_FIND))
        }
      case Failure(exception) =>
        errorLog("convertGameInfo", s"first user ${gameInfo.toString} ${exception.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
      case other =>
        log("convertGameInfo", s"first user ${gameInfo.toString} ${other.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
    }
  }
  /** Get game room info END **/


  /** Put answers START **/
  override def putAnswersRoute(token: String, gameRoomId: String, answerNumber: Int, answer: Int): Route = {
    val tokenValidateFuture = userRepository.getUserIdByToken(token)
    onComplete(tokenValidateFuture) {
      case Success(Some(userId)) => findGameToPut(userId, gameRoomId, answerNumber, answer)
      case Failure(exception) =>
        errorLog("putAnswersRoute", s"${exception.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
      case other =>
        log("putAnswersRoute", s"${other.toString}")
        view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
    }
  }

  private def findGameToPut(userId: Int, gameRoomId: String, answerNumber: Int, answer: Int) : Route = {
    val gameRoomInfo = onlineGameRepository.getRoomInfo(gameRoomId)
    onComplete(gameRoomInfo) {
      case Success(Some(gameInfo)) =>
        if(gameInfo.player1Id.contains(userId)) {
          putAnswersFirst(userId, gameInfo, answerNumber, answer)
        } else if(gameInfo.player2Id.contains(userId)) {
          putAnswersSecond(userId, gameInfo, answerNumber, answer)
        } else {
          log("findGameToPut", s"can't find user to put $gameRoomId $userId")
          view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
        }
      case Failure(exception) =>
        errorLog("findGameToPut", s"${exception.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
      case other =>
        log("findGameToPut", s"${other.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_FIND))
    }
  }

  private def putAnswersFirst(userId: Int, gameRoomInfo: OnlineGame, answerNumber: Int, answer: Int) : Route = {
    decode[List[Int]](gameRoomInfo.answersPlayer1List) match {
      case Right(answers1) =>
        val answers = answers1.patch(answerNumber, Seq(answer), 1) //todo check answer number
        val putAnswersFirstFuture = onlineGameRepository.putAnswersFirst(gameRoomInfo.gameRoomId, answers.asJson.noSpaces)
        onComplete(putAnswersFirstFuture) {
          case Success(1) => view.onPutAnswer
          case Failure(exception) =>
            errorLog("putAnswersFirst", s"${exception.toString}")
            view.onAuthError(List(errors.ERROR_PUT_ANSWER))
          case other =>
            log("putAnswersFirst", s"${other.toString}")
            view.onAuthError(List(errors.ERROR_PUT_ANSWER))
        }

      case _ =>
        errorLog("convertGameInfo", s"parce error answers1 ${gameRoomInfo.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_ANSWERS_FIND))
    }
  }

  private def putAnswersSecond(userId: Int, gameRoomInfo: OnlineGame, answerNumber: Int, answer: Int) : Route = {
    decode[List[Int]](gameRoomInfo.answersPlayer2List) match {
      case Right(answers2) =>
        val answers = answers2.patch(answerNumber, Seq(answer), 1) //todo check answer number
        val putAnswersSecondFuture = onlineGameRepository.putAnswersSecond(gameRoomInfo.gameRoomId, answers.asJson.noSpaces)
        onComplete(putAnswersSecondFuture) {
          case Success(1) => view.onPutAnswer
          case Failure(exception) =>
            errorLog("putAnswersSecond", s"${exception.toString}")
            view.onAuthError(List(errors.ERROR_PUT_ANSWER))
          case other =>
            log("putAnswersSecond", s"${other.toString}")
            view.onAuthError(List(errors.ERROR_PUT_ANSWER))
        }

      case _ =>
        errorLog("convertGameInfo", s"parce error answers2 ${gameRoomInfo.toString}")
        view.onAuthError(List(errors.ERROR_GAME_ROOM_ANSWERS_FIND))
    }
  }
  /** Put answers END **/


  /** WS echo START **/
  def echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }
  /** WS echo END **/
}
