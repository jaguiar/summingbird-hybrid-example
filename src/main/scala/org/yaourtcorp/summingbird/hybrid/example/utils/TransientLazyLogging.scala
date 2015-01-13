package com.typesafe.scalalogging
package slf4j
package example.utils

import org.slf4j.LoggerFactory
/**
 * Sigh...
 * Ok, so the issue here is that [[com.typesafe.scalalogging.LazyLogging]] uses the following code 
 * {{{ getClass.getName}}} 
 * => which may cause a [[java.io.NotSerializableException]] (refer to http://javarevisited.blogspot.fr/2012/12/javaionotserializableexception-orgapache-log4j-logger-error-exception-fix.html for further explanation)
 * Since I like the trait stuff and don't want to use the 
 * {{{val logger = Logger(LoggerFactory.getLogger("name"))}}} form, I need to declare the logger [[@transient]]
 */
trait TransientLazyLogging extends LazyLogging {

  @transient override protected lazy val logger: Logger =
    Logger(LoggerFactory getLogger getClass.getName)
}