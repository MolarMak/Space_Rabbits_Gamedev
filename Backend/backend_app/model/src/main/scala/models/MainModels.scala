package models

import io.circe.generic.JsonCodec

@JsonCodec
case class ResponseTrue(result: Boolean = true)

@JsonCodec
case class ResponseFalse(result: Boolean = false, errors: List[String])
