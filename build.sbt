import sbt.Keys._

// Maintainers: Use sbt publishM2 to publish to ~/.m2/local; sbt publish-local cannot publish maven style

val quillVer = "3.5.0"

val useQuillSnapshot = false

cancelable := true

Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary

crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.2")

developers := List(
  Developer("mslinn",
            "Mike Slinn",
            "mslinn@micronauticsresearch.com",
            url("https://github.com/mslinn")
  )
)

//fork in Test := true

lazy val commonInitialCommands =
  """import java.net.URL
    |import java.util.UUID
    |import io.getquill._
    |import io.getquill.context.jdbc.JdbcContext
    |import scala.reflect.ClassTag
    |import model._
    |""".stripMargin

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := commonInitialCommands

// define the statements initially evaluated when entering 'test:console', 'test:console-quick', but not 'test:console-project'
initialCommands in Test in console := commonInitialCommands +
  """import model.dao.Ctx.{run => qRun, _}
    |""".stripMargin

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
  )

libraryDependencies ++= Seq(
  "com.google.guava"       %  "guava"                % "28.1-jre" withSources(),
  "com.micronautics"       %% "has-id"               % "1.3.0"    withSources(),
  "com.micronautics"       %% "scalacourses-utils"   % "0.3.5"    withSources(),
 // "io.getquill"            %% "quill-async-mysql"    % quillVer   withSources(),
 // "io.getquill"            %% "quill-async-postgres" % quillVer   withSources(),
  "io.getquill"            %% "quill-jdbc"           % quillVer   withSources(),
  "net.codingwell"         %% "scala-guice"          % "4.2.6"    withSources(),
  "ch.qos.logback"         %  "logback-classic"      % "1.2.3",
  //
  "com.h2database"         %  "h2"                   % "1.4.200"  % Test withSources(),
  "junit"                  %  "junit"                % "4.12"     % Test,
  "org.postgresql"         %  "postgresql"           % "42.2.9"   % Test,
  "org.scalatest"          %% "scalatest"            % "3.1.0"    % Test withSources(),
  "org.xerial"             %  "sqlite-jdbc"          % "3.28.0"   % Test withSources()
)

licenses +=  ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

logBuffered in Test := false

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Debug

name := "quill-cache"

organization := "com.micronautics"

parallelExecution in Test := false

resolvers ++= Seq(
  "micronautics/scala on bintray" at "https://dl.bintray.com/micronautics/scala"
)

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
    "-Xlint"
  )

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  bd: File => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/quill-cache/tree/master€{FILE_PATH}.scala"
  )
}.value

scalaVersion := "2.13.1"

scmInfo := Some(
  ScmInfo(
    url("https://github.com/mslinn/quill-cache"),
    "git@github.com:mslinn/quill-cache.git"
  )
)

version := "3.5.12"
