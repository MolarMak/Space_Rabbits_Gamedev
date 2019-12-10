package models.v1

import entities.Fact
import io.circe.generic.JsonCodec

@JsonCodec
case class FactResponse(result: Boolean = true, data: FactDataResponse)

@JsonCodec
case class FactDataResponse(facts: Vector[Fact], offset: Int, limit: Int, hasNext: Boolean)
