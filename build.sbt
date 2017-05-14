import com.trueaccord.scalapb.{ ScalaPbPlugin => PB }

name := "akka-http-study"

version := "1.0.0"
scalaVersion := "2.11.8"

val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.6"

dockerExposedPorts := Seq(8082)
enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
    "com.typesafe.akka"         %%    "akka-http"                  %    akkaHttpVersion,
    "com.typesafe.akka"         %%    "akka-http-spray-json"       %   akkaHttpVersion,
    "com.typesafe.akka"         %%    "akka-slf4j"                 %    akkaVersion,
    "ch.qos.logback"            %     "logback-classic"            %    "1.1.7",

    // Utils
    "com.typesafe"              %     "config"                     %    "1.3.1",
    "com.trueaccord.scalapb"  %%      "scalapb-runtime"            %     "0.5.34" % PB.protobufConfig,
    "com.trueaccord.scalapb"  %%      "scalapb-json4s"            %     "0.1.1",


    // Testing
    "org.scalatest"             %%    "scalatest"                  %    "3.0.0"      %  Test,
    "com.typesafe.akka"         %%    "akka-testkit"               %    akkaVersion  %  Test
)


PB.protobufSettings
PB.runProtoc in PB.protobufConfig := {
  args => com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
}
PB.flatPackage in PB.protobufConfig := true
version in PB.protobufConfig := "3.0.0"