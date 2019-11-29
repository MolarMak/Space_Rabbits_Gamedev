package models

import io.circe.generic.JsonCodec

@JsonCodec
case class ResponseTrue(result: Boolean = true)

@JsonCodec
case class ResponseToken(result: Boolean = true, token: String)
