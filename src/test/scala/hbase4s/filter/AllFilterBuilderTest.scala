package hbase4s.filter

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class AllFilterBuilderTest extends AbstractFilterTest {

  "Following filters" should "be supported" in {
    // store some data in advanced
    parseOrFailOnErr("key") shouldBe KeyOnly
    parseOrFailOnErr("first_key") shouldBe FirstKeyOnly

    parseOrFailOnErr("row_prefix == row1") shouldBe RowPrefix("row1")
    parseOrFailOnErr("column_prefix == name2") shouldBe ColumnPrefix("name2")
    parseOrFailOnErr("column_prefix == (name2,name3,name4)") shouldBe MultipleColumnPrefix(List("name2", "name3", "name4"))
    parseOrFailOnErr("column_limit == 5") shouldBe ColumnCountGet(5)
    parseOrFailOnErr("page_count = 2") shouldBe Page(2)
    parseOrFailOnErr("stop_row = row7") shouldBe InclusiveStop("row7")
    parseOrFailOnErr("column_name == event:date") shouldBe Qualifier(Eq, Column("event", "date"))
    parseOrFailOnErr("column_value == some") shouldBe Value(Eq, "some")
  }
}
