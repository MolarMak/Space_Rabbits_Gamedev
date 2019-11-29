package apiVersions.v1

import akka.http.scaladsl.server.{Directives, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import view.QuizApiTrait

class QuizApiV1 extends Directives with FailFastCirceSupport with QuizApiTrait {

  val authView = new AuthView()

  def login: Route = authView.login

}
