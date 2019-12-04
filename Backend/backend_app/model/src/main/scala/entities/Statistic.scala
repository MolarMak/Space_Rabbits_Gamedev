package entities

import io.circe.generic.JsonCodec
import repositories.HasId

@JsonCodec
case class Statistic(id: Int, online: Int, offline: Int, userId: Int) extends HasId
