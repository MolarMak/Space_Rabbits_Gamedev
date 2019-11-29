val scalaSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.12.8",
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  )
)

val circeV = "0.9.3"
val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % circeV)

val slickDeps = Seq(
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.github.tminglei" %% "slick-pg" % "0.17.2"
)

val appDeps = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
  "org.jsoup" % "jsoup" % "1.11.3"
)

lazy val model =
  Project(id = "model", base = file("model"))
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= slickDeps ++ circeDeps
    )

lazy val controller =
  Project(id = "controller", base = file("controller"))
    .dependsOn(model)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= slickDeps ++ circeDeps ++ appDeps
    )

lazy val view =
  Project(id = "view", base = file("view"))
    .dependsOn(controller)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= appDeps
    )

lazy val root =
  Project("backend_app", file("."))
    .aggregate(view)