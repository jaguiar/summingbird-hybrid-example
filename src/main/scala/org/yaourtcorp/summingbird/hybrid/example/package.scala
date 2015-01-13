package org.yaourtcorp.summingbird.hybrid

/**
 * The "example" package contains all of the code and configuration necessary to run a basic Summingbird job locally that consumes objects from a Kafka stream and generates "something" ("computes something").
 *
 * # Code Structure
 *
 * ## StringToBytesSerialization.scala
 *
 * defines a number of serialization Injections needed by the Storm and Scalding platforms to ensure that data can move across network boundaries without corruption.
 *
 * ## AbstractJob.scala
 *
 * An abstract class that defines a Summingbird job, plus a couple of helper implicits (a batcher and a time extractor) necessary for running jobs in combined batch/realtime mode across Scalding and Storm.
 *
 * ## AbstractStormRunner.scala
 *
 * Generic code for configuring and executing a summingbird job in Storm's local mode.
 *
 * ## yogurt package
 * 
 * Everything for running jobs related to yogurts (online done, offline TBD)
 * 
 * ### Yogurt
 *
 * The yogurt object to deserialize from a json string (kafka, online job) or a json file (hdfs, offline job)
 * 
 * ### YogurtsPerManufacturerJob.scala
 *
 * The actual Summingbird job for counting "yogurts produced per manufacturer"
 *
 * ### YogurtStormRunner.scala
 *
 * Configuration and Execution of the summingbird yogurts per manufacturer job in Storm's local mode, plus some advice on how to test that Storm is
 * populating the Memcache store with good revenues.
 *
 * # Have Fun!
 */

import org.json4s._
import org.json4s.ext.JodaTimeSerializers

import org.yaourtcorp.summingbird.hybrid.example.utils.{ ISO8601DateFormatter, ApplicationSettings }

package object example {

  val settings = ApplicationSettings

  implicit val formats = new DefaultFormats {
    override val dateFormat = new ISO8601DateFormatter()
  }.withBigDecimal ++ JodaTimeSerializers.all

  
}