import akka.http.scaladsl.server.Route

import scala.util.Random

package object controllers {

  trait ViewTrait {
    def onError(errors: List[String]) : Route
  }

  trait DomainValidation {
    def errorMessage: String
  }

  def onValidate(rules: List[(Boolean, DomainValidation)]) : List[String] =
    rules.foldLeft(List[Option[String]]()) {
      case (seq, (rule, validator)) => seq :+ validationStage(rule, validator)
    }.flatten

  private def validationStage(rule: Boolean, domainValidation: DomainValidation): Option[String] =
    if (!rule) Some(domainValidation.errorMessage) else None

  def randomString(alphabet: String, random: Random)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(n).mkString

  def generateToken(): String = {
    val random = new scala.util.Random
    randomString("abcdefghijklmnopqrstuvwxyz0123456789", random)(32)
  }

}
