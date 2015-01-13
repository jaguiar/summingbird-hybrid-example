package org.yaourtcorp.summingbird.hybrid.example.utils

import org.json4s.DateFormat
import java.text.ParseException
import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import java.util.Date

/**
 * Wrap ISO8601DateFormat to avoid using java.text.SimpleDateFormat (performances) and since Jackson JodaModule does not work with Json4s
 *
 */
class ISO8601DateFormatter extends DateFormat {

  def parse(s: String) = try {
    Some(formatter.parse(s))
  } catch {
    case e: ParseException => None
  }

  def format(d: Date) = formatter.format(d)

  private[this] def formatter = new ISO8601DateFormat()

}