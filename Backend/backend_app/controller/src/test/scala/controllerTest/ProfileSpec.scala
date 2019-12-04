package controllerTest

import akka.http.scaladsl.server.Route
import controllers.{Errors, ProfileController, ProfileViewTrait}
import entities.Statistic
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.PostgresProfile.api._

class ProfileSpec extends WordSpec with Matchers {

  val db = Database.forConfig("database")
  val justForTestView = new ProfileViewTrait {
    override def onLoadScores(statistic: Statistic, login: String, countFacts: Int): Route = ???
    override def onError(errors: List[String]): Route = ???
    override def onAuthError(errors: List[String]): Route = ???
  }

  private val controller = new ProfileController(db, justForTestView)
  val errors = new Errors("en")

  "check that readId > 0" in {
    controller.loadScoresValidate(-1) shouldEqual List(errors.ERROR_READ_ID)
    controller.loadScoresValidate(1) shouldEqual List()
  }

}
