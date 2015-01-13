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
import org.scalatest.BeforeAndAfterAll
import com.fasterxml.jackson.databind.util.ISO8601Utils
import com.fasterxml.jackson.core.JsonParseException

/**
 * @author jaguiar
 *
 */
@RunWith(classOf[JUnitRunner])
class ParseYogurtSpec extends AbstractUnitSpec with BeforeAndAfterAll {

  override def beforeAll() = {
    System.setProperty("config.resource", "yogurt.conf")
  }

  val aJsonString = "{ \"uuid\":\"uuddlrlrba\", \"manufacturerId\":42, \"productName\":\"professional_lemon\", \"creationTime\":\"2014-11-03T13:41:35.000+01:00\"}"
  val anErrorString = "{ \"uuid\":135, \"manufacturerId\":}"
  
  "Parsing a yogurt" should "serialized as a json string" in {
    //test
    val result = parseYogurt(aJsonString)

    //check
    result.isInstanceOf[Yogurt] shouldBe true
    result.uuid shouldBe "uuddlrlrba"
    result.manufacturerId shouldBe "42"
    result.productName shouldBe "professional_lemon"
    result.creationTime.getMillis shouldBe DateTime.parse("2014-11-03T13:41:35.000+01:00").getMillis()
  }

  it should "fail when there are formatting errors in string" in {
    //test
    intercept[JsonParseException] {
      parseYogurt(anErrorString)
    }
  }
  
  it should "serialized as a json string encoded as a byte array" in {
    //test
    val result = parseYogurt(aJsonString.getBytes)

    //check
    result.isInstanceOf[Yogurt] shouldBe true
    result.uuid shouldBe "uuddlrlrba"
    result.manufacturerId shouldBe "42"
    result.productName shouldBe "professional_lemon"
    result.creationTime.getMillis shouldBe DateTime.parse("2014-11-03T13:41:35.000+01:00").getMillis
  }

   it should "fail when there are formatting errors in byte array" in {
    //test
    intercept[JsonParseException] {
      parseYogurt(anErrorString.getBytes)
    }
  }
}