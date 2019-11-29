package models

import io.circe.generic.JsonCodec

@JsonCodec
case class LoginRequest(login: String, password: String)

@JsonCodec
case class LoginResponse(result: Boolean = true, token: String)