package controllers

import akka.http.scaladsl.server.{Directives, Route}
import entities.Fact
import repositories.FactRepository
import slick.jdbc.PostgresProfile.backend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

trait FactControllerTrait {
  def synchroniseFactsRoute(version: Int, offset: Int, limit: Int) : Route
}

trait FactViewTrait extends ViewTrait {
  def onLoadFacts(facts: Vector[Fact], offset: Int, limit: Int, hasNext: Boolean) : Route
}

class FactController(private val db: Database, private val view: FactViewTrait) extends Directives with FactControllerTrait {

  private val factRepository = new FactRepository(db)
  private val errors = new Errors("en")

  /** synchronize facts START **/
  override def synchroniseFactsRoute(version: Int, offset: Int, limit: Int) : Route = {
    val validate = factValidate(version, offset, limit)
    if(validate.nonEmpty) {
      view.onError(validate)
    } else {
      getAvailableFacts(version, offset, limit)
    }
  }

  private def getAvailableFacts(version: Int, offset: Int, limit: Int) : Route = {
    val getAvailableFactsFuture = factRepository.getAllGreaterThanVersionLimitBy(version, offset, limit)
    onComplete(getAvailableFactsFuture) {
      case Success(facts) => view.onLoadFacts(facts, offset, limit, hasNext = facts.size == limit)
      case Failure(exception) =>
        errorLog("getAvailableFacts", s"v$version, offset $offset, limit $limit, ${exception.toString}")
        view.onError(List(errors.ERROR_LOAD_FACTS))
      case other =>
        log("getAvailableFacts", s"v$version, offset $offset, limit $limit, ${other.toString}")
        view.onError(List(errors.ERROR_LOAD_FACTS))
    }
  }
  /** synchronize facts END **/


  /** Validation START **/
  private def versionRule(version: Int) = version >= 0
  private def offsetRule(offset: Int) = offset >= 0 && offset <= 200
  private def limitRule(limit: Int) = limit > 0 && limit <= 200

  private case object VersionValidation extends DomainValidation {
    def errorMessage: String = errors.ERROR_VERSION
  }

  private case object OffsetValidation extends DomainValidation {
    def errorMessage: String = errors.ERROR_OFFSET
  }

  private case object LimitValidation extends DomainValidation {
    def errorMessage: String = errors.ERROR_LIMIT
  }

  def factValidate(version: Int, offset: Int, limit: Int): List[String] = {
    onValidate(List(
      (versionRule(version), VersionValidation),
      (offsetRule(offset), OffsetValidation),
      (limitRule(limit), LimitValidation)
    ))
  }
  /** Validation END **/
}
