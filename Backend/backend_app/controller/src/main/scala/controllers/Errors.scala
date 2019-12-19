package controllers

class Errors(private val language: String) {

  /** Common START **/
  def ERROR_UPDATE_API_VERSION(version: String): String = language match {
    case _ => s"Please, update to version $version"
  }

  /** Common END **/

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


  /** Profile errors START **/
  def ERROR_READ_ID: String = language match {
    case _ => "Read id must be >= 0"
  }

  def ERROR_LOAD_STATISTIC: String = language match {
    case _ => "Can't load user statistic"
  }

  def ERROR_SAVE_STATISTIC: String = language match {
    case _ => "Can't save user statistic"
  }

  def ERROR_LOAD_USER_DATA: String = language match {
    case _ => "Can't load user data"
  }

  def ERROR_COUNT_FACTS: String = language match {
    case _ => "Can't count facts"
  }
  /** Profile errors END **/


  /** Online Game errors START **/
  def ERROR_GAME_ROOM_OPEN: String = language match {
    case _ => "Can't open new game room"
  }

  /** Online Game errors END **/

}
