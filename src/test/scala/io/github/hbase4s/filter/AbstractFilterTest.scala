package io.github.hbase4s.filter

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by Volodymyr.Glushak on 21/05/2017.
  */
trait AbstractFilterTest extends FlatSpec with Matchers {

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  protected def parseOrFailOnErr[T](scf: String): Expr = {
    try {
      FilterParser.parse(scf)
    } catch {
      case ex: Throwable =>
        fail(s"Failed to process $scf", ex)
    }
  }

}
