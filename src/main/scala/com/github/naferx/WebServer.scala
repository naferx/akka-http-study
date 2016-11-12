package com.github.naferx


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.github.naferx.request.{ProductoMessage, ScalaPBMarshalling}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol._

final case class Producto(id: Int, nombre: String)
final case class Cliente(id: Int, nombre: String)

// http://stackoverflow.com/questions/39440028/how-do-i-serialise-deserialise-protobuf-in-akka-http
object WebServer extends App with ClientsApi with ScalaPBMarshalling  {
  implicit val system = ActorSystem("HttpServer")
  val log = system.log
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val applicationConf = ConfigFactory.load()
  val serverConfig = new ServerConfig(applicationConf)

  implicit val readerProducto = jsonFormat2(Producto)
  implicit val writerCliente = jsonFormat2(Cliente)


  val route =
    path("hello") {
      logRequest("hello-resource") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }
    } ~
      clientsRoutes ~
      path("productojson") (
        (post & entity(as[Producto])) { producto =>
            complete(producto)
          }
      ) ~
      path("productoproto") (
        (post & entity(as[ProductoMessage])) { producto =>
          complete(producto)
        }
      )


  val bindingFuture = Http().bindAndHandle(route, serverConfig.interface, serverConfig.port)

  log.info(s"Server online at http://${serverConfig.interface}:${serverConfig.port}/\nPress RETURN to stop...")
}