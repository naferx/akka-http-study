package com.github.naferx


import java.util.UUID

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleExceptions, path, _}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.BasicDirectives.provide
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.naferx.directives.`X-Request-Id`
import com.github.naferx.request.{ProductoMessage, ScalaPBMarshalling}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol._
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

import scala.concurrent.Future
import scala.io.StdIn

final case class Producto(id: Int, nombre: String)

final case class Cliente(id: Int, nombre: String)

final case class Post2(userId: Int, id: Int, title: String, body: String)

final case class DeserializationException(message: String) extends java.lang.Exception(message)

// http://stackoverflow.com/questions/39440028/how-do-i-serialise-deserialise-protobuf-in-akka-http
object WebServer extends App with ClientsApi with ScalaPBMarshalling {
  implicit val system = ActorSystem("HttpServer")
  val log: LoggingAdapter = system.log
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val applicationConf = ConfigFactory.load()
  val serverConfig = new ServerSettings(applicationConf)


  implicit val readerProducto = jsonFormat2(Producto)
  implicit val writerCliente = jsonFormat2(Cliente)
  implicit val poster = jsonFormat4(Post2)

  implicit val productoProto = scalaPBFromRequestUnmarshaller(ProductoMessage)


  def extractRequestId: PartialFunction[HttpHeader, String] = {
    case h: `X-Request-Id` => h.value
  }


  /*val extract: Directive[Tuple1[String]] = optionalHeaderValuePF(extractRequestId).map {
    case Some(requestId) => requestId
    case None            => UUID.randomUUID().toString
  }*/

  /*val extract = optionalHeaderValueByType[`X-Request-Id`]().map {
    case Some(requestId) => requestId
    case None            => UUID.randomUUID().toString
  }*/

  val extract: Directive[Tuple1[String]] = optionalHeaderValueByName(`X-Request-Id`.name).map {
    case Some(requestId) =>
      val components = requestId.split("-")
      if (components.length != 5) throw new IllegalArgumentException("Invalid UUID string: " + requestId)
      else
        requestId
    case None => UUID.randomUUID().toString
  }


  private def optionalHeader(lowerCaseName: String): HttpHeader ⇒ Option[HttpHeader] = {
    case e@HttpHeader(`lowerCaseName`, x) ⇒ Some(e)
    case _ ⇒ None
  }


  def headerByName: Directive1[HttpHeader] =
    headerValue(optionalHeader(`X-Request-Id`.name.toLowerCase)) // | reject(MissingHeaderRejection(`X-Request-Id`.name))

  def headerByName2: Directive1[HttpHeader] =
    headerValue(optionalHeader(`X-Request-Id`.name.toLowerCase))


  def myHeader: Directive[Tuple1[HttpHeader]] =
    headerByName | provide(`X-Request-Id`(UUID.randomUUID().toString))

  val loggingRequest: Directive[Unit] =

    extractRequestContext.flatMap { ctx =>
      extractClientIP.flatMap { client =>
        myHeader.flatMap { (correlationId: HttpHeader) =>
          mapRequest { request =>
            ctx.log.info(s"${correlationId} ${ctx.request.method.name} ${ctx.request.uri.path} ${client.toOption.map(_.getHostAddress).getOrElse("unknown")}")
            // val p = request.headers :+ `X-Request-Id`(correlationId)
            //request.copy(headers = p)
            request
          }
        }
      }
    } & handleRejections(RejectionHandler.default)


  implicit object DeserializationExceptionFormat extends DefaultJsonProtocol with RootJsonFormat[DeserializationException] {

    def write(e: DeserializationException) = JsObject("message" -> JsString(e.getMessage))

    def read(v: JsValue) = throw new NotImplementedError()

  }

  val myExceptionHandler = ExceptionHandler {
    case e: DeserializationException => complete(StatusCodes.BadRequest, e)
    case _: ArithmeticException =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally")
        complete(HttpResponse(akka.http.scaladsl.model.StatusCodes.InternalServerError, entity = "Bad numbers, bad result!!!"))
      }
  }


  val route = handleExceptions(myExceptionHandler) {
    loggingRequest {
      path("hello") {
        logRequest("hello-resource") {

          get {
            myHeader { (header: HttpHeader) =>
              log.error(s">>> $header")
              respondWithHeader(header) {
                // throw DeserializationException("error deserializing")
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
              }
            }
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
      } ~ {
        pathPrefix("polizas") {
          get {
            parameters("fecha".as[String], "placa".as[String]) { (f, p) =>
              complete(s"fecha: $f placa: $p")
            }
            /* | parameters("fecha".as[String], "tipo_documento".as[String], "numero_documento".as[String]) { (f, t, n) =>

                            complete(s"fecha: $f tipo: $t numero: $n")
                        }*/
          }
        }
      }
    }
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

  // val binding = Http().bindAndHandle(route, serverConfig.interface, serverConfig.port)


  val binding =
    Http().bindAndHandle(route, serverConfig.interface, serverConfig.port)
  println(
    s"Server online at http://${serverConfig.interface}:${serverConfig.port}/\nPress RETURN to stop...")
  StdIn.readLine()
  binding.flatMap(_.unbind()).onComplete(_ => system.terminate())

  /*
   binding.onComplete {
     case scala.util.Success(binding) ⇒
       val localAddress: InetSocketAddress = binding.localAddress
       log.info(s"Server online at http://${localAddress.getHostName}:${localAddress.getPort}/\nPress RETURN to stop...")
     case scala.util.Failure(e) ⇒
       println(s"Binding failed with ${e.getMessage}")
       system.terminate()

   }*/


}
