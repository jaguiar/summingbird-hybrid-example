package org.yaourtcorp.summingbird.hybrid.example

import com.twitter.summingbird.batch.BatchID
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
import org.json4s._
import org.json4s.jackson.Serialization.{ read, write }
import org.joda.time.DateTime
import utils.{ ISO8601DateFormatter, ApplicationSettings }
import java.text.SimpleDateFormat
import java.io.StringWriter
//import org.yaourtcorp.summingbird.hybrid.example._

package object yogurt {

  // Scalding methods
  val DataFileDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss")
  def dataFileForBatch(batch: BatchID) = {
    settings.dataDir + "yogurtview_0_" + DataFileDateFormat.format(YogurtsPerManufacturerJob.batcher.earliestTimeOf(batch.next).toDate)
  }
  

  def parseYogurt(bytes: Array[Byte]): Yogurt = {
    parseYogurt(new String(bytes))
  }
    
  def parseYogurt(s: String): Yogurt = {
    read[Yogurt](s)
  }
  
  def serializeYogurt(yogurt: Yogurt): String = {
    val writer = new StringWriter()
    write(yogurt, writer)
    writer.toString()
  }
  
  
  
}