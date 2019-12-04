package rest

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import apiVersions.v1.QuizApiV1
import controllers.Errors
import entities.Fact
import models.{LoginRequest, LoginResponse, ProfileResponse, RegisterRequest, RegisterResponse}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import repositories.{FactRepository, StatisticRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._
import io.circe.syntax._
import io.circe.parser.decode

class ProfileV1Spec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val db = Database.forConfig("database")
  val errors = new Errors("en")

  val quizApi = new QuizApiV1(db)

  val repoStatistic: StatisticRepository = new StatisticRepository(db)
  val repoUser: UserRepository = new UserRepository(db)
  val repoFact: FactRepository = new FactRepository(db)

  "preparing facts, user and statistic" in {
    for(i <- 1 until 21) {
      whenReady(repoFact.insert(Fact(i, s"fact$i", s"trueFact$i", s"falseFact$i", if(i % 2 == 0) 1 else 2))) { result =>
        result shouldBe 1
      }
    }

    val register = RegisterRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, register.asJson.toString())
    Post("/api/v1/register", requestEntity) ~> Route.seal(quizApi.register) ~> check {
      status shouldEqual StatusCodes.OK
      decode[RegisterResponse](responseAs[String]) match {
        case Right(json) => json.result shouldEqual true
        case _ => false shouldEqual true
      }
    }
  }

  "first user statistic is fully 0" in {
    val login = LoginRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    def getUserStatistic(token: String) = {
      Get("/api/v1/loadScores?readId=0") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.loadScores) ~> check {
        status shouldEqual StatusCodes.OK
        decode[ProfileResponse](responseAs[String]) match {
          case Right(json) =>
            json.result shouldEqual true
            json.data.offline shouldEqual 0
            json.data.allOffline shouldEqual 20
            json.data.online shouldEqual 0
            json.data.login shouldEqual "molarmaker"
          case _ => false shouldEqual true
        }
      }
    }

    Post("/api/v1/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[LoginResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          getUserStatistic(json.data.token)
        case _ => false shouldEqual true
      }
    }
  }

  "read 10 facts offline and get statistic" in {
    val login = LoginRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    def getUserStatistic(token: String) = {
      Get("/api/v1/loadScores?readId=10") ~> RawHeader("Authorization", token) ~> Route.seal(quizApi.loadScores) ~> check {
        status shouldEqual StatusCodes.OK
        decode[ProfileResponse](responseAs[String]) match {
          case Right(json) =>
            json.result shouldEqual true
            json.data.offline shouldEqual 10
            json.data.allOffline shouldEqual 20
            json.data.online shouldEqual 0
            json.data.login shouldEqual "molarmaker"
          case _ => false shouldEqual true
        }
      }
    }

    Post("/api/v1/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[LoginResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          getUserStatistic(json.data.token)
        case _ => false shouldEqual true
      }
    }
  }

  "drop all records" in {
    whenReady(repoFact.all) { facts =>
      whenReady(repoFact.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(repoFact.all)(_.size shouldBe 0)
    }
    whenReady(repoStatistic.all) { stats =>
      whenReady(repoStatistic.deleteAll) { res =>
        res shouldBe stats.size
      }
      whenReady(repoStatistic.all)(_.size shouldBe 0)
    }
    whenReady(repoUser.all) { users =>
      whenReady(repoUser.deleteAll) { res =>
        res shouldBe users.size
      }
      whenReady(repoUser.all)(_.size shouldBe 0)
    }
  }

}
