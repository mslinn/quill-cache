val vcs = "bitbucket"

// bintray settings
bintrayOrganization := Some("micronautics")
bintrayRepository := "scala"
bintrayPackageLabels := Seq("aws", "scala")
bintrayVcsUrl := Some(s"git@$vcs.org:mslinn/${ name.value }.git")

// sbt-site settings
enablePlugins(SiteScaladocPlugin)
siteSourceDirectory := target.value / "api"
publishSite

// sbt-ghpages settings
enablePlugins(GhpagesPlugin)
git.remoteRepo := s"git@$vcs.com:mslinn/${ name.value }.git"
