/**
 * Base class for all unit tests
 */
package org.yaourtcorp.summingbird.hybrid.example

import org.scalatest._
import org.scalatest.mock.MockitoSugar

/**
 * @author jaguiar
 *
 */
abstract class AbstractUnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors with MockitoSugar {

}