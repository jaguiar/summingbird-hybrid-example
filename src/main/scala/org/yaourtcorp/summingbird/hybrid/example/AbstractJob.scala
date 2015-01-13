
package org.yaourtcorp.summingbird.hybrid.example

import com.twitter.summingbird._
import com.twitter.summingbird.batch.Batcher
import com.twitter.summingbird.storm.{ MergeableStoreSupplier, Storm }
import com.typesafe.scalalogging.slf4j.example.utils.TransientLazyLogging

/**
 * Define a job to run on SummingBird
 */
abstract class AbstractJob[MAIN_OBJECT_TYPE, RESULT_KEY_TYPE, RESULT_VALUE_TYPE] extends TransientLazyLogging with Serializable {
  
  /**
   * These two items are required to run Summingbird in
   * batch/realtime mode, across the boundary between storm and
   * scalding jobs.
   */
  implicit val timeOf: TimeExtractor[MAIN_OBJECT_TYPE] = TimeExtractor(timeExtractorFunction)

  /**
   * Value to override in implementation
   */
  implicit val batcher: Batcher
  val jobName: String // can be useful

  /**
   * Methods to override in implementation
   */
  def timeExtractorFunction(mainObject: MAIN_OBJECT_TYPE): Long
  /**
   * The actual Summingbird job. Notice that the execution platform
   * "P" stays abstract. This job will work just as well in memory,
   * in Storm or in Scalding, or in any future platform supported by
   * Summingbird.
   */
  def job[P <: Platform[P]](
    source: Producer[P, MAIN_OBJECT_TYPE],
    store: P#Store[RESULT_KEY_TYPE, RESULT_VALUE_TYPE]): TailProducer[P, (RESULT_KEY_TYPE, (Option[RESULT_VALUE_TYPE], RESULT_VALUE_TYPE))]

}
