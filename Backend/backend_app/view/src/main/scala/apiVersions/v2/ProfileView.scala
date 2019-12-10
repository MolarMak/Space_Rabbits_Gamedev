package apiVersions.v2

import akka.http.scaladsl.server.Route
import apiVersions.BaseView
import controllers.{ProfileController, ProfileControllerTrait, ProfileViewTrait, log}
import entities.Statistic
import models.{ProfileData, ProfileResponse}
import slick.jdbc.PostgresProfile.backend.Database

class ProfileView(private val db: Database) extends BaseView with ProfileViewTrait {

  private val controller: ProfileControllerTrait = new ProfileController(db, this)

  def loadScores: Route =
    path("api" / apiVersion / "loadScores") {
      get {
        headerValueByName("Authorization") { token =>
          parameter('readId.as[Int]) { readId =>
            log("loadScores", s"input: $readId")
            controller.loadScoresController(token, readId)
          }
        }
      }
    }

  override def onLoadScores(statistic: Statistic, login: String, countFacts: Int): Route = {
    complete(ProfileResponse(result = true, ProfileData(login, statistic.offline, countFacts, statistic.online)))
  }
}
