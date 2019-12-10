package apiVersions.v2

import akka.http.scaladsl.server.Route
import apiVersions.BaseView
import controllers.{FactController, FactControllerTrait, FactViewTrait, log}
import entities.Fact
import models.{FactDataResponse, FactResponse}
import slick.jdbc.PostgresProfile.backend.Database

class FactView(private val db: Database) extends BaseView with FactViewTrait {

  private val controller: FactControllerTrait = new FactController(db, this)

  def synchroniseFacts: Route =
    path("api" / apiVersion / "synchroniseFacts") {
      get {
        parameter(('version.as[Int], 'offset.as[Int], 'limit.as[Int])) { (version, offset, limit) =>
          log("synchroniseFacts", s"input: $version $offset $limit")
          controller.synchroniseFactsRoute(version, offset, limit)
        }
      }
    }

  override def onLoadFacts(facts: Vector[Fact], offset: Int, limit: Int, hasNext: Boolean): Route = {
    complete(FactResponse(result = true, FactDataResponse(facts, offset, limit, hasNext)))
  }

}
