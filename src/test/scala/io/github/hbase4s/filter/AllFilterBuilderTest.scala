package io.github.hbase4s.filter

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class AllFilterBuilderTest extends AbstractFilterTest {

  "Following filters" should "be supported" in {
    // store some data in advanced
    parseOrFailOnErr("keys") shouldBe KeyOnly
    parseOrFailOnErr("first_key") shouldBe FirstKeyOnly

    parseOrFailOnErr("row_prefix == row1") shouldBe RowPrefix("row1")
    parseOrFailOnErr("column_prefix == name2") shouldBe ColumnPrefix("name2")
    parseOrFailOnErr("column_prefix == (name2,name3,name4)") shouldBe MultipleColumnPrefix(List("name2", "name3", "name4"))
    parseOrFailOnErr("column_limit == 5") shouldBe ColumnCountGet(5)
    parseOrFailOnErr("page_count = 2") shouldBe Page(2L)
    parseOrFailOnErr("stop_row = row7") shouldBe InclusiveStop("row7")
    parseOrFailOnErr("column_name == date") shouldBe Qualifier(Eq, "date")
    parseOrFailOnErr("column_value == some") shouldBe Value(Eq, "some")
    parseOrFailOnErr("column_value == some ! start_row == row1, stop_row == row15") shouldBe TopLevelExpr(Value(Eq, "some"), Seq(StartRowId("row1"), StopRowId("row15")))
  }

  "Static and string based filters" should "produce the same output" in {
    val filter1 = keys & (c("family", "column") !== "SomeValue")
    val filter2 = parseOrFailOnErr("(keys AND (family:column != SomeValue))")
    filter1 shouldBe filter2

    keys shouldBe "keys".f
    firstKey shouldBe "first_key".f
    rowPrefix is "row_id_1" shouldBe "row_prefix == row_id_1".f
    columnPrefix is "col_name_a" shouldBe "column_prefix == col_name_a".f
    columnPrefix in("col_a", "col_b") shouldBe "column_prefix == (col_a, col_b)".f
    columnLimit === 10 shouldBe "column_limit == 10".f
    pageLimit === 10 shouldBe "page_count == 10".f
    stop on "row_id_181" shouldBe "stop_row == row_id_181".f
    columnName is "col_name_a" shouldBe "column_name == col_name_a".f
    columnValue is "some_value_b" shouldBe "column_value == some_value_b".f
    c("event", "name") === "Henry VIII" shouldBe "event:name == \"Henry VIII\"".f
    keys & pageLimit === 2 shouldBe "keys AND (page_count == 2)".f
    (rowPrefix is "r_a") | (columnPrefix is "c_b") shouldBe "(row_prefix == r_a) OR (column_prefix == c_b)".f


    (keys | firstKey) ! (startRow is "row1", stopRow is "row18") shouldBe "(keys OR (first_key)) ! start_row == row1, stop_row == row18".f

    // tests for custom types
    c("event", "age") === 15 shouldBe "event:age == int(15)".f
    c("event", "description") === Option("some text") shouldBe "event:description == option_str(\"some text\")".f
    c("e", "t") === Some(true) & c("e", "d") === Some(15.01) shouldBe "e:t == option_bool(true) AND (e:d == option_double(\"15.01\"))".f
    c("e", "t") === None & c("e", "d") === Some(15.01f) shouldBe "e:t == none(null) AND (e:d == option_float(\"15.01\"))".f

  }
}
