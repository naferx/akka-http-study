package com.github.naferx.directives

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final class `X-Request-Id`(id: String) extends ModeledCustomHeader[`X-Request-Id`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `X-Request-Id`
  override def value: String = id
}

object `X-Request-Id` extends ModeledCustomHeaderCompanion[`X-Request-Id`] {
  override val name = "X-Request-Id"
  override def parse(value: String) = Try(new `X-Request-Id`(value))
}