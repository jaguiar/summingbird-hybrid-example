/**
 *
 */
package org.yaourtcorp.summingbird.hybrid.example
package yogurt

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.joda.time.DateTime
import com.twitter.summingbird.Producer
import com.twitter.summingbird.Platform
import com.twitter.summingbird.memory.Memory

/**
 * @author jaguiar
 *
 */
@RunWith(classOf[JUnitRunner])
class YogurtsPerManufacturerJobSpec extends AbstractUnitSpec {

  val yogurtsPerManufacturerJob = YogurtsPerManufacturerJob 
  
  "YogurtsPerManufacturerJob" should "return creationTime" in {
    val creationTime = DateTime.now()
    val yogurt = new Yogurt("uuddlrlrba","constructive_carrot", 42, creationTime)
      yogurtsPerManufacturerJob.timeExtractorFunction(yogurt) shouldBe creationTime.getMillis
  }
  
  it should "create a new entry for a yogurt of a manufacturer never seen before" in {
//    val mockedSource = mock[Producer[Memory, Yogurt]]
//    val mockedStore = mock[Memory#Store[String, Long]]
//    yogurtsPerManufacturerJob.job[Memory](mockedSource, mockedStore)
  }
}