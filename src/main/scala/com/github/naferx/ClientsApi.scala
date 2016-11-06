package com.github.naferx

import akka.http.scaladsl.server.Directives._


trait ClientsApi {

  val clientsRoutes = path("hi") {
    get {
      complete("completado")
    }
  }

}
