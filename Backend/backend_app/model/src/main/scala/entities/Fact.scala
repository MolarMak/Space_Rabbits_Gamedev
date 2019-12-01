package entities

import repositories.HasId

case class Fact(id: Int, fact: String, trueFact: String, falseFact: String, factVersion: Int) extends HasId
