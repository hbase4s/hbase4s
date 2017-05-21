package hbase4s.filter

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class ColumnBasedFilterBuilderTest extends FlatSpec with Matchers {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  private[this] val sName = "family:column"
  private[this] val sVal = "12345"
  private[this] val singleColumnFilter = s"( $sName = $sVal)"
  private[this] val columnOrFilter = "( family:column = 12345 ) OR ( family:column = 12346 ) "
  private[this] val columnOrAndFilter = " ( ( family:column = 12345 ) OR ( family:column = 12346 ) ) AND ( family:col4 = 6 )"

  private[this] val otherVal = "value.(22) and value:'55'"
  private[this] val otherFilter = s"""( ( family:field1 = value_20) OR ( family:field2 = "$otherVal" ) )"""

  val singleColVariance = List(singleColumnFilter, s"$sName=$sVal", s" ( $sName == $sVal ) ")
  val multiColVariance = List(columnOrFilter, columnOrAndFilter, otherFilter)
  val singleColFails = List(s"($sName == $sVal")

  "All variants of single expression" should "be transformed" in {
    singleColVariance.foreach { scf =>
      val value = parseOrFailOnErr(scf) match {
        case SingleColVal(col, op, v, _) =>
          op shouldBe Eq
          col shouldBe Column("family", "column")
          v should be("12345")
      }
    }
  }

  "All variants of multi expression" should "be parsed" in {
    multiColVariance.foreach { scf =>
      parseOrFailOnErr(scf)
    }

    parseOrFailOnErr(otherFilter) shouldBe Or(
      SingleColVal(Column("family", "field1"), Eq, "value_20"),
      SingleColVal(Column("family", "field2"), Eq, otherVal))
  }

  "Invalid filters" should "be treated user-friendly" in {
    intercept[RuntimeException] {
      FilterParser.parse("column_aaa == 18")
    }
  }

  "Int value type" should "be treated as non-string" in {
    import FilterDsl._
    FilterParser.parse("family:column == int(18)") shouldBe (c("family", "column") === 18)
  }

  private[this] def parseOrFailOnErr[T](scf: String) = {
    try {
      FilterParser.parse(scf)
    } catch {
      case ex: Throwable =>
        fail(s"Failed to process $scf", ex)
    }
  }

}
