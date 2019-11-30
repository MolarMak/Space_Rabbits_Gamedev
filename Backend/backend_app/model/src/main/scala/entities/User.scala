package entities

import repositories.HasId

case class User(id: Int, login: String, password: String, token: String) extends HasId
