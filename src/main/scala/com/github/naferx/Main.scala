package com.github.naferx

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

object Main extends App {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val paths = {
    path("path") {
      complete("akka http server")
    }
  }

  Http().bindAndHandle(paths, "localhost", 8080)

  println(s"Server online at http://localhost:8080")
}
