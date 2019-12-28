package apiVersions.v2.chat

import akka.actor.{Actor, ActorRef, Terminated}
import akka.http.scaladsl.model.ws.TextMessage

class ClientConnectionActor extends Actor {
  var connection: Option[ActorRef] = None

  val receive: Receive = {
    case ('income, actor: ActorRef) =>
//      connection = Some(actor)
//      context.watch(actor)
    case Terminated(actor) if connection.contains(actor) =>
//      connection = None
//      context.stop(self)
    case 'sinkclose =>
//      context.stop(self)

    case TextMessage.Strict(t) =>
//      connection.foreach(_ ! TextMessage.Strict(s"echo $t"))
    case _ ⇒
//      connection.foreach(_ ! TextMessage.Strict("whaaat"))
  }

//  override def postStop(): Unit =
//    connection.foreach(context.stop)
}
