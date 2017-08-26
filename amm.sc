import ammonite.repl.tools.Resolver
import coursier.MavenRepository
import coursier.ivy.IvyRepository.fromPattern
import coursier.Cache.ivy2Local
val sonatypeSnapshots = Resolver.Http(
  "Sonatype OSS Snapshots", 
  "https://oss.sonatype.org/content/repositories/snapshots",
  MavenPattern,  
  true
)
interp.repositories() ++= Seq(
  ivy2Local, 
  sonatypeSnapshots
)
import $ivy.`"io.getquill::quill-jdbc:1.3.1-SNAPSHOT`, io.getquill._

