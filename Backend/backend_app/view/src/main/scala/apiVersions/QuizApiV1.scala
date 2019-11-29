package apiVersions

import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.syntax._
import models.{LoginRequest, ResponseTrue}
import view.{QuizApiTrait, log}

class QuizApiV1 extends Directives with FailFastCirceSupport with QuizApiTrait {

  val apiVersion = "v1"

  def login: Route =
    path("api" / apiVersion / "login") {
      post {
        entity(as[LoginRequest]) { login =>
          log("login", s"input: ${login.toString}")
          complete(ResponseTrue().asJson)
        }
      }
    }

}
