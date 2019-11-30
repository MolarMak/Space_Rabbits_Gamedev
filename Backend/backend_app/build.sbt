// *****************************************************************************
// Dependencies https://github.com/Freshwood/akka-http-slick-sample/blob/master/build.sbt
// *****************************************************************************
lazy val dependency = new {

  object Version {
    val akka = "2.5.18"
    val akkaHttp = "10.1.5"
    val circeVersion = "0.9.3"
    val scalaTest = "3.0.5"
    val slick = "3.2.3"
  }

  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % Version.scalaTest

  lazy val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-stream" % Version.akka,
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % Test
  )

  val akkaHttp: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % Version.akkaHttp,
    "com.typesafe.akka" %% "akka-http-core" % Version.akkaHttp,
    "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % "test",
    "de.heikoseeberger" %% "akka-http-circe" % "1.21.0"
  )
  
  val circe: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core" % Version.circeVersion,
    "io.circe" %% "circe-generic" % Version.circeVersion,
    "io.circe" %% "circe-parser" % Version.circeVersion,
    "io.circe" %% "circe-java8" % Version.circeVersion
  )

  val slick: Seq[ModuleID] = Seq(
    "com.typesafe.slick" %% "slick" % Version.slick,
    "com.typesafe.slick" %% "slick-hikaricp" % Version.slick,
    "org.postgresql" % "postgresql" % "9.4.1211",
    "com.h2database" % "h2" % "1.4.192" % "test",
    "org.slf4j" % "slf4j-nop" % "1.6.4"
  )
}

// *****************************************************************************
// Settings
// *****************************************************************************

val scalaSettings = Seq(
  version := "1.0.1",
  scalaVersion := "2.12.8",
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  ),
  javacOptions ++= Seq("-source", "1.8"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Xfuture"
  ),
)

lazy val model =
  Project(id = "model", base = file("model"))
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= dependency.slick ++ dependency.circe
    )

lazy val controller =
  Project(id = "controller", base = file("controller"))
    .dependsOn(model)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= dependency.slick ++ dependency.circe ++ dependency.akkaHttp
    )

lazy val view =
  Project(id = "view", base = file("view"))
    .dependsOn(controller)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= dependency.akkaHttp
    )

lazy val root =
  Project("backend_app", file("."))
    .aggregate(view)