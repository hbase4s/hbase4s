package hbase4s.filter

import hbase4s.filter._

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
    parseOrFailOnErr("column_name == date") shouldBe Qualifier(Eq, "date")
    parseOrFailOnErr("column_value == some") shouldBe Value(Eq, "some")
  }

  "Static and string based filters" should "produce the same output" in {
    val filter1 = keys & (c("family", "column") !== "SomeValue")
    val filter2 = parseOrFailOnErr("(key AND (family:column != SomeValue))")
    filter1 shouldBe filter2

    keys shouldBe "key".f
    firstKeys shouldBe "first_key".f
    rowPrefix is "row_id_1" shouldBe "row_prefix == row_id_1".f
    columnPrefix is "col_name_a" shouldBe "column_prefix == col_name_a".f
    columnPrefix in("col_a", "col_b") shouldBe "column_prefix == (col_a, col_b)".f
    columnLimit === 10 shouldBe "column_limit == 10".f
    pageLimit === 10 shouldBe "page_count == 10".f
    stop on "row_id_181" shouldBe "stop_row == row_id_181".f
    columnName is "col_name_a" shouldBe "column_name == col_name_a".f
    columnValue is "some_value_b" shouldBe "column_value == some_value_b".f
    c("event", "name") === "Henry VIII" shouldBe "event:name == \"Henry VIII\"".f
    keys & pageLimit === 2 shouldBe "key AND (page_count == 2)".f
    (rowPrefix is "r_a") | (columnPrefix is "c_b") shouldBe "(row_prefix == r_a) OR (column_prefix == c_b)".f
  }
}
