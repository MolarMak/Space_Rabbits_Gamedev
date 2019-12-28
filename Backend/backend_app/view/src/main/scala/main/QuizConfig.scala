package main

import akka.http.scaladsl.server.{Directives, Route}
import apiVersions.v1.QuizApiV1
import apiVersions.v2.QuizApiV2
import slick.jdbc.PostgresProfile.backend.Database

class QuizConfig(private val db: Database) extends Directives {

  private def hello : Route =
    path("hello") {
      complete(s"Hello, it's QuizApp API v$serverVersion!")
    }

  def routes: Route = {
    val quizApiList : List[QuizApiTrait] = List(new QuizApiV1(db), new QuizApiV2(db))

    val routeList = quizApiList.map(quizRoutes).reduce(_ ~ _)

    hello ~ routeList
  }

  private def quizRoutes(quiz: QuizApiTrait) : Route = {
    quiz.login ~
      quiz.register ~
      quiz.logout ~
      quiz.synchroniseFacts ~
      quiz.loadScores ~
      quiz.onlineGameRoom ~
      quiz.onlineGameRoomInfo ~
      quiz.putAnswer ~
      quiz.wsEcho
  }

}
