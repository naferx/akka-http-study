package com.github.naferx


import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.io.StdIn
import scala.util.{Failure, Success}

object WebServer {
  def main(args: Array[String]) {

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

    val bindingFuture = Http().bindAndHandle(route, serverConfig.host, serverConfig.port)

    log.info(s"Server online at http://${serverConfig.host}:${serverConfig.port}/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind())  // trigger unbinding from the port
      .onComplete {
      case Success(o) =>
        system.terminate()
      case Failure(failure) =>
        log.error(s"Failed to bind to {}:{}! Error: {}", serverConfig.host, serverConfig.port, failure.getMessage)
        system.terminate()
    }
  }
}