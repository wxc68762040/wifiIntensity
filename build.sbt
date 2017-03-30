import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport.packageMinifiedJSDependencies


val projectName = "wifiIntensity"
val projectVersion = "1.1.0"
val scalaV = "2.11.8"


val scalaXmlV = "1.0.6"
val akkaV = "2.4.17"
val akkaHttpV = "10.0.5"
val hikariCpV = "2.5.1"
val slickV = "3.1.1"
val logbackV = "1.1.7"
val nscalaTimeV = "2.14.0"
val codecV = "1.10"
val postgresJdbcV = "9.4.1208"


val scalaJsDomV = "0.9.1"
val scalatagsV = "0.6.2"


val circeVersion = "0.6.1"
val asyncHttpClientV = "2.0.24"

val playComponentV = "2.5.4"
val playJsonForAkkaHttp = "1.7.0"

val diodeV = "1.1.0"

val projectMainClass = "com.neo.sk.nyx.Boot"

def commonSettings = Seq(
  version := projectVersion,
  scalaVersion := scalaV
)


lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(name := projectName + "_shared")
  .settings(commonSettings: _*)


lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js


// Scala-Js frontend
lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(name := "frontend")
  .settings(commonSettings: _*)
  .settings(inConfig(Compile)(
    Seq(
      fullOptJS,
      fastOptJS,
      packageScalaJSLauncher,
      packageJSDependencies,
      packageMinifiedJSDependencies
    ).map(f => (crossTarget in f) ~= (_ / "sjsout"))
  ))
  .settings(skip in packageJSDependencies := false)
  .settings(
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomV withSources(),
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "me.chrons" %%% "diode" % diodeV,
      "com.github.nscala-time" %% "nscala-time" % nscalaTimeV,
      //"org.scala-js" %%% "scalajs-java-time" % scalajsJavaTimeV,
      //"com.lihaoyi" %%% "upickle" % upickleV,
      "com.lihaoyi" %%% "scalatags" % scalatagsV withSources()
      //"com.lihaoyi" %%% "utest" % "0.3.0" % "test"
    )
  ).dependsOn(sharedJs)


// Akka Http based backend
lazy val backend = (project in file("backend"))
  .settings(commonSettings: _*)
  .settings(
    Revolver.settings.settings,
    mainClass in Revolver.reStart := Some(projectMainClass)
  ).settings(name := projectName + "_backend")
  .settings(
    //pack
    // If you need to specify main classes manually, use packSettings and packMain
    packSettings,
    // [Optional] Creating `hello` command that calls org.mydomain.Hello#main(Array[String])
    packMain := Map("nyx" -> projectMainClass),
    packJvmOpts := Map("nyx" -> Seq("-Xmx2048m", "-Xms1024m")),
    packExtraClasspath := Map("nyx" -> Seq("."))
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.scala-lang" % "scala-reflect" % scalaV,
      "org.scala-lang.modules" % "scala-xml_2.11" % scalaXmlV,
      "com.typesafe.akka" %% "akka-actor" % akkaV withSources() withSources(),
      "com.typesafe.akka" %% "akka-remote" % akkaV,
      "com.typesafe.akka" %% "akka-slf4j" % akkaV,
      "com.typesafe.akka" %% "akka-stream" % akkaV,
      "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
      "com.typesafe.slick" %% "slick" % slickV withSources(),
      "com.typesafe.slick" %% "slick-codegen" % slickV,
      //"com.typesafe.play" %% "play-ws" % playComponentV,
      "com.lihaoyi" %% "scalatags" % scalatagsV,
      "com.zaxxer" % "HikariCP" % hikariCpV,
      "ch.qos.logback" % "logback-classic" % logbackV withSources(),
      "com.github.nscala-time" %% "nscala-time" % nscalaTimeV,
      "commons-codec" % "commons-codec" % codecV,
      //"com.lihaoyi" %% "upickle" % upickleV,
      "org.postgresql" % "postgresql" % postgresJdbcV,
      //      "com.typesafe.play" %% "play-ws" % playComponentV,
      "org.asynchttpclient" % "async-http-client" % asyncHttpClientV,
      //      "com.typesafe.play" %% "play-json" % playComponentV,
      //      "de.heikoseeberger" %% "akka-http-play-json" % playJsonForAkkaHttp,
      //      "io.spray" % "spray-caching_2.11" % "1.3.4",
      "com.typesafe.play" %% "play-cache" % playComponentV
    )
  )
  .settings(
    (resourceGenerators in Compile) += Def.task {
      val fastJsOut = (fastOptJS in Compile in frontend).value.data
      val fastJsSourceMap = fastJsOut.getParentFile / (fastJsOut.getName + ".map")
      Seq(
        fastJsOut,
        fastJsSourceMap
      )
    }.taskValue
  )
  //  .settings(
  //    (resourceGenerators in Compile) += Def.task {
  //      val fullJsOut = (fullOptJS in Compile in frontend).value.data
  //      val fullJsSourceMap = fullJsOut.getParentFile / (fullJsOut.getName + ".map")
  //      Seq(
  //        fullJsOut,
  //        fullJsSourceMap
  //      )
  //    }.taskValue
  //  )
  .settings(
  (resourceGenerators in Compile) += Def.task {
    Seq(
      (packageScalaJSLauncher in Compile in frontend).value.data,
      (packageJSDependencies in Compile in frontend).value
      //(packageMinifiedJSDependencies in Compile in frontend).value
    )
  }.taskValue
)
  .settings(
    (resourceDirectories in Compile) += (crossTarget in frontend).value,
    watchSources ++= (watchSources in frontend).value
  )
  .dependsOn(sharedJvm)