package org.yaourtcorp.summingbird.hybrid.example

import com.typesafe.scalalogging.slf4j.LazyLogging
import com.twitter.algebird.Monoid
import com.twitter.storehaus.ReadableStore
import com.twitter.summingbird.batch.BatchID
import com.twitter.summingbird.store.ClientStore
import com.twitter.summingbird.batch.Batcher
import com.twitter.util.Await

abstract class AbstractLookupClient[RESULT_KEY_TYPE, RESULT_VALUE_TYPE](implicit val batcher: Batcher, monoid: Monoid[RESULT_VALUE_TYPE])
  extends LazyLogging with OnlineClient[RESULT_KEY_TYPE, RESULT_VALUE_TYPE] with OfflineClient[RESULT_KEY_TYPE, RESULT_VALUE_TYPE] {

  //, val onlineStore:ReadableStore[(RESULT_KEY_TYPE, BatchID), RESULT_VALUE_TYPE], val offlineStore:ReadableStore[RESULT_KEY_TYPE, (BatchID, RESULT_VALUE_TYPE)] 
  //values may be passed as implicit in AbstractLookupClient constructor, but we need then to declare the injected values (aka "stores" in runners) implicit
  /**
    * Values that may be overriden
    */
  val batchesToKeep = 5

  val clientStore: ClientStore[RESULT_KEY_TYPE, RESULT_VALUE_TYPE] =
    ClientStore(
      offlineStore,
      onlineStore,
      batchesToKeep)

  /**
    * Query the online and offline stores to get the last value
    *
    * {{{
    * scala>     lookup("98713") // Or any other account id
    * res7: Option[Double] = Some(1774.50)
    *
    * scala>     lookup("98713")
    * res8: Option[Double] = Some(1779.80)
    * }}}
    */
  def lookup(key: RESULT_KEY_TYPE): Option[RESULT_VALUE_TYPE] =
    Await.result {
      logger.info("Key:{}", key toString)
      clientStore.get(key)
    }

  /**
    * Same but display debug information
    */
  def lookupDebug(lookId: RESULT_KEY_TYPE): Unit = {
    val offline = lookupOffline(lookId)
    logger.info("Offline: {}", offline)

    Stream.iterate(offline.map(_._1).getOrElse(batcher.currentBatch))(_ + 1)
      .takeWhile(_ <= batcher.currentBatch).foreach { batch =>
        val online = lookupOnline(lookId, batch)
        logger.info("Online: {}", (batch, online))
      }

    val hybrid = lookup(lookId)

    logger.info("Hybrid: {}", hybrid)

  }
}
/**
  * Trait for online clients, i.e. client to query online (streaming) stores
  */
trait OnlineClient[RESULT_KEY_TYPE, RESULT_VALUE_TYPE] extends LazyLogging {

  /**
    * values to define implicilty or not
    */
  val batcher: Batcher
  val onlineStore: ReadableStore[(RESULT_KEY_TYPE, BatchID), RESULT_VALUE_TYPE]

  /**
    * Once you've got this running in the background, fire up another
    * repl and query memcached for some computations.
    *
    * The following commands will look up for a computation in an onlineStore. Hitting a key twice
    * will show that Storm is updating Memcache behind the scenes.
    */
  def lookupOnline(key: RESULT_KEY_TYPE, batchId: String): Option[RESULT_VALUE_TYPE] = {
    lookupOnline(key, Option(batchId))
  }

  def lookupOnline(key: RESULT_KEY_TYPE, optionalBatchId: Option[String] = None): Option[RESULT_VALUE_TYPE] = {
    val batchId = optionalBatchId.map(batchIdString => BatchID.apply("BatchID." + batchIdString)).getOrElse(batcher.currentBatch);
    logger.info("Batcher:{}, provided batchId={}", batcher.currentBatch, batchId)
    lookupOnline(key, batchId)
  }

  def lookupOnline(key: RESULT_KEY_TYPE, batchId: BatchID): Option[RESULT_VALUE_TYPE] =
    Await.result {
      logger.info("Batcher:{}, provided batchId={}", batcher.currentBatch, batchId)
      onlineStore.get(key -> batchId)
    }
}
/**
  * Trait for offline clients, i.e. client to query offline (batch) stores
  */
trait OfflineClient[RESULT_KEY_TYPE, RESULT_VALUE_TYPE] extends LazyLogging {

  /**
    * values to define implicilty or not
    */
  val batcher: Batcher
  val offlineStore: ReadableStore[RESULT_KEY_TYPE, (BatchID, RESULT_VALUE_TYPE)]

  /**
    * Once you've got this running in the background, fire up another
    * repl and query memcached for some computations.
    *
    * The following commands will look up for a computation in an offlineStore.
    */
  def lookupOffline(key: RESULT_KEY_TYPE): Option[(BatchID, RESULT_VALUE_TYPE)] =
    Await.result {
      logger.info("Key:{}", key toString)
      offlineStore.get(key)
    }
}
