package com.github.naferx.request

import akka.http.scaladsl.marshalling.{Marshaller, PredefinedToEntityMarshallers, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaType.Compressible
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, MediaType}
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, PredefinedFromEntityUnmarshallers, Unmarshaller}
import akka.http.scaladsl.util.FastFuture
import com.google.protobuf.CodedInputStream
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import com.trueaccord.scalapb.json.JsonFormat

import scala.concurrent.Future

trait ScalaPBMarshalling {
  private val protobufContentType = ContentType(MediaType.applicationBinary("octet-stream", Compressible, "proto"))
  private val applicationJsonContentType = ContentTypes.`application/json`

  /*def scalaPBFromRequestUnmarshaller[O <: GeneratedMessage with Message[O]](implicit companion: GeneratedMessageCompanion[O]): FromEntityUnmarshaller[O] = {
    Unmarshaller.withMaterializer[HttpEntity, O](_ ⇒ implicit mat ⇒ {
      case entity@HttpEntity.Strict(`applicationJsonContentType`, data) ⇒
        val charBuffer = Unmarshaller.bestUnmarshallingCharsetFor(entity)
        FastFuture.successful(JsonFormat.fromJsonString(data.decodeString(charBuffer.nioCharset().name()))(companion))
      case entity@HttpEntity.Strict(`protobufContentType`, data) ⇒
        FastFuture.successful(companion.parseFrom(CodedInputStream.newInstance(data.asByteBuffer)))
      case entity ⇒
        Future.failed(UnsupportedContentTypeException(applicationJsonContentType, protobufContentType))
    })
  }

  implicit def scalaPBToEntityMarshaller[U <: GeneratedMessage]: ToEntityMarshaller[U] = {
    def jsonMarshaller(): ToEntityMarshaller[U] = {
      val contentType = applicationJsonContentType
      Marshaller.withFixedContentType(contentType) { value ⇒
        HttpEntity(contentType, JsonFormat.toJsonString(value))
      }
    }

    def protobufMarshaller(): ToEntityMarshaller[U] = {
      Marshaller.withFixedContentType(protobufContentType) { value ⇒
        HttpEntity(protobufContentType, value.toByteArray)
      }
    }

    Marshaller.oneOf(jsonMarshaller(), protobufMarshaller())
  }*/

  implicit val ordenUnmarshaller: FromEntityUnmarshaller[ProductoMessage] =
    PredefinedFromEntityUnmarshallers.byteArrayUnmarshaller map { (bytes: Array[Byte]) =>
      ProductoMessage.parseFrom(bytes)
    }

  implicit val ordenMarshaller: ToEntityMarshaller[ProductoMessage] =
    Marshaller.withFixedContentType(protobufContentType) { value ⇒
      HttpEntity(protobufContentType, value.toByteArray)
    }


}
