/*

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yaourtcorp.summingbird.hybrid.example


import backtype.storm.{ Config => BTConfig }
import com.typesafe.scalalogging.slf4j.LazyLogging
import com.twitter.algebird.Monoid
import com.twitter.bijection.{ Bufferable, Codec, Injection }
import com.twitter.scalding.Args
import com.twitter.storehaus.memcache.MemcacheStore
import com.twitter.summingbird.batch.Batcher 
import com.twitter.summingbird.batch.BatchID
import com.twitter.summingbird.Options
import com.twitter.summingbird.option.CacheSize
import com.twitter.summingbird.TailProducer
import com.twitter.summingbird.storm.{ StormStore, Storm, StormExecutionConfig }
import com.twitter.summingbird.storm.option.{ FlatMapParallelism, SummerParallelism, SpoutParallelism }
import com.twitter.tormenta.scheme.Scheme
import com.twitter.tormenta.spout.KafkaSpout
import com.twitter.util.Duration

/**
 * The following abstract class contains generic code to execute a Summingbird job on a storm cluster.
 */
abstract class AbstractStormRunner[MAIN_OBJECT_TYPE, RESULT_KEY_TYPE, RESULT_VALUE_TYPE]
(implicit kInj: Injection[(RESULT_KEY_TYPE, BatchID), Array[Byte]], vInj: Injection[RESULT_VALUE_TYPE, Array[Byte]], monoid: Monoid[RESULT_VALUE_TYPE], batcher:Batcher) 
  extends LazyLogging with Serializable  {
  /**
   * These imports bring the requisite serialization injections, the
   * time extractor and the batcher into implicit scope. This is
   * required for the dependency injection pattern used by the
   * Summingbird Storm platform.
   */
  import XToBytesSerialization._

  val kafkaScheme = Scheme { bytes => Some(parseObject(bytes)) }
  /**
   * Values to override in implementation
   */
  val appId: String
  val configName: String
  val storeKeyPrefix: String

  /**
   * Methods to override in implementation
   */
  def graphFunction(spout: KafkaSpout[MAIN_OBJECT_TYPE], storeSupplier: StormStore[RESULT_KEY_TYPE, RESULT_VALUE_TYPE]): TailProducer[Storm, Any]

  def parseObject(bytes: Array[Byte]): MAIN_OBJECT_TYPE
  
  /**
   * "spout" is a concrete Storm source for Yogurt data. This will
   * act as the initial producer of Yogurt instances in the
   * Summingbird revenue per customer job.
   */
  val spout = new KafkaSpout(
    kafkaScheme,
    settings.kafkaZkHostString,
    settings.kafkaBrokerZkPath,
    settings.kafkaTopic,
    appId,
    settings.kafkaZkRoot)

  /**
   * And here's our MergeableStore supplier.
   *
   * A supplier is required (vs a bare store) because Storm
   * serializes every constructor parameter to its
   * "bolts". Serializing a live memcache store is a no-no, so the
   * Storm platform accepts a "supplier", essentially a function0
   * that when called will pop out a new instance of the required
   * store. This instance is cached when the bolt starts up and
   * starts merging tuples.
   *
   * A MergeableStore is a store that's aware of aggregation and
   * knows how to merge in new (K, V) pairs using a Monoid[V]. The
   * Monoid[Long] used by this particular store is being pulled in
   * from the Monoid companion object in Algebird. (Most trivial
   * Monoid instances will resolve this way.)
   *
   * First, the backing store:
   */
  lazy val store =
    MemcacheStore.mergeable[(RESULT_KEY_TYPE, BatchID), RESULT_VALUE_TYPE](MemcacheStore.defaultClient(settings.memcachedName, settings.memcachedHost, 2, Duration.fromSeconds(500)), storeKeyPrefix)
  /**
   * the param to store is by name, so this is still not created created
   * yet
   */
  val storeSupplier: StormStore[RESULT_KEY_TYPE, RESULT_VALUE_TYPE] = Storm.store(store)

  /**
   * This function will be called by the storm runner to request the info
   * of what to run. In local mode it will start up as a
   * separate thread on the local machine, pulling tweets off of the
   * TwitterSpout, generating and aggregating key-value pairs and
   * merging the incremental counts in the memcache store.
   *
   * Before running this code, make sure to start a local memcached
   * instance with "memcached". ("brew install memcached" will get
   * you all set up if you don't already have memcache installed
   * locally.)
   */

  def apply(args: Args): StormExecutionConfig = {
    new StormExecutionConfig {
      override val name = configName

      // No Ackers
      override def transformConfig(config: Map[String, AnyRef]): Map[String, AnyRef] = {
        config ++ List((BTConfig.TOPOLOGY_ACKER_EXECUTORS -> (new java.lang.Integer(0))))
      }

      override def getNamedOptions: Map[String, Options] = Map(
        "DEFAULT" -> Options().set(SummerParallelism(1)) //2
          .set(FlatMapParallelism(1)) //80
          .set(SpoutParallelism(1)) //60
          .set(CacheSize(100)))
      override def graph = graphFunction(spout, storeSupplier)
    }
  }
  
}
