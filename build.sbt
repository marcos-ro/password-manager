/**
 * build.sbt
 * Copyright(C) 2021 marcos-ro <marcosroropeza@prontomail.com>
 *
 * Distributed under terms of GPL license
 */

lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}

lazy val javaFXModules =
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

lazy val root  = (project in file("."))
  .settings(
    scalaVersion        := "2.13.5",
    organization        := "com.github.marcosro.passwordmanager",
    name                := "Password Manager",
    version             := "3.0.0",
    scalacOptions       ++= Seq("-Ymacro-annotations", "-deprecation"),
    libraryDependencies ++= javaFXModules.map {m =>
      "org.openjfx" % s"javafx-$m" % "15.0.1" classifier osName
    },
    libraryDependencies ++= Seq(
    // scalafx dependencies
    "org.scalafx" %% "scalafx" % "15.0.1-R21",
    "org.scalafx" %% "scalafxml-core-sfx8" % "0.5",

    // flat desing depdencies
    "io.github.palexdev" % "materialfx" % "11.12.0",

    // font icons dependencies
    "de.jensd" % "fontawesomefx-commons" % "9.1.2" % "runtime",
    "de.jensd" % "fontawesomefx-fontawesome" % "4.7.0-9.1.2",
    "de.jensd" % "fontawesomefx-weathericons" % "2.0.10-9.1.2",
    "de.jensd" % "fontawesomefx-materialicons" % "2.2.0-9.1.2",
    "de.jensd" % "fontawesomefx-controls" % "9.1.2",
    "de.jensd" % "fontawesomefx-octicons" % "4.3.0-9.1.2",

    // database dependencies
    "org.dizitart" % "nitrite" % "3.4.3",

    // unit testing dependencies
    "org.scalactic" %% "scalactic" % "3.2.9",
    "org.scalatest" %% "scalatest" % "3.2.9" % "test"
  ),
  Compile / doc / scalacOptions    ++= Seq("-skip-packages", "com.github.passwordmanager.controllers:com.github.passwordmanager.app"),
  assembly / assemblyJarName       := "password_manager.jar",
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)
