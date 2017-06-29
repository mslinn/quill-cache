import sbt.Keys._

organization := "com.micronautics"
name := "quill-cache"
version := "3.0.3"
licenses +=  ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
val scalaVer = "2.11.11"
crossScalaVersions := Seq("2.11.11", "2.12.2")

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/quill-cache/tree/master€{FILE_PATH}.scala"
  )
}.value

val quillVer = "1.2.1"

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := scalaVer,
  scalacOptions ++=
    scalaVersion {
      case sv if sv.startsWith("2.10") => List(
        "-target:jvm-1.7"
      )

      case _ => List(
        "-target:jvm-1.8",
        "-Ywarn-unused"
      )
    }.value ++ Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture",
      "-Xlint"
    ),
  javacOptions ++=
    scalaVersion {
      case sv if sv.startsWith("2.10") => List(
        "-source", "1.7",
        "-target", "1.7"
      )

      case _ => List(
        "-source", "1.8",
        "-target", "1.8"
      )
    }.value ++ Seq(
      "-Xlint:deprecation",
      "-Xlint:unchecked",
      "-g:vars"
    ),
  resolvers ++= Seq(
    "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala"
//    Resolver.sonatypeRepo("snapshots")
  )
)

lazy val macrosModule = project.in(file("macro"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect"         % scalaVer,
      "org.scala-lang" % "scala-compiler"        % scalaVer,
     // probably don't need any of the following dependencies
      "io.getquill"    %% "quill-async-postgres" % quillVer,
      "io.getquill"    %% "quill-jdbc"           % quillVer,
      "org.postgresql" %  "postgresql"           % "9.4.1208"
    )
  )

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback"         %  "logback-classic"      % "1.2.3"   withSources(),
      "com.github.nscala-time" %% "nscala-time"          % "2.16.0"  withSources(),
      "com.google.guava"       %  "guava"                % "19.0"    withSources(),
      "com.micronautics"       %% "has-id"               % "1.2.5"   withSources(),
      "io.getquill"            %% "quill-jdbc"           % quillVer  withSources(),
      "io.getquill"            %% "quill-async-postgres" % quillVer  withSources(),
      "net.codingwell"         %% "scala-guice"          % "4.1.0"   withSources(),
      "org.joda"               %  "joda-convert"         % "1.6"     withSources(),
      "com.h2database"         %  "h2"                   % "1.4.192" withSources(),
      "org.postgresql"         %  "postgresql"           % "42.1.1"  withSources(),
      //
      "junit"                  %  "junit"                % "4.12"    % Test withSources(),
      "org.scalatest"          %% "scalatest"            % "3.0.1"   % Test withSources()
    )
  )
  .dependsOn(macrosModule)

publishArtifact in (Compile, packageSrc) := false

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |""".stripMargin

cancelable := true
