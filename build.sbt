import com.raquo.buildkit.SourceDownloader

ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := Versions.Scala_3

lazy val root = project.in(file("."))
  .aggregate(server2)
  .settings(
    name := "Sportstarts Infographics"
  )
  .settings(commonSettings)

lazy val server2 = project
  .in(file("./server2"))
  .settings(commonSettings)
  .settings(Compile / run / fork := true)
  .settings(
    libraryDependencies ++= List(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.12.2",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.12.2",
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15"
    )
  )

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    // "-feature",
    "-language:implicitConversions"
  ),
)

// -- Scalafmt

lazy val preload = taskKey[Unit]("runs Laminar-specific pre-load tasks")
preload := {
  val projectDir = (ThisBuild / baseDirectory).value
  // TODO Move code generators here as well?
  SourceDownloader.downloadVersionedFile(
    name = "scalafmt-shared-conf",
    version = "v0.1.0",
    urlPattern = version => s"https://raw.githubusercontent.com/raquo/scalafmt-config/refs/tags/$version/.scalafmt.shared.conf",
    versionFile = projectDir / ".downloads" / ".scalafmt.shared.conf.version",
    outputFile = projectDir / ".downloads" / ".scalafmt.shared.conf",
    processOutput = "#\n# DO NOT EDIT. See SourceDownloader in build.sbt\n" + _
  )
}
Global / onLoad := {
  (Global / onLoad).value andThen { state => preload.key.label :: state }
}
