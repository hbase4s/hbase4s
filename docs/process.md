### Feature development

1. Create separate branch with name of feature or ticket id (from github tickets).
2. Implement test for your feature. Make sure test coverage stays the same or increase after feature developed.
3. Create pull request on develop branch and wait for it to be approved by other contributors.

### Release procedure

TODO: automate release procedure

Manual approach described below.
1. Create release branch and bump version in build.sbt. 
2. Release new binaries to sonatype nexus
`sonatypeOpen "io.github.hbase4s" "hbase4s-<version>"` - create new staging
`publishSigned` - publish binaries
Go to nexus website and release binaries from staging to maven central.
3. Back merge changes to `develop` (should be just version change)