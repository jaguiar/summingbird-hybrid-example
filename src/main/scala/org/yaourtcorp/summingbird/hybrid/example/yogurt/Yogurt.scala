package org.yaourtcorp.summingbird.hybrid.example.yogurt

import org.joda.time.DateTime


class Yogurt(val uuid: String, val productName: String, val manufacturerId: Int, val creationTime: DateTime) extends Serializable{
	
}