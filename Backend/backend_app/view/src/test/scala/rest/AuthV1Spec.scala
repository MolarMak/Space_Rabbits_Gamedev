package rest

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import apiVersions.v1.QuizApiV1
import controllers.Errors
import models.{LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, ResponseFalse, ResponseTrue}
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.PostgresProfile.api._
import io.circe.syntax._
import io.circe.parser.decode
import org.scalatest.concurrent.ScalaFutures
import repositories.UserRepository

class AuthV1Spec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val db = Database.forConfig("database")
  val errors = new Errors("en")

  val quizApi = new QuizApiV1(db)
  val repo: UserRepository = new UserRepository(db)

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
          logout(json.token)
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
    whenReady(repo.deleteAll) { res =>
      res shouldBe 1
    }
    whenReady(repo.all)(_.size shouldBe 0)
  }

}
