package rest.v1

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import apiVersions.v1.QuizApiV1
import controllers.Errors
import io.circe.parser.decode
import io.circe.syntax._
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import repositories.{StatisticRepository, UserRepository}
import slick.jdbc.PostgresProfile.api._

class AuthV1Spec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val db = Database.forConfig("database")
  val errors = new Errors("en")

  val quizApi = new QuizApiV1(db)
  val repoStatistic: StatisticRepository = new StatisticRepository(db)
  val repoUser: UserRepository = new UserRepository(db)

  "register new user" in {
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

  "user already exists when register" in {
    val register = RegisterRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, register.asJson.toString())

    Post("/api/v1/register", requestEntity) ~> Route.seal(quizApi.register) ~> check {
      status shouldEqual StatusCodes.OK
      decode[ResponseFalse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual false
          json.errors shouldEqual List(errors.ERROR_USER_ALREADY_EXISTS)

        case _ => false shouldEqual true
      }
    }
  }

  "login user" in {
    val login = LoginRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    Post("/api/v1/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[LoginResponse](responseAs[String]) match {
        case Right(json) => json.result shouldEqual true
        case _ => false shouldEqual true
      }
    }
  }

  "can't find user data when login" in {
    val login = LoginRequest("molarmaker", "123456789")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    Post("/api/v1/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[ResponseFalse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual false
          json.errors shouldEqual List(errors.ERROR_CAN_T_FIND_USER)

        case _ => false shouldEqual true
      }
    }
  }

  "logout user" in {
    val login = LoginRequest("molarmaker", "12345678")
    val requestEntity = HttpEntity(ContentTypes.`application/json`, login.asJson.toString())

    def logout(token: String) = {
      Get(s"/api/v1/logout?token=$token") ~> Route.seal(quizApi.logout) ~> check {
        status shouldEqual StatusCodes.OK
        decode[ResponseTrue](responseAs[String]) match {
          case Right(json) => json.result shouldEqual true
          case _ => false shouldEqual true
        }
      }
    }

    Post("/api/v1/login", requestEntity) ~> Route.seal(quizApi.login) ~> check {
      status shouldEqual StatusCodes.OK
      decode[LoginResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          logout(json.data.token)
        case _ => false shouldEqual true
      }
    }
  }

  "user not found when logout (token not valid)" in {
    Get(s"/api/v1/logout?token=wrongToken") ~> Route.seal(quizApi.logout) ~> check {
      status shouldEqual StatusCodes.Unauthorized
      decode[ResponseFalse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual false
          json.errors shouldEqual List(errors.ERROR_TOKEN_NOT_VALID)
        case _ => false shouldEqual true
      }
    }
  }

  "drop all records" in {
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
