package apiVersions

import akka.http.scaladsl.server.Route
import controllers.Errors
import models.ResponseFalse

class UpdateVersionView extends BaseView {

  private val errors = new Errors("en")

  def updateVersion(versionWithThisRoute: String): Route =
    complete(ResponseFalse(result = false, List(errors.ERROR_UPDATE_API_VERSION(versionWithThisRoute))))

}
