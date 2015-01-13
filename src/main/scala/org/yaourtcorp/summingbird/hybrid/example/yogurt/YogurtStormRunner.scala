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
package yogurt

 /**
   * These imports bring the requisite serialization injections, the
   * time extractor and the batcher into implicit scope. This is
   * required for the dependency injection pattern used by the
   * Summingbird Storm platform.
   */
import YogurtsPerManufacturerJob.{batcher, job, timeOf}
import XToBytesSerialization.kInjection
import com.twitter.summingbird.batch.BatchID
import com.twitter.summingbird.storm.{Executor, Storm, StormStore}
import com.twitter.tormenta.spout.KafkaSpout
import com.twitter.util.Await
import com.twitter.summingbird.TailProducer
import org.json4s.jackson.Serialization.{read}

object ExeStorm {
  def main(args: Array[String]) {
    Executor(args, YogurtStormRunner(_))
  }
}

/**
 * The following object contains code to execute the Summingbird
 * WordCount job defined in ExampleJob.scala on a storm
 * cluster.
 */
object YogurtStormRunner extends AbstractStormRunner[Yogurt, Int, Long] {
 
   /**
   * Values to override in implementation
   */
  override val appId = "summingbird-example-storm"
  override val configName = "SummingbirdYogurt"
  override val storeKeyPrefix = "stormLookYogurtsPerManufacturer"
  
   def graphFunction(spout: KafkaSpout[Yogurt], storeSupplier: StormStore[Int,Long]): TailProducer[Storm,Any] = {
    job[Storm](spout, storeSupplier)
  }
   
  def parseObject(bytes: Array[Byte]): Yogurt = {
    parseYogurt(bytes)
  }
  
  /**
   * Once you've got this running in the background, fire up another
   * repl and query memcached for some sums.
   *
   * The following commands will look up for revenue per customer. Hitting an account (customer) twice
   * will show that Storm is updating Memcache behind the scenes:
   * {{{
   * scala>     lookup("3") // Or any other manufacturer id
   * res7: Option[Long] = Some(1774)
   *
   * scala>     lookup("3")
   * res8: Option[Double] = Some(1779)
   * }}}
   */
  def lookup(manufacturer: Int, optionalBatchId: Option[String] = None ): Option[Long] =
    Await.result {
    val batchId = optionalBatchId.map( batchIdString => BatchID.apply("BatchID."+ batchIdString)).getOrElse(YogurtsPerManufacturerJob.batcher.currentBatch);
      logger.info("Batcher:{}, provided batchId={}", batcher.currentBatch, batchId)
      store.get(manufacturer -> batchId)
    }

 

 
}
