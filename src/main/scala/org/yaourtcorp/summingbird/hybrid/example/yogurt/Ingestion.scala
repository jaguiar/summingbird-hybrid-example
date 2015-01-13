package org.yaourtcorp.summingbird.hybrid.example
package yogurt

import java.io.PrintWriter
import java.util.Properties
import java.util.concurrent.Executors
import kafka.consumer.Consumer
import kafka.consumer.ConsumerConfig
import utils.ApplicationSettings._
import org.yaourtcorp.summingbird.hybrid.example._
import com.typesafe.scalalogging.slf4j.example.utils.TransientLazyLogging
import YogurtsPerManufacturerJob.batcher

object RunIngestion extends App {
  Ingestion.run
}

object Ingestion extends TransientLazyLogging {

  import YogurtsPerManufacturerJob._

  val RESTART_CONSUMER_PERIOD_MS = 60000L

  val props = new Properties()
  props.put("zookeeper.connect", settings.kafkaZkHostString)
  props.put("group.id", "summingbird-prototype-ingestion")
  props.put("zookeeper.session.timeout.ms", "1000");
  props.put("zookeeper.sync.time.ms", "200");
  props.put("auto.commit.interval.ms", "1000");

  val config = new ConsumerConfig(props);

  val consumer = Consumer.create(config)

  val streams = consumer.createMessageStreams(Map(settings.kafkaTopic -> 1)).get(settings.kafkaTopic).getOrElse {
    throw new IllegalArgumentException("Unable to connect to topic: " + settings.kafkaTopic)
  }

  val executor = Executors.newFixedThreadPool(1)

  var ingested = 0L

  def newCurrentFile = dataFileForBatch(batcher.currentBatch)

  def run(): Unit = run(() => ())

  def run(callback: () => Unit): Unit = {
		  
    logger.debug("newCurrentFile {} with currentBatch {}", newCurrentFile, batcher.currentBatch.id.toString)

    var currentFile: String = newCurrentFile
    var buffer = Seq.empty[Yogurt]

    for (stream <- streams) {
      val r = new Runnable() {
        override final def run: Unit = {
          try {
            stream.iterator.foreach { messageAndMetadata =>
              logger.debug("Consumed {} from kafka", messageAndMetadata)

              val yogurt = parseYogurt(messageAndMetadata.message)
              logger.debug("Parsed yogurt id {} manufacturerId {}", yogurt.uuid, yogurt.manufacturerId.toString)

              if (newCurrentFile != currentFile) {
                val writer = new PrintWriter(currentFile)

                for (pdpView <- buffer) {
                  writer.println(serializeYogurt(pdpView))
                }

                writer.close()

                callback()

                buffer = Seq.empty[Yogurt]
                currentFile = newCurrentFile
              } else {
                logger.debug(s"Same file {} for same batchId {}", currentFile, batcher.currentBatch.id.toString)
              }

              buffer +:= yogurt
              ingested += 1
              logger.debug("buffer size {}",buffer.size.toString)
            }
          } catch {
            // There was a problem talking to Kafka.  We need to handle this situation.  Let's wait a bit and try again.
            case ex: Exception => {
              logger.error("Unable to talk to Kafka.  Attempting to restart the consumer in " + RESTART_CONSUMER_PERIOD_MS + " ms.", ex)
              Thread.sleep(RESTART_CONSUMER_PERIOD_MS)
              executor.submit(this)
            }
          }
        }
      }

      executor.submit(r)
    }
  }
}
