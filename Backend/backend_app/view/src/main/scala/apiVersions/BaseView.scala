package apiVersions

import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.server.{Directives, Route}
import controllers.ViewTrait
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import models.ResponseFalse
import io.circe.syntax._

class BaseView extends Directives with FailFastCirceSupport with ViewTrait {

  override def onAuthError(errors: List[String]): Route = {
    complete((Unauthorized, ResponseFalse(result = false, errors)))
  }

  override def onError(errors: List[String]): Route = {
    complete(ResponseFalse(result = false, errors).asJson)
  }

}
