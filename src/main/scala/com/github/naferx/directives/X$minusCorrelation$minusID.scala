package com.github.naferx.directives

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.Try

final class `X-Correlation-ID`(id: String) extends ModeledCustomHeader[`X-Correlation-ID`] {
  override def renderInRequests = false
  override def renderInResponses = false
  override val companion = `X-Correlation-ID`
  override def value: String = id
}

object `X-Correlation-ID` extends ModeledCustomHeaderCompanion[`X-Correlation-ID`] {
  override val name = "X-Request-Id"
  override def parse(value: String) = Try(new `X-Correlation-ID`(value))
}