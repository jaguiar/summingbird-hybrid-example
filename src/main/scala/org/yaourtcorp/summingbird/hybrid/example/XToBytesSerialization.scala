
package org.yaourtcorp.summingbird.hybrid.example

import com.twitter.bijection.{ Bufferable, Codec, Injection }
import com.twitter.summingbird.batch.BatchID

/**
 * Serialization is often the most important (and hairy)
 * configuration issue for any system that needs to store its data
 * over the long term. Summingbird controls serialization through the
 * "Injection" interface.
 *
 * By maintaining identical Injections from K and V to Array[Byte],
 * one can guarantee that data written one day will be readable the
 * next. This isn't the case with serialization engines like Kryo,
 * where serialization format depends on unstable parameters, like
 * the serializer registration order for the given Kryo instance.
 */
object XToBytesSerialization {
	/**
	 * Summingbird's implementation of the batch/realtime merge
	 * requires that the Storm-based workflow store (K, BatchID) -> V
	 * pairs, while the Hadoop-based workflow stores K -> (BatchID, V)
	 * pairs.
	 *
	 * The following two injections use Bijection's "Bufferable" object
	 * to generate injections that take (T, BatchID) or (BatchID, T) to
	 * bytes.
	 *
	 * For true production applications, I'd suggest defining a thrift
	 * or protobuf "pair" structure that can safely store these pairs
	 * over the long-term.
	 */
	implicit def kInjection[T: Codec]: Injection[(T, BatchID), Array[Byte]] = {
			implicit val buf =
					Bufferable.viaInjection[(T, BatchID), (Array[Byte], Array[Byte])]
							Bufferable.injectionOf[(T, BatchID)]
	}

	implicit def vInj[V: Codec]: Injection[(BatchID, V), Array[Byte]] =
			Injection.connect[(BatchID, V), (V, BatchID), Array[Byte]]

}
