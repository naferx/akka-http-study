package com.github.naferx

import com.typesafe.config.Config


final class ServerConfig(rootConfig: Config) {
  private lazy val config = rootConfig.getConfig("server")

  val interface: String = config.getString("interface")
  val port: Int = config.getInt("port")
}
