package io.github.hbase4s

import io.github.hbase4s.config.HBaseExternalConfig
import io.github.hbase4s.utils.HBaseImplicitUtils._
import io.github.hbase4s.utils.HBaseTesting
import org.scalatest.{FlatSpec, Matchers}
import io.github.hbase4s.filter._
/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class HBaseClientTest extends FlatSpec with Matchers {

  private[this] val utility = HBaseTesting.hBaseServer
  private[this] val TestTable = "test_table"
  private[this] val Fam1 = "fox_family"
  private[this] val Fam2 = "dog_family"
  private[this] val Fam3 = "event"
  private[this] val Fam4 = "withopt"
  private[this] val F1 = "field1"
  private[this] val F2 = "field2"
  private[this] val Families: Array[Array[Byte]] = Array(Fam1, Fam2, Fam3, Fam4)
  utility.createTable(TestTable, Families, 1)

  case class Test2Field[T](key: T, field1: String, field2: String)

  val Max = 1000
  val dsl = new HBaseClient(new HBaseConnection(new HBaseExternalConfig(utility.getConfiguration)), TestTable)

  "It" should "perform put and scan with string key" in {

    (1 to Max).foreach { i =>
      dsl.putFields(s"key_$i", List(Field(Fam1, F1, s"value_$i"), Field(Fam2, F2, "value_2")))
    }
    val res = dsl.scanAllAsStr
    res.size shouldBe Max

    val res2 = dsl.scan[String](s"($Fam1:$F1=value_20 )")
      .map(wr => Test2Field(wr.key, wr.asString(s"$Fam1:$F1"), wr.asString(s"$Fam2:$F2")))
    res2.size shouldBe 1
    res2.headOption.map(_.field1 shouldBe "value_20")
  }

  "It" should "perform put and scan with int key" in {
    val someField = "field_with_int"
    (1 to Max).foreach { i =>
      dsl.putFields(-i, List(Field(Fam1, someField, s"value_$i"), Field(Fam2, F2, "value_2")))
    }

    val res2 = dsl.scan[Int](s"($Fam1:$someField=value_20)")
      .map(wr => {
        // println(wr.allAsString)
        Test2Field(wr.key, wr.asString(s"$Fam1:$someField"), wr.asString(s"$Fam2:$F2"))
      })
    res2.size shouldBe 1
    res2.headOption.map(_.key shouldBe -20)
    res2.headOption.map(_.field1 shouldBe "value_20")
    res2.headOption.map(_.field2 shouldBe "value_2")
  }

  "It" should "know how to store case class" in {
    val testData = Event(1, 10 ^ 10, "oh_yes", enabled = true)
    (1 to 1000).foreach { x =>
      dsl.put(x, testData.copy(index = x))
    }

    val events = dsl.scan[Int]("event:description == oh_yes").map { wr =>
      wr.typed[Event].asClass
    }.toList

    events.foreach { e =>
      e.enabled shouldBe true
    }
  }

  "It" should "allow to put, get, filter, delete" in {
    val e = Event(1, 10L, "oh-oh", enabled = true)
    val rowId = "oh-oh-event"
    dsl.put(rowId, e)
    dsl.get(rowId).map(_.typed[Event].asClass) shouldBe Some(e)
    dsl.scan[String]("event:description = \"oh-oh\"").map(_.typed[Event].asClass) shouldBe List(e)
    dsl.delete(rowId)
    dsl.get(rowId).map(_.typed[Event].asClass) shouldBe None
    dsl.scan[String]("event:description = \"oh-oh\"").map(_.typed[Event].asClass) shouldBe List()
  }

  "Custom filters" should "return correct results" in {

    val testData = Event(1, 1L, "default", 1.0f, 1.15, 15, enabled = true)
    (1 to 1000).foreach { x =>
      dsl.put(x, testData.copy(index = x, id = 10L * x, description = s"Some Text #$x"))
    }

    def search(q: String) = dsl.scan[Int](q).map(_.typed[Event].asClass).toList

    search("(event:id < long(100))").size shouldBe 9

    val res2 = search(
      "(event:id > long(120)) AND (page_count == 5) AND" +
        " (event:amount <= float(1.1)) AND (event:sum > double(1.10)) AND (event:rate >= short(15))"
    )
    res2.size shouldBe 5
    res2.headOption.foreach {
      _.id shouldBe 140L
    }

    dsl.scan[Int](
      "(column_prefix == i) AND (page_count == 1)"
    ).map(_.allColumnNames).toList shouldBe List(List("event:id", "event:index"))

    dsl.scan[Int]("(column_name == enabled) AND (page_count ==1)").map(_.allColumnNames).toList shouldBe List(List("event:enabled"))

    dsl.scan[Int]("column_value == long(240)").map(_.allColumnNames).toList shouldBe List(List("event:id"))

    val r1 = dsl.scan[Int]("keys AND (row_prefix == int(200))")
    r1.map(_.key).toList shouldBe List(200) // just one value based on row prefix
    r1.map(_.asString("event:description")).toList shouldBe List("") // field values should be empty

    val r2 = dsl.scan[Int]("first_key AND (row_prefix == int(200)) | start_row == int(199), stop_row == int(201)")
    r2.map(_.key).toList shouldBe List(200)
    r2.map(_.allColumnNames).toList shouldBe List(List("event:amount")) // first field alphabetical order
  }


  "Option type" should "be handled" in {

    // test put
    dsl.put(1, WithOpt(1, Option("knight"), Some(true)))

    // test get
    val res = dsl.get(1).map(_.typed[WithOpt].asClass).getOrElse(sys.error("Can't find WithOpt record in HBase"))
    res.i shouldBe 1
    res.name shouldBe Some("knight")
    res.exists shouldBe Some(true)

    // test query within different DSLs
    val res2 = dsl.scan[Int]("withopt:name = option_str(knight) AND (withopt:exists = option_bool(true))")
      .map(_.typed[WithOpt].asClass).headOption.getOrElse(sys.error("Can't find WithOpt record in HBase"))
    res2.i shouldBe 1


    val res3 = dsl.scan[Int](c("withopt", "name") === Option("knight") & c("withopt", "exists") === Option(true))
      .map(_.typed[WithOpt].asClass).headOption.getOrElse(sys.error("Can't find WithOpt record in HBase"))
    res3.i shouldBe 1
  }
}

case class WithOpt(i: Int, name: Option[String], exists: Option[Boolean])

case class Event(index: Int, id: Long, description: String, amount: Float = 0f, sum: Double = 0.0d, rate: Short = 1, enabled: Boolean)

