package rest.v2

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import apiVersions.v2.QuizApiV2
import controllers.Errors
import entities.Fact
import io.circe.parser.decode
import io.circe.syntax._
import models.ResponseTrue
import models.v2._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import repositories.{FactRepository, OnlineGameRepository, StatisticRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

class OnlineGameV2Spec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val db = Database.forConfig("database")
  val errors = new Errors("en")

  val quizApi = new QuizApiV2(db)
  val apiVersion = "v2"
  val factRepo: FactRepository = new FactRepository(db)
  val statisticRepo: StatisticRepository = new StatisticRepository(db)
  val userRepo: UserRepository = new UserRepository(db)
  val repo: OnlineGameRepository = new OnlineGameRepository(db)

  "insert 20 facts, 2 users" in {
    for(i <- 1 until 21) {
      whenReady(factRepo.insert(Fact(i, s"fact$i", s"trueFact$i", s"falseFact$i", if(i % 2 == 0) 1 else 2))) { result =>
        result shouldBe 1
      }
    }

    whenReady(factRepo.all) { facts =>
      whenReady(factRepo.findById(facts(0).id)) { fact =>
        fact.get.fact shouldBe "fact1"
      }
      whenReady(factRepo.findById(facts(1).id)) { user =>
        user.get.fact shouldBe "fact2"
      }
    }

    val register = RegisterRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, register.asJson.toString())

    Post(s"/api/$apiVersion/register", requestEntity) ~> Route.seal(quizApi.register) ~> check {
      status shouldEqual StatusCodes.OK
      decode[RegisterResponse](responseAs[String]) match {
        case Right(json) => json.result shouldEqual true
        case _ => false shouldEqual true
      }
    }

    val register2 = RegisterRequest("molarmaker2", "12345678")
    val requestEntity2 = HttpEntity(ContentTypes.`application/json`, register2.asJson.toString())

    Post(s"/api/$apiVersion/register", requestEntity2) ~> Route.seal(quizApi.register) ~> check {
      status shouldEqual StatusCodes.OK
      decode[RegisterResponse](responseAs[String]) match {
        case Right(json) => json.result shouldEqual true
        case _ => false shouldEqual true
      }
    }
  }

  "get online game room by first user" in {
    def getOnlineGameRoom(token: String) = {
      Get(s"/api/$apiVersion/onlineGameRoom") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.onlineGameRoom) ~> check {
        status shouldEqual StatusCodes.OK
        decode[StartGameResponse](responseAs[String]) match {
          case Right(json) =>
            whenReady(repo.findById(1)) { gameRoom =>
              json.data.roomId shouldEqual gameRoom.get.gameRoomId
              json.result shouldEqual true
              getOnlineGameRoomInfo(token, json.data.roomId)
            }
          case _ => false shouldEqual true
        }
      }
    }

    def getOnlineGameRoomInfo(token: String, roomId: String) = { //todo put in another function
      Get(s"/api/$apiVersion/onlineGameRoomInfo?gameRoomId=$roomId") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.onlineGameRoomInfo) ~> check {
        status shouldEqual StatusCodes.OK
        decode[OnlineGameResponse](responseAs[String]) match {
          case Right(json) =>
            whenReady(repo.findById(1)) { gameRoom =>
              json.data.gameRoomId shouldEqual gameRoom.get.gameRoomId
              json.data.player1Name shouldEqual Some("molarmaker")
              json.data.player2Name shouldEqual None
              json.data.answersPlayer1List shouldEqual List(0,0,0,0,0)
              json.data.answersPlayer2List shouldEqual List(0,0,0,0,0)
              json.result shouldEqual true
              putAnswer(token, roomId)
            }
          case _ => false shouldEqual true
        }
      }
    }

    def checkPutAnswerResult(token: String, roomId: String) = { //todo put in another function
      Get(s"/api/$apiVersion/onlineGameRoomInfo?gameRoomId=$roomId") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.onlineGameRoomInfo) ~> check {
        status shouldEqual StatusCodes.OK
        decode[OnlineGameResponse](responseAs[String]) match {
          case Right(json) =>
            whenReady(repo.findById(1)) { gameRoom =>
              json.data.gameRoomId shouldEqual gameRoom.get.gameRoomId
              json.data.player1Name shouldEqual Some("molarmaker")
              json.data.player2Name shouldEqual None
              json.data.answersPlayer1List shouldEqual List(1,0,0,0,0)
              json.data.answersPlayer2List shouldEqual List(0,0,0,0,0)
              json.result shouldEqual true
            }
          case _ => false shouldEqual true
        }
      }
    }

    def putAnswer(token: String, roomId: String) = { //todo put in another function
      val answer = PutAnswerRequest(roomId, 0, 1)
      val requestEntity = HttpEntity(ContentTypes.`application/json`, answer.asJson.toString())

      Post(s"/api/$apiVersion/putAnswer", requestEntity) ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.putAnswer) ~> check {
        status shouldEqual StatusCodes.OK
        decode[ResponseTrue](responseAs[String]) match {
          case Right(json) =>
            json.result shouldEqual true
            checkPutAnswerResult(token, roomId)
          case _ => false shouldEqual true
        }
      }
    }

    val login = LoginRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    Post(s"/api/$apiVersion/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[LoginResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          getOnlineGameRoom(json.data.token)
        case _ => false shouldEqual true
      }
    }
  }

  "get online game room by second user" in {
    def getOnlineGameRoom(token: String) = {
      whenReady(repo.findById(1)) {
        case Some(gameRoom) =>
          Get(s"/api/$apiVersion/onlineGameRoom") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.onlineGameRoom) ~> check {
            status shouldEqual StatusCodes.OK
            decode[StartGameResponse](responseAs[String]) match {
              case Right(json) =>
                json.result shouldEqual true
                json.data.roomId shouldEqual gameRoom.gameRoomId
                getOnlineGameRoomInfo(token, json.data.roomId)
              case _ => false shouldEqual true
            }
          }
        case _ => false shouldEqual true
      }
    }

    def getOnlineGameRoomInfo(token: String, roomId: String) = { //todo put in another function
      Get(s"/api/$apiVersion/onlineGameRoomInfo?gameRoomId=$roomId") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.onlineGameRoomInfo) ~> check {
        status shouldEqual StatusCodes.OK
        decode[OnlineGameResponse](responseAs[String]) match {
          case Right(json) =>
            whenReady(repo.findById(1)) { gameRoom =>
              json.data.gameRoomId shouldEqual gameRoom.get.gameRoomId
              json.data.player1Name shouldEqual Some("molarmaker")
              json.data.player2Name shouldEqual Some("molarmaker2")
              json.data.answersPlayer1List shouldEqual List(1,0,0,0,0)
              json.data.answersPlayer2List shouldEqual List(0,0,0,0,0)
              json.result shouldEqual true
              putAnswer(token, roomId)
            }
          case _ => false shouldEqual true
        }
      }
    }

    def checkPutAnswerResult(token: String, roomId: String) = { //todo put in another function
      Get(s"/api/$apiVersion/onlineGameRoomInfo?gameRoomId=$roomId") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.onlineGameRoomInfo) ~> check {
        status shouldEqual StatusCodes.OK
        decode[OnlineGameResponse](responseAs[String]) match {
          case Right(json) =>
            whenReady(repo.findById(1)) { gameRoom =>
              json.data.gameRoomId shouldEqual gameRoom.get.gameRoomId
              json.data.player1Name shouldEqual Some("molarmaker")
              json.data.player2Name shouldEqual Some("molarmaker2")
              json.data.answersPlayer1List shouldEqual List(1,0,0,0,0)
              json.data.answersPlayer2List shouldEqual List(0,1,0,0,0)
              json.result shouldEqual true
            }
          case _ => false shouldEqual true
        }
      }
    }

    def putAnswer(token: String, roomId: String) = { //todo put in another function
      val answer = PutAnswerRequest(roomId, 1, 1)
      val requestEntity = HttpEntity(ContentTypes.`application/json`, answer.asJson.toString())

      Post(s"/api/$apiVersion/putAnswer", requestEntity) ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.putAnswer) ~> check {
        status shouldEqual StatusCodes.OK
        decode[ResponseTrue](responseAs[String]) match {
          case Right(json) =>
            json.result shouldEqual true
            checkPutAnswerResult(token, roomId)
          case _ => false shouldEqual true
        }
      }
    }

    val login = LoginRequest("molarmaker2", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    Post(s"/api/$apiVersion/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[LoginResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          getOnlineGameRoom(json.data.token)
        case _ => false shouldEqual true
      }
    }
  }

  "echo ws test" in {
    val wsClient = WSProbe()

    WS(s"/api/$apiVersion/wsEcho", wsClient.flow) ~> quizApi.wsEcho ~> check {
      // check response for WS Upgrade headers
      isWebSocketUpgrade shouldEqual true

      wsClient.sendMessage("Hello!")
      wsClient.expectMessage("ECHO: Hello!")

      wsClient.sendMessage("How are you?")
      wsClient.expectMessage("ECHO: How are you?")
    }
  }

  "drop all records" in {
    whenReady(factRepo.all) { facts =>
      whenReady(factRepo.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(factRepo.all)(_.size shouldBe 0)
    }
    whenReady(repo.all) { facts =>
      whenReady(repo.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(repo.all)(_.size shouldBe 0)
    }
    whenReady(statisticRepo.all) { facts =>
      whenReady(statisticRepo.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(statisticRepo.all)(_.size shouldBe 0)
    }
    whenReady(userRepo.all) { facts =>
      whenReady(userRepo.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(userRepo.all)(_.size shouldBe 0)
    }
  }

}
