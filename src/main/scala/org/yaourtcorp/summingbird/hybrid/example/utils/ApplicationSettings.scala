package org.yaourtcorp.summingbird.hybrid.example
package utils

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * Here is an OPTIONAL alternative way to access settings, which
 * has the advantage of validating fields on startup and avoiding
 * typos.
 */
object ApplicationSettings {

  val config = ConfigFactory.load()

  // -Dconfig.resource={objecttype}.conf shall be set when running the program
	// note that these fields are NOT lazy, because if we're going to get any exceptions, we want to get them on startup.
	// main configuration
  val kafkaZkHostString = config.getString("yaourtcorp.kafka.zk.host")
  val kafkaBrokerZkPath = config.getString("yaourtcorp.kafka.zk.broker.path")
  val memcachedName = config.getString("yaourtcorp.memcached.name")
  val memcachedHost = config.getString("yaourtcorp.memcached.host")
  val memcachedTimeout = config.getInt("yaourtcorp.memcached.timeout")
  // specific configuration
  val jobDir = config.getString("yaourtcorp.job.dir")
  val dataDir = config.getString("yaourtcorp.data.dir")
  val kafkaTopic = config.getString("yaourtcorp.kafka.topic")
  val kafkaZkRoot = config.getString("yaourtcorp.kafka.zk.root") //Location in ZK for the Kafka spout to store state.
}