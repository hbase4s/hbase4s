package hbase4s

import hbase4s.config.HBaseExternalConfig
import hbase4s.utils.HBaseImplicitUtils._
import hbase4s.utils.HBaseTesting
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
    res2.head.field1 shouldBe "value_20"
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
    res2.head.key shouldBe 20
    res2.head.field1 shouldBe "value_20"
    res2.head.field2 shouldBe "value_2"
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
    val row_id = "oh-oh-event"
    dsl.put(row_id, e)
    dsl.get(row_id).map(_.typed[Event].asClass) shouldBe Some(e)
    dsl.scan[String]("event:description = \"oh-oh\"").map(x => x.typed[Event].asClass) shouldBe List(e)
    dsl.delete(row_id)
    dsl.get(row_id).map(_.typed[Event].asClass) shouldBe None
    dsl.scan[String]("event:description = \"oh-oh\"").map(x => x.typed[Event].asClass) shouldBe List()
  }
}

case class Event(index: Int, id: Long, enabled: Boolean, description: String)

