package org.yaourtcorp.summingbird.hybrid.example
package yogurt

import com.twitter.summingbird._
import com.twitter.summingbird.batch.Batcher
import com.twitter.summingbird.storm.{ MergeableStoreSupplier, Storm }
import org.joda.time.DateTime
import scala.math.BigDecimal.RoundingMode

object YogurtsPerManufacturerJob extends AbstractJob[Yogurt, Int, Long] {
  
   /**
   * These two items are required to run Summingbird in
   * batch/realtime mode, across the boundary between storm and
   * scalding jobs.
   */
  override implicit val batcher = Batcher.ofMinutes(1L)
  override val jobName = "yogurtsPerManufacturer"
    
  def timeExtractorFunction(yogurt: Yogurt): Long = {
	   yogurt.creationTime.getMillis()
//	  DateTime.now().getMillis()
	}
	
  /**
   * The actual Summingbird job. Notice that the execution platform
   * "P" stays abstract. This job will work just as well in memory,
   * in Storm or in Scalding, or in any future platform supported by
   * Summingbird.
   */
  def job[P <: Platform[P]](
    source: Producer[P, Yogurt],
    store: P#Store[Int, Long]) = {
    logger.info("Initializing Yogurt Streaming Job {}, Batcher:{}", jobName, batcher.currentBatch)
    source
      .filter(_.manufacturerId != null ) 
      .map ({
            yogurt: Yogurt => {
              logger.debug("yogurt:[productName -> {}, manufacturerId -> {}, batcher={}]", yogurt.productName, yogurt.manufacturerId.toString, batcher.currentBatch)
              yogurt.manufacturerId -> 1L 
            }
          })
      .sumByKey(store)
  }

 
}
