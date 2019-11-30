package apiVersions.v1

import akka.http.scaladsl.server.{Directives, Route}
import controllers.{AuthController, AuthControllerTrait, AuthViewTrait}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.syntax._
import models.{LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, ResponseFalse}
import view.log
import slick.jdbc.PostgresProfile.backend.Database

class AuthView(private val db: Database) extends Directives with FailFastCirceSupport with AuthViewTrait {

  private val controller: AuthControllerTrait = new AuthController(db, this)

  def login: Route =
    path("api" / apiVersion / "login") {
      post {
        entity(as[LoginRequest]) { loginRequest =>
          log("login", s"input: ${loginRequest.toString}")
          controller.loginController(loginRequest)
        }
      }
    }

  def register: Route =
    path("api" / apiVersion / "register") {
      post {
        entity(as[RegisterRequest]) { registerRequest =>
          log("register", s"input: ${registerRequest.toString}")
          controller.registerController(registerRequest)
        }
      }
    }

  override def onLogin(token: String): Route = {
    complete(LoginResponse(result = true, token).asJson)
  }

  override def onRegister(token: String): Route = {
    complete(RegisterResponse(result = true, token).asJson)
  }

  override def onError(errors: List[String]): Route = {
    complete(ResponseFalse(result = false, errors).asJson)
  }
}
