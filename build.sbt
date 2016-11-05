name := "akka-http-study"

version := "1.0.0"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.12"

dockerExposedPorts := Seq(8082)
enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
    "com.typesafe.akka"       %%    "akka-http"                  %    "3.0.0-RC1",
    "com.typesafe.akka"       %%    "akka-slf4j"                 %    akkaVersion,
    "ch.qos.logback"          %     "logback-classic"            %    "1.1.7",

    // Utils
    "com.typesafe"            %     "config"                     %    "1.3.1",

    // Testing
    "org.scalatest"           %%    "scalatest"                  %    "3.0.0"      %  Test,
    "com.typesafe.akka"       %%    "akka-testkit"               %    akkaVersion  %  Test
)

