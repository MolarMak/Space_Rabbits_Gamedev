package models

import io.circe.generic.JsonCodec

@JsonCodec
case class LoginRequest(login: String, password: String)

@JsonCodec
case class LoginResponse(result: Boolean = true, data: TokenResponse)

@JsonCodec
case class RegisterRequest(login: String, password: String)

@JsonCodec
case class RegisterResponse(result: Boolean = true, data: TokenResponse)

@JsonCodec
case class TokenResponse(token: String)