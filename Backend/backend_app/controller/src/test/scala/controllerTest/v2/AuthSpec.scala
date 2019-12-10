package controllerTest.v2

import akka.http.scaladsl.server.Route
import controllers.Errors
import controllers.v2.{AuthController, AuthViewTrait}
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.PostgresProfile.api._

class AuthSpec extends WordSpec with Matchers {

  val db = Database.forConfig("database")
  val justForTestView = new AuthViewTrait {
    override def onLogin(token: String): Route = ???
    override def onRegister(token: String): Route = ???
    override def onLogout: Route = ???
    override def onError(errors: List[String]): Route = ???
    override def onAuthError(errors: List[String]): Route = ???
  }

  val authController = new AuthController(db, justForTestView)
  val errors = new Errors("en")

  "check that password less than 2 symbols" in {
    authController.authValidate("molarmaker", "1234") shouldEqual List(errors.ERROR_PASSWORD_LENGTH)
    authController.authValidate("molarmaker", "12345678") shouldEqual List()
  }

  "check that name is between range 2..30" in {
    authController.authValidate("mo", "12346789") shouldEqual List(errors.ERROR_NAME_LENGTH)
    authController.authValidate("mo123456789012345678901234567890", "12345678") shouldEqual List(errors.ERROR_NAME_LENGTH)
    authController.authValidate("molarmaker", "12345678") shouldEqual List()
  }

}
