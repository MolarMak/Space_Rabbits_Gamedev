import akka.http.scaladsl.server.Route

package object controllers {

  trait ControllerTrait {
    def onError(errors: List[String])
  }

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

}
