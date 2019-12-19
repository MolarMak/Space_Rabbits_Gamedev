import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.server.Route

import scala.util.Random

package object controllers {

  val isLogged = true

  val dateFormatFile: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val timeFormatFile: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  trait ViewTrait {
    def onError(errors: List[String]) : Route
    def onAuthError(errors: List[String]) : Route
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

  def generateGameRoomId(): String = {
    val random = new scala.util.Random
    randomString("abcdefghijklmnopqrstuvwxyz0123456789", random)(24)
  }

  def log(methodName: String, message: String): Unit = {
    if(isLogged) {
      writeToLogFile("log", s"$methodName => $message")
    }
  }

  def errorLog(methodName: String, error: String): Unit = {
    if(isLogged) {
      writeToLogFile("error", s"[ERROR]: $error")
    }
  }

  private def writeToLogFile(fileName: String, text: String): Unit = {
    val path = "logs"
    if(!Files.exists(Paths.get(path))) {
      Files.createDirectory(Paths.get(path))
    }

    val date: LocalDateTime = LocalDateTime.now()
    val file = new File(s"$path/${fileName}_${dateFormatFile.format(date)}.txt")
    val fr = new FileWriter(file, true)
    fr.write(s"[${timeFormatFile.format(LocalDateTime.now())}]: $text\n")
    fr.close()
  }

}
