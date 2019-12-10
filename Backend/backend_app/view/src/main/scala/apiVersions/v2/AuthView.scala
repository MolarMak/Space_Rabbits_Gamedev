package apiVersions.v2

import akka.http.scaladsl.server.Route
import apiVersions.BaseView
import controllers.{AuthController, AuthControllerTrait, AuthViewTrait, log}
import io.circe.syntax._
import models._
import slick.jdbc.PostgresProfile.backend.Database

class AuthView(private val db: Database) extends BaseView with AuthViewTrait {

  private val controller: AuthControllerTrait = new AuthController(db, this)

  def login: Route =
    path("api" / apiVersion / "login") {
      post {
        entity(as[LoginRequest]) { loginRequest =>
          log("login", s"input: ${loginRequest.login}")
          controller.loginController(loginRequest)
        }
      }
    }

  def register: Route =
    path("api" / apiVersion / "register") {
      post {
        entity(as[RegisterRequest]) { registerRequest =>
          log("register", s"input: ${registerRequest.login}")
          controller.registerController(registerRequest)
        }
      }
    }

  def logout: Route =
    path("api" / apiVersion / "logout") {
      parameter('token.as[String]) { token =>
        log("logout", s"input: $token")
        controller.logoutController(token)
      }
    }

  override def onLogin(token: String): Route = {
    complete(LoginResponse(result = true, TokenResponse(token)).asJson)
  }

  override def onRegister(token: String): Route = {
    complete(RegisterResponse(result = true, TokenResponse(token)).asJson)
  }

  override def onLogout: Route = {
    complete(ResponseTrue().asJson)
  }

}
