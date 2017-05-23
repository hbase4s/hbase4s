package io.github.hbase4s

import io.github.hbase4s.config.HBaseExternalConfig
import io.github.hbase4s.utils.HBaseImplicitUtils._
import io.github.hbase4s.utils.HBaseTesting
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class HBaseClientTest extends FlatSpec with Matchers {

  private[this] val utility = HBaseTesting.hBaseServer
  private[this] val TestTable = "test_table"
  private[this] val Fam1 = "fox_family"
  private[this] val Fam2 = "dog_family"
  private[this] val Fam3 = "event"
  private[this] val F1 = "field1"
  private[this] val F2 = "field2"
  private[this] val Families: Array[Array[Byte]] = Array(Fam1, Fam2, Fam3)
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
      dsl.putFields(i, List(Field(Fam1, someField, s"value_$i"), Field(Fam2, F2, "value_2")))
    }

    val res2 = dsl.scan[Int](s"($Fam1:$someField=value_20)")
      .map(wr => {
        // println(wr.allAsString)
        Test2Field(wr.key, wr.asString(s"$Fam1:$someField"), wr.asString(s"$Fam2:$F2"))
      })
    res2.size shouldBe 1
    res2.headOption.map(_.key shouldBe 20)
    res2.headOption.map(_.field1 shouldBe "value_20")
    res2.headOption.map(_.field2 shouldBe "value_2")
  }

  "It" should "know how to store case class" in {
    val testData = Event(1, 10 ^ 10, enabled = true, "oh_yes")
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
    val e = Event(1, 10L, enabled = true, "oh-oh")
    val rowId = "oh-oh-event"
    dsl.put(rowId, e)
    dsl.get(rowId).map(_.typed[Event].asClass) shouldBe Some(e)
    dsl.scan[String]("event:description = \"oh-oh\"").map(x => x.typed[Event].asClass) shouldBe List(e)
    dsl.delete(rowId)
    dsl.get(rowId).map(_.typed[Event].asClass) shouldBe None
    dsl.scan[String]("event:description = \"oh-oh\"").map(x => x.typed[Event].asClass) shouldBe List()
  }

  "Custom filters" should "return correct results" in {

    val testData = Event(1, 1L, enabled = true, "default")
    (1 to 1000).foreach { x =>
      dsl.put(x, testData.copy(index = x, id = 10L * x, description = s"Some Text #$x"))
    }

    def search(q: String) = dsl.scan[Int](q).map(wr => wr.typed[Event].asClass).toList

    def searchStr(q: String) =

      search("(event:id < long(100))").size shouldBe 9

    val res2 = search("(event:id > long(120)) AND (page_count == 5)")
    res2.size shouldBe 5
    res2.headOption.map { ev => ev.id shouldBe 140L }

    dsl.scan[Int](
      "(column_prefix == i) AND (page_count == 1)"
    ).map(wr => wr.allColumnNames).toList shouldBe List(List("event:id", "event:index"))

    dsl.scan[Int]("(column_name == enabled) AND (page_count ==1)").map(
      wr => wr.allColumnNames
    ).toList shouldBe List(List("event:enabled"))

    dsl.scan[Int]("column_value == long(240)").map(
      wr => wr.allColumnNames
    ).toList shouldBe List(List("event:id"))


  }
}

case class Event(index: Int, id: Long, enabled: Boolean, description: String)

