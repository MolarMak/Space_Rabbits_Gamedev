package controllers

class Errors(private val language: String) {

  /** Auth errors START **/
  def ERROR_NAME_LENGTH: String = language match {
    case _ => "Name length must be between 2 and 30 characters"
  }

  def ERROR_PASSWORD_LENGTH: String = language match {
    case _ => "Password length must at least 8 characters"
  }

  def ERROR_CAN_T_FIND_USER: String = language match {
    case _ => "Can't find user with such data"
  }

  def ERROR_CAN_T_REGISTER_USER: String = language match {
    case _ => "Can't register user with such data"
  }

  def ERROR_USER_ALREADY_EXISTS: String = language match {
    case _ => "User already exists"
  }

  def ERROR_REGISTRATION: String = language match {
    case _ => "Registration error"
  }

  def ERROR_TOKEN_NOT_VALID: String = language match {
    case _ => "Token not valid"
  }

  def ERROR_WHEN_LOGOUT: String = language match {
    case _ => "Error when logout"
  }
  /** Auth errors END **/


  /** Fact errors START **/
  def ERROR_VERSION: String = language match {
    case _ => "Enter correct version number > 0"
  }

  def ERROR_OFFSET: String = language match {
    case _ => "Enter correct offset number in range 0..200"
  }

  def ERROR_LIMIT: String = language match {
    case _ => "Enter correct limit number in range 1..200"
  }

  def ERROR_LOAD_FACTS: String = language match {
    case _ => "Error when load facts"
  }
  /** Fact errors END **/

}
