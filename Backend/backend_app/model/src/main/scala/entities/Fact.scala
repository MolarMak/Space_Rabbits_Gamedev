package entities

import io.circe.generic.JsonCodec
import repositories.HasId

@JsonCodec
case class Fact(id: Int, fact: String, trueFact: String, falseFact: String, factVersion: Int) extends HasId
