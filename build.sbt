
lazy val Versions = new {
  val typesafeConfig = "1.3.1"
  val scalaLogging = "3.5.0"
  val logback = "1.2.1"
  val parboiled = "2.1.4"
  val hadoop = "2.6.0"
  val hbase = "1.3.1"
  val scalaTest = "3.0.0"
}

lazy val hbase4s = (project in file("."))
  .settings(
    scalaVersion := "2.11.8",
    name := "hbase4s",
    moduleName := "hbase4s-core",
    version := "0.1.1",
    useGpg := true,
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    homepage := Some(url("http://github.com/hbase4s")),
    scmInfo := Some(
      ScmInfo(
        url("http://github.com/hbase4s/hbase4s"),
        "scm:git@github.com:hbase4s/hbase4s.git"
      )
    ),
    developers := List(
      Developer(
        id = "vglushak-vt",
        name = "Volodymyr Glushak",
        email = "noreply@email",
        url = url("http://github.com/vglushak-vt")
      )

    ),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := true,
    scalacOptions in ThisBuild ++= Seq(
      "-language:experimental.macros",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-language:reflectiveCalls",
      "-language:experimental.macros",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:postfixOps",
      "-deprecation", // warning and location for usages of deprecated APIs
      "-feature", // warning and location for usages of features that should be imported explicitly
      "-unchecked", // additional warnings where generated code depends on assumptions
      "-Xlint", // recommended additional warnings
      "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
      "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures
      "-Ywarn-dead-code", // Warn when dead code is identified
      "-Ywarn-unused", // Warn when local and private vals, vars, defs, and types are unused
      "-Ywarn-unused-import", //  Warn when imports are unused (don't want IntelliJ to do it automatically)
      "-Ywarn-numeric-widen" // Warn when numerics are widened
    ),
    libraryDependencies ++= Seq(

      "com.typesafe" % "config" % Versions.typesafeConfig,
      "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging,


      "org.parboiled" %% "parboiled" % Versions.parboiled,

      "ch.qos.logback" % "logback-classic" % Versions.logback,
      "org.apache.hadoop" % "hadoop-common" % Versions.hadoop,
      "org.apache.hbase" % "hbase-common" % Versions.hbase,
      "org.apache.hbase" % "hbase-client" % Versions.hbase,

      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test,

      // below are set of libraries required for testing HBase with testing server.
      "org.apache.hadoop" % "hadoop-common" % Versions.hadoop % Test classifier "tests",
      "org.apache.hadoop" % "hadoop-hdfs" % Versions.hadoop % Test classifier "tests",
      "org.apache.hadoop" % "hadoop-hdfs" % Versions.hadoop % Test,

      "org.apache.hbase" % "hbase-common" % Versions.hbase % Test classifier "tests",
      "org.apache.hbase" % "hbase-client" % Versions.hbase % Test classifier "tests",
      "org.apache.hbase" % "hbase-server" % Versions.hbase % Test,
      "org.apache.hbase" % "hbase-server" % Versions.hbase % Test classifier "tests",
      "org.apache.hbase" % "hbase-hadoop-compat" % Versions.hbase % Test,
      "org.apache.hbase" % "hbase-hadoop2-compat" % Versions.hbase % Test,
      "org.apache.hbase" % "hbase-hadoop-compat" % Versions.hbase % Test classifier "tests",
      "org.apache.hbase" % "hbase-hadoop2-compat" % Versions.hbase % Test classifier "tests"
    )
  )
