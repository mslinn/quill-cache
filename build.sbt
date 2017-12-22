import sbt.Keys._

// Maintainers: Use sbt publishM2 to publish to ~/.m2/local; sbt publish-local cannot publish maven style

val useQuillSnapshot = false

organization := "com.micronautics"
name := "quill-cache"
version := "3.5.8"
licenses +=  ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
scalaVersion := "2.11.11"
crossScalaVersions := Seq("2.11.11", "2.12.4")

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
//    "-Xlog-implicits",     // verbose but useful when quill mappings are at issue
//    "-Xlog-implicit-conversions",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Xlint"
  )

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/quill-cache/tree/master€{FILE_PATH}.scala"
  )
}.value

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

resolvers ++= Seq(
  "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala"
)

val quillVer: String = if (useQuillSnapshot) {
  resolvers += Resolver.sonatypeRepo("snapshots")
  "2.3.2-SNAPSHOT"
} else "2.3.1"

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time"          % "2.18.0"   withSources(),
  "com.google.guava"       %  "guava"                % "23.5-jre" withSources(),
  "com.micronautics"       %% "has-id"               % "1.2.8"    withSources(),
  "io.getquill"            %% "quill-async-mysql"    % quillVer   withSources(),
  "io.getquill"            %% "quill-async-postgres" % quillVer   withSources(),
  "io.getquill"            %% "quill-jdbc"           % quillVer   withSources(),
  "net.codingwell"         %% "scala-guice"          % "4.1.0"    withSources(),
  "ch.qos.logback"         %  "logback-classic"      % "1.2.3",
  //
  "com.h2database"         %  "h2"                   % "1.4.196"  % Test withSources(),
  "junit"                  %  "junit"                % "4.12"     % Test,
  "org.postgresql"         %  "postgresql"           % "42.1.4"   % Test,
  "org.xerial"             %  "sqlite-jdbc"          % "3.20.1"   % Test withSources(),
  "org.scalatest"          %% "scalatest"            % "3.0.4"    % Test withSources()
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Debug

val commonInitialCommands =
  """import java.net.URL
    |import java.util.UUID
    |import com.github.nscala_time.time.Imports._
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

cancelable := true
