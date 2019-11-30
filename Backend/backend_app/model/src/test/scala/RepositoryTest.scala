import slick.jdbc.JdbcProfile

/**
  * The root DB component which provides information
  * how a driver has to be look -> Just a JDBC Profile
  */
trait DB {

  val driver: JdbcProfile

  import driver.api._

  lazy val db: Database = Database.forConfig("database")
}

/**
  * The final H2 in memory implementation which we can mixin
  */
trait H2 extends DB {
  override val driver: JdbcProfile = slick.jdbc.H2Profile
}