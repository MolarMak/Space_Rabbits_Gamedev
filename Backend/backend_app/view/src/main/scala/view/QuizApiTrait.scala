package view

import akka.http.scaladsl.server.Route

trait QuizApiTrait {

  def login: Route

}
