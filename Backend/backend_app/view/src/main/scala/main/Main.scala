package main

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import controllers.log

object Main {

  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("postgresql")

    implicit val system: ActorSystem = ActorSystem("quiz-system")
    implicit val materialise: ActorMaterializer = ActorMaterializer()
    val http: HttpExt = Http()

    val quizApi = new QuizConfig(db)

    val bindingFuture = http.bindAndHandle(quizApi.routes, "0.0.0.0", 80)

    println(s"Server (version $serverVersion) is now online. Press RETURN to stop...")
    log("main", "Server started")
    scala.io.StdIn.readLine()
    val stop = for {
      binding <- bindingFuture
      _ <- binding.unbind()
      _ <- system.terminate()
    } yield ()
    Await.result(stop, 10.second)
    db.close()
    println("Server is offline...")
    log("main", "Server stopped")
  }

}
