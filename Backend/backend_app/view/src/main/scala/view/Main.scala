package view

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

object Main {

  def main(args: Array[String]): Unit = {
    val db = Database.forConfig("postgresql")

    implicit val system: ActorSystem = ActorSystem("quiz-system")
    implicit val materialise: ActorMaterializer = ActorMaterializer()
    val http: HttpExt = Http()

    val quizApi = new QuizConfig()

    val bindingFuture = http.bindAndHandle(quizApi.routes, "0.0.0.0", 8080)

    println("Server (version 1.0.1) is now online. Press RETURN to stop...")
    scala.io.StdIn.readLine()
    val stop = for {
      binding <- bindingFuture
      _ <- binding.unbind()
      _ <- system.terminate()
    } yield ()
    Await.result(stop, 10.second)
    db.close()
    println("Server is offline...")
  }

}
