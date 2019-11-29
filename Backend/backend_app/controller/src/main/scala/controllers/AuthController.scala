package controllers

import akka.http.scaladsl.server.Route
import models.LoginRequest

trait AuthControllerTrait extends ControllerTrait {
  def loginValidate(name: String, password: String): List[String]
}

trait AuthViewTrait extends ViewTrait {
  def onLogin(token: String) : Route
}

class AuthController(private val view: AuthViewTrait) extends AuthControllerTrait {

  private def nameRule(name: String) = name.length > 2 && name.length < 30
  private def passwordRule(password: String) = password.length >= 8

  private case object NameRuleValidation extends DomainValidation {
    def errorMessage: String = "Name length must be between 2 and 30 characters"
  }

  private case object PasswordValidation extends DomainValidation {
    def errorMessage: String = "Password length must at least 8 characters"
  }

  def loginValidate(name: String, password: String): List[String] = {
    onValidate(List(
      (nameRule(name), NameRuleValidation),
      (passwordRule(password), PasswordValidation)
    ))
  }

  override def onError(errors: List[String]): Unit = {
    view.onError(errors)
  }

  def loginController(loginRequest: LoginRequest) : Route = {
    val validate = loginValidate(loginRequest.login, loginRequest.password)
    if(validate.nonEmpty) {
      view.onError(validate)
    } else {
      view.onLogin("exampleToken")
    }
  }

}
