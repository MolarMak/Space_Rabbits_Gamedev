package view

import akka.http.scaladsl.server.{Directives, Route}
import apiVersions.v1.QuizApiV1

class QuizConfig extends Directives {

  private def hello : Route =
    path("hello") {
      complete(s"Hello, it's QuizApp API v$serverVersion!")
    }

  def routes: Route = {
    val quizApiList : List[QuizApiTrait] = List(new QuizApiV1())

    val routeList = quizApiList.map(quizRoutes).reduce(_ ~ _)

    hello ~ routeList
  }

  private def quizRoutes(quiz: QuizApiTrait) : Route = {
    quiz.login
  }

}
