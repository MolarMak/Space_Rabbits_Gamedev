package models.v2

import io.circe.generic.JsonCodec

@JsonCodec
case class StartGameResponse(result: Boolean = true, roomId: String)
