package org.yaourtcorp.summingbird.hybrid.example
package yogurt

import com.twitter.summingbird.store.ClientStore
import com.twitter.util.Await
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.LazyLogging



/**
  * The following object contains code to execute the Summingbird
  * yogurts job defined in YogurtsPerManufacturerJob.scala on a hybrid
  * cluster.
  */
object HybridRunner extends LazyLogging {

  /**
    * These imports bring the requisite serialization injections, the
    * time extractor and the batcher into implicit scope. This is
    * required for the dependency injection pattern used by the
    * Summingbird Scalding platform.
    * 
    */
  import XToBytesSerialization._, YogurtsPerManufacturerJob._

  /**
    * The ClientStore combines results from the offline store
    * with the results for any unprocessed batches from the
    * online store.
    */
  val store = ClientStore(
    YogurtScaldingRunner.servingStore,
    YogurtStormRunner.store,
    5
  )

  def lookup(lookId: Int): Option[Long] =
    Await.result {
      store.get(lookId)
    }

  def lookupDebug(lookId: Int): Unit = {
    val offline = YogurtScaldingRunner.lookup(lookId)
    logger.info("Offline: %s".format(offline))

    Stream.iterate(offline.map(_._1).getOrElse(batcher.currentBatch))(_ + 1)
      .takeWhile(_ <= batcher.currentBatch).foreach { batch =>
      val online = Await.result {
        YogurtStormRunner.store.get(lookId -> batch)
      }
      logger.info("Online: %s".format((batch,online)))
    }

    val hybrid = lookup(lookId)

    logger.info("Hybrid: %s".format(hybrid))

  }

}


object RunHybrid extends App {
  @transient private val logger = LoggerFactory.getLogger(this.getClass)

  import java.util.concurrent.{CyclicBarrier, Executors,TimeUnit}
  import com.twitter.summingbird.storm
  import YogurtsPerManufacturerJob._

  import sys.process._

  s"rm -rf ${settings.dataDir}".!!
  s"rm -rf ${settings.jobDir}waitstate".!!
  s"rm -rf ${settings.jobDir}store".!!

  s"mkdir -p ${settings.dataDir}".!!


  val executor = Executors.newScheduledThreadPool(5)

  val barrier = new CyclicBarrier(2)

  // start ingestion
  executor.submit(new Runnable {
    def run = try { Ingestion.run(() => barrier.await()) } catch { case e: Throwable => logger.error("ingestion error", e) }
  })


  // start storm processing
  // not really sure if this needs a separate thread or if storm.Executor does that
  executor.submit(new Runnable {
    def run = try { storm.Executor(Array("--local"), YogurtStormRunner(_)) } catch { case e: Throwable => logger.error("storm error", e) }
  })


  // Run the batch job after each log file is available
  executor.submit(new Runnable {
    def run = {
      while (true) {
        barrier.await()
        try { YogurtScaldingRunner.runOnce } catch { case e: Throwable => logger.error("batch failure", e) }
      }
    }
  })


  // run sanity checks
  executor.scheduleAtFixedRate(
    new Runnable {
      def run = { try {
        logger.info("Sanity Check")
        logger.info("Events Ingested: " + Ingestion.ingested)
        
        logger.info("lookupDebug(1)")
        HybridRunner.lookupDebug(1)

        
        val ids = 1 to 10
        logger.info("Events Counted (offline): " + YogurtScaldingRunner.servingStore.multiGet(ids.toSet).map(kv => Await.result(kv._2).map(_._2).getOrElse(0L)).sum)
        logger.info("Events Counted (online): " + YogurtStormRunner.store.multiGet(ids.map(_ -> batcher.currentBatch).toSet).map(kv => Await.result(kv._2).getOrElse(0L)).sum)
        logger.info("Events Counted (hybrid): " + HybridRunner.store.multiGet(ids.toSet).map(kv => Await.result(kv._2).getOrElse(0L)).sum)
      }
      catch {
        case e: Throwable => logger.error("sanity check failure", e)
      }}
    },
    20, 20, TimeUnit.SECONDS
  )

}
