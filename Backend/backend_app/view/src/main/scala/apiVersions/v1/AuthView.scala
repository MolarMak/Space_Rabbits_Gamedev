package apiVersions.v1

import akka.http.scaladsl.server.{Directives, Route}
import controllers.{AuthController, AuthViewTrait}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.syntax._
import models.{LoginRequest, LoginResponse, ResponseFalse}
import view.log

class AuthView extends Directives with FailFastCirceSupport with AuthViewTrait {

  private val loginController = new AuthController(this)

  def login: Route =
    path("api" / apiVersion / "login") {
      post {
        entity(as[LoginRequest]) { loginRequest =>
          log("login", s"input: ${login.toString}")
          loginController.loginController(loginRequest)
        }
      }
    }

  override def onLogin(token: String): Route = {
    complete(LoginResponse(result = true, "exampleToken").asJson)
  }

  override def onError(errors: List[String]): Route = {
    complete(ResponseFalse(result = false, errors).asJson)
  }

}
