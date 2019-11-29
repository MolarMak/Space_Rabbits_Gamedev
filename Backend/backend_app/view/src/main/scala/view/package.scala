
package object view {

  val serverVersion = "1.0.1"
  val isLogged = true

  def log(methodName: String, message: String): Unit = {
    if(isLogged) {
      println(s"$methodName => $message")
    }
  }

}
