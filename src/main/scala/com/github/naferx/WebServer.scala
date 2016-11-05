package com.github.naferx


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory


object WebServer extends App {
    implicit val system = ActorSystem("HttpServer")
    val log = system.log
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val applicationConf = ConfigFactory.load()
    val serverConfig = new ServerConfig(applicationConf)

    val route =
      path("hello") {
        logRequest("hello-resource") {
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, serverConfig.interface, serverConfig.port)

    log.info(s"Server online at http://${serverConfig.interface}:${serverConfig.port}/\nPress RETURN to stop...")
}