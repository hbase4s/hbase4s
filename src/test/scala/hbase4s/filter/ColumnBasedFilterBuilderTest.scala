package hbase4s.filter

import hbase4s.filter.FilterDsl._

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class ColumnBasedFilterBuilderTest extends AbstractFilterTest {

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
      parseOrFailOnErr(scf) match {
        case SingleColVal(col, op, v, _) =>
          op shouldBe Eq
          col shouldBe Column("family", "column")
          v should be("12345")
        case x => fail(s"Incorrectly parsed expression $scf -> $x.")
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
      "column_aaa == 18".f
    }
  }

  "Int value type" should "be treated as non-string" in {
    "family:column == int(18)".f shouldBe (c("family", "column") === 18)
  }

}
