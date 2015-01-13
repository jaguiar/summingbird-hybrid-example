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


import com.typesafe.scalalogging.slf4j.LazyLogging
import com.twitter.algebird.Monoid
import com.twitter.bijection.{ Bufferable, Codec, Injection }
import com.twitter.storehaus.memcache.MemcacheStore
import com.twitter.summingbird.batch.Batcher
import com.twitter.summingbird.batch.BatchID
import com.twitter.summingbird.scalding.store.{InitialBatchedStore,VersionedStore}

/**
 * The following abstract class contains generic code to execute a Summingbird job on a hadoop/scalding cluster.
 */
abstract class AbstractScaldingRunner[MAIN_OBJECT_TYPE, RESULT_KEY_TYPE, RESULT_VALUE_TYPE]
(implicit kInj: Injection[(RESULT_KEY_TYPE, BatchID), Array[Byte]], vInj: Injection[(BatchID, RESULT_VALUE_TYPE), Array[Byte]], oInj: Injection[RESULT_KEY_TYPE, Array[Byte]], ord: Ordering[RESULT_KEY_TYPE], batcher:Batcher) 
  extends LazyLogging with Serializable  {
  /**
   * These imports bring the requisite serialization injections, the
   * time extractor and the batcher into implicit scope. This is
   * required for the dependency injection pattern used by the
   * Summingbird Storm platform.
   */
  import XToBytesSerialization._

  /**
   * Values to override in implementation
   */
  val storeKeyPrefix: String

  /**
   * Methods to override in implementation
   */
  
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
  /**
    * Store for results of batches.
    *
    * This is an HDFS SequenceFile which is efficient to write to,
    * but not appropriate for serving (no random key access)
    */

  val versionedStore = VersionedStore[RESULT_KEY_TYPE, RESULT_VALUE_TYPE](settings.jobDir + "/store")
  // won't worry about any historical data for this demo
  val store = new InitialBatchedStore(batcher.currentBatch, versionedStore)
  /**
    * For serving, we need to load the results of the batch into
    * a ReadableStore.  Normally this would be a persistent store,
    * but just using Memcache here for simplicity.
    */
  lazy val servingStore =
    MemcacheStore.typed[RESULT_KEY_TYPE, (BatchID, RESULT_VALUE_TYPE)](MemcacheStore.defaultClient(settings.memcachedName, settings.memcachedHost), storeKeyPrefix)

}
