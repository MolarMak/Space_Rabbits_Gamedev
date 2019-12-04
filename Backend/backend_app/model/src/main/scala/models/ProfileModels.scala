package models

import io.circe.generic.JsonCodec

@JsonCodec
case class ProfileResponse(result: Boolean = true, data: ProfileData)

@JsonCodec
case class ProfileData(login: String, offline: Int, allOffline: Int, online: Int)
