package controllers

import akka.http.scaladsl.server.{Directives, Route}
import entities.{Statistic, User}
import models.{LoginRequest, RegisterRequest}
import repositories.{StatisticRepository, UserRepository}
import slick.jdbc.PostgresProfile.backend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

trait AuthControllerTrait {
  def loginController(loginRequest: LoginRequest) : Route
  def registerController(registerRequest: RegisterRequest) : Route
  def logoutController(token: String) : Route
}

trait AuthViewTrait extends ViewTrait {
  def onLogin(token: String) : Route
  def onRegister(token: String) : Route
  def onLogout : Route
}

class AuthController(private val db: Database, private val view: AuthViewTrait) extends Directives with AuthControllerTrait {

  private val userRepository = new UserRepository(db)
  private val statisticRepository = new StatisticRepository(db)
  private val errors = new Errors("en")

  /** Login START **/
  def loginController(loginRequest: LoginRequest) : Route = {
    val validate = authValidate(loginRequest.login, loginRequest.password)
    if(validate.nonEmpty) {
      view.onError(validate)
    } else {
      checkAvailableLogin(loginRequest)
    }
  }

  private def checkAvailableLogin(loginRequest: LoginRequest) : Route = {
    val checkAccountAvailableFuture = userRepository.checkAccount(loginRequest.login, loginRequest.password)
    onComplete(checkAccountAvailableFuture) {
      case Success(Some(token)) => view.onLogin(token)
      case _ => view.onError(List(errors.ERROR_CAN_T_FIND_USER))
    }
  }
  /** Login END **/


  /** Register START **/
  override def registerController(registerRequest: RegisterRequest): Route = {
    val validate = authValidate(registerRequest.login, registerRequest.password)
    if(validate.nonEmpty) {
      view.onError(validate)
    } else {
      checkAvailableRegister(registerRequest)
    }
  }

  private def checkAvailableRegister(registerRequest: RegisterRequest) : Route = {
    val checkAccountAvailableFuture = userRepository.checkExists(registerRequest.login, registerRequest.password)
    onComplete(checkAccountAvailableFuture) {
      case Success(true) => view.onError(List(errors.ERROR_USER_ALREADY_EXISTS))
      case Success(false) => saveUserData(registerRequest)
      case some => println(some); view.onError(List(errors.ERROR_CAN_T_REGISTER_USER))
    }
  }

  private def saveUserData(registerRequest: RegisterRequest) : Route = {
    val token = generateToken()
    val user = User(1, registerRequest.login, registerRequest.password, token)
    val insertFuture = userRepository.insert(user)
    onComplete(insertFuture) {
      case Success(1) => findUserIdByToken(token)
      case _ => view.onError(List(errors.ERROR_REGISTRATION))
    }
  }

  private def findUserIdByToken(token: String) : Route = {
    val userIdFuture = userRepository.getUserIdByToken(token)
    onComplete(userIdFuture) {
      case Success(Some(userId)) => createUserStatistic(userId, token)
      case _ => view.onError(List(errors.ERROR_REGISTRATION))
    }
  }

  private def createUserStatistic(userId: Int, token: String) : Route = {
    val statistic = Statistic(1, 0, 0, userId)
    val insertStatisticFuture = statisticRepository.insert(statistic)
    onComplete(insertStatisticFuture) {
      case Success(1) => view.onRegister(token)
      case _ => view.onError(List(errors.ERROR_REGISTRATION))
    }
  }
  /** Register END **/


  /** Logout START **/
  override def logoutController(token: String): Route = {
    val newToken = generateToken()
    val logoutFuture = userRepository.logoutByToken(token, newToken)
    onComplete(logoutFuture) {
      case Success(1) => view.onLogout
      case Success(0) => view.onAuthError(List(errors.ERROR_TOKEN_NOT_VALID))
      case _ => view.onError(List(errors.ERROR_WHEN_LOGOUT))
    }
  }
  /** Logout END **/


  /** Validation START **/
  private def nameRule(name: String) = name.length > 2 && name.length < 30
  private def passwordRule(password: String) = password.length >= 8

  private case object NameRuleValidation extends DomainValidation {
    def errorMessage: String = errors.ERROR_NAME_LENGTH
  }

  private case object PasswordValidation extends DomainValidation {
    def errorMessage: String = errors.ERROR_PASSWORD_LENGTH
  }

  def authValidate(name: String, password: String): List[String] = {
    onValidate(List(
      (nameRule(name), NameRuleValidation),
      (passwordRule(password), PasswordValidation)
    ))
  }
  /** Validation END **/
}
