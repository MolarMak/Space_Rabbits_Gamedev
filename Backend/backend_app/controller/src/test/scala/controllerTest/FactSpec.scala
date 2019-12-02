package controllerTest

import akka.http.scaladsl.server.Route
import controllers.{Errors, FactController, FactViewTrait}
import entities.Fact
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.PostgresProfile.api._

class FactSpec extends WordSpec with Matchers {

  val db = Database.forConfig("database")
  val justForTestView = new FactViewTrait {
    override def onLoadFacts(facts: Vector[Fact], offset: Int, limit: Int, hasNext: Boolean): Route = ???
    override def onError(errors: List[String]): Route = ???
    override def onAuthError(errors: List[String]): Route = ???
  }

  val factController = new FactController(db, justForTestView)
  val errors = new Errors("en")

  "check that version > 0" in {
    factController.factValidate(-1, 0, 1) shouldEqual List(errors.ERROR_VERSION)
    factController.factValidate(0, 0, 1) shouldEqual List()
  }

  "check that offset >= 0 and <= 200" in {
    factController.factValidate(0, -1, 1) shouldEqual List(errors.ERROR_OFFSET)
    factController.factValidate(0, 201, 1) shouldEqual List(errors.ERROR_OFFSET)
    factController.factValidate(0, 20, 1) shouldEqual List()
  }

  "check that limit > 0 and <= 200" in {
    factController.factValidate(0, 0, -1) shouldEqual List(errors.ERROR_LIMIT)
    factController.factValidate(0, 0, 201) shouldEqual List(errors.ERROR_LIMIT)
    factController.factValidate(0, 0, 10) shouldEqual List()
  }

}
