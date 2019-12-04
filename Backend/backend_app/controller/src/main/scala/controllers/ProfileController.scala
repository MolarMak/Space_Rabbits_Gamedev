package controllers

import akka.http.scaladsl.server.{Directives, Route}
import entities.Statistic
import repositories.{FactRepository, StatisticRepository, UserRepository}
import slick.jdbc.PostgresProfile.backend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

trait ProfileControllerTrait {
  def loadScoresController(token: String, readOffline: Int) : Route
}

trait ProfileViewTrait extends ViewTrait {
  def onLoadScores(statistic: Statistic, login: String, countFacts: Int) : Route
}

class ProfileController(private val db: Database, private val view: ProfileViewTrait) extends Directives with ProfileControllerTrait {

  private val userRepository = new UserRepository(db)
  private val statisticRepository = new StatisticRepository(db)
  private val factRepository = new FactRepository(db)
  val errors = new Errors("en")

  /** Load scores START **/
  def loadScoresController(token: String, readOffline: Int) : Route = {
    val tokenValidateFuture = userRepository.checkToken(token)
    onComplete(tokenValidateFuture) {
      case Success(true) => validateReadOffline(token, readOffline)
      case _ => view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
    }
  }

  private def validateReadOffline(token: String, readOffline: Int) : Route = {
    val validate = loadScoresValidate(readOffline)
    if(validate.nonEmpty) {
      view.onError(validate)
    } else {
      loadUserStatistic(token, readOffline)
    }
  }

  private def loadUserStatistic(token: String, readOffline: Int) : Route = {
    val loadUserStatistic = statisticRepository.getUserStatistic(token)
    onComplete(loadUserStatistic) {
      case Success(Some(statistic)) => saveOfflineStatistic(token, readOffline, statistic.id)
      case _ => view.onError(List(errors.ERROR_LOAD_STATISTIC))
    }
  }

  private def saveOfflineStatistic(token: String, readOffline: Int, statisticId: Int) : Route = {
    val updateOfflineStatistic = statisticRepository.updateOffline(statisticId, readOffline)
    onComplete(updateOfflineStatistic) {
      case Success(1) => reloadUserStatistic(token)
      case _ => view.onError(List(errors.ERROR_SAVE_STATISTIC))
    }
  }

  private def reloadUserStatistic(token: String) : Route = {
    val loadUserStatistic = statisticRepository.getUserStatistic(token)
    onComplete(loadUserStatistic) {
      case Success(Some(statistic)) => getUserData(token, statistic)
      case _ => view.onError(List(errors.ERROR_LOAD_STATISTIC))
    }
  }

  private def getUserData(token: String, statistic: Statistic) : Route = {
    val userDataFuture = userRepository.getUserProfileDataByToken(token)
    onComplete(userDataFuture) {
      case Success(Some(login)) => countFacts(statistic, login)
      case _ => view.onError(List(errors.ERROR_LOAD_USER_DATA))
    }
  }

  private def countFacts(statistic: Statistic, login: String) : Route = {
    val countFactFuture = factRepository.countFacts
    onComplete(countFactFuture) {
      case Success(factsCount) => view.onLoadScores(statistic, login, factsCount)
      case _ => view.onError(List(errors.ERROR_COUNT_FACTS))
    }
  }
  /** Load scores END **/


  /** Validation START **/
  private def readIdRule(readId: Int) = readId >= 0

  private case object ReadIdValidation extends DomainValidation {
    def errorMessage: String = errors.ERROR_READ_ID
  }

  def loadScoresValidate(readId: Int): List[String] = {
    onValidate(List(
      (readIdRule(readId), ReadIdValidation)
    ))
  }
  /** Validation END **/

}
