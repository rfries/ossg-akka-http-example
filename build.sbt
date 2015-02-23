name := "ossg-akka-http-example"

organization := "org.funobjects"

scalaVersion := "2.11.5"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= {
  object v {
    val akka        = "1.0-M3"
    val scalatest   = "2.2.4"
    val scalactic   = "2.2.4"
    val json4s      = "3.2.11"
  }
  Seq(
    "com.typesafe.akka"     %% "akka-http-core-experimental"        % v.akka        withSources(),
    "com.typesafe.akka"     %% "akka-stream-experimental"           % v.akka        withSources(),
    "com.typesafe.akka"     %% "akka-http-experimental"             % v.akka        withSources(),
    "org.json4s"            %% "json4s-jackson"                     % v.json4s      withSources(),
    "org.scalactic"         %% "scalactic"                          % v.scalactic   withSources(),
    "org.scalatest"         %% "scalatest"                          % v.scalatest   % "test" withSources()
  )
}

