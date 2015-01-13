package org.yaourtcorp.summingbird.hybrid.example
package yogurt

import YogurtsPerManufacturerJob.{batcher}
//import YogurtStormRunner.store
//import ScaldingRunner.servingStore

object YogurtLookupClient extends AbstractLookupClient[Int, Long] {

  override val onlineStore = YogurtStormRunner.store
  override val offlineStore = YogurtScaldingRunner.servingStore

}