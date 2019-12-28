package apiVersions.v2.chat

import akka.actor.ActorRef

sealed trait OnlineGameEvent

case class UserJoined(userId: Int, userActor: ActorRef) extends OnlineGameEvent

case class UserLeft(userId: Int) extends OnlineGameEvent

case class GameEventMessage(sender: String, message: String) extends OnlineGameEvent



