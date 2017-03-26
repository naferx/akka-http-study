package com.github.naferx


import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.naferx.request.{ProductoMessage, ScalaPBMarshalling}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

final case class Producto(id: Int, nombre: String)

final case class Cliente(id: Int, nombre: String)

final case class Post2(userId: Int, id: Int, title: String, body: String)


// http://stackoverflow.com/questions/39440028/how-do-i-serialise-deserialise-protobuf-in-akka-http
object WebServer extends App with ClientsApi with ScalaPBMarshalling {
  implicit val system = ActorSystem("HttpServer")
  val log = system.log
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val applicationConf = ConfigFactory.load()
  val serverConfig = new ServerSettings(applicationConf)


  implicit val readerProducto = jsonFormat2(Producto)
  implicit val writerCliente = jsonFormat2(Cliente)
  implicit val poster = jsonFormat4(Post2)

  implicit val productoProto = scalaPBFromRequestUnmarshaller(ProductoMessage)

  val route =
    path("hello") {
      logRequest("hello-resource") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        }
      }
    } ~
      clientsRoutes ~
      path("productojson")(
        (post & entity(as[Producto])) { producto =>
          complete(producto)
        }
      ) ~
      path("productoproto")(
        (post & entity(as[ProductoMessage])) { producto =>
          complete(producto)
        }
      ) ~
      (path("parametros") & get) {
        parameters("id".as[String], "name".as[Int]) { (id, name) =>
          complete(id + name)
       } ~ parameters("saludo".as[String]) { (saludo) =>
          complete("Hey " + saludo)
        }
      } ~ path("greeter") {
      handleWebSocketMessages(greeter)
    } ~ path("client") {
      complete(consume())
    }


  def consume(): Future[Post2] = {
    val httpRequest = Source.single(RequestBuilding.Get(uri = "/posts/1"))
    val flow = Http().outgoingConnectionHttps(host = "jsonplaceholder.typicode.com", port = 443)
      .mapAsync(1) {
        response =>
          log.info(s"Got unsuccessful response $response")
          Unmarshal(response.entity).to[Post2]
      }
    httpRequest.via(flow).runWith(Sink.head)
  }

  def greeter: Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
      case tm: TextMessage =>
//        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
        import spray.json._

        TextMessage(Producto(12, "dasd").toJson.prettyPrint) :: Nil
//        TextMessage(scalaPBToEntityMarshaller Producto(12, "leche")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  val binding = Http().bindAndHandle(route, serverConfig.interface, serverConfig.port)

  binding.onComplete {
    case scala.util.Success(binding) ⇒
      val localAddress: InetSocketAddress = binding.localAddress
      log.info(s"Server online at http://${localAddress.getHostName}:${localAddress.getPort}/\nPress RETURN to stop...")
    case scala.util.Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }


}
