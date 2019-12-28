package apiVersions.v2.chat

import akka.actor.{Actor, ActorRef}

class GameRoomActor(roomId: String) extends Actor {

  var participants: Map[Int, ActorRef] = Map.empty[Int, ActorRef]

  override def receive: Receive = {
    case UserJoined(userId, actorRef) =>
//      participants += name -> actorRef
//      broadcast(SystemMessage(s"User $name joined channel..."))
      println(s"User $userId joined channel[$roomId]")

    case UserLeft(name) =>
//      println(s"User $name left channel[$roomId]")
//      broadcast(SystemMessage(s"User $name left channel[$roomId]"))
      participants -= name

//    case msg: IncomingMessage =>
//      broadcast(msg)
  }

//  def broadcast(message: ChatMessage): Unit = participants.values.foreach(_ ! message)

}
