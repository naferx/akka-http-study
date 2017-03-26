package com.github.naferx

import com.typesafe.config.Config


final class ServerSettings(config: Config) {
  private val cfg: Config = config.getConfig("server")

  // non-lazy fields, we want all exceptions at construct time
  val interface: String = cfg.getString("interface")
  val port: Int = cfg.getInt("port")
}
