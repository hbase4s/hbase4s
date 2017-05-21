package hbase4s

import hbase4s.config.HBaseExternalConfig
import hbase4s.utils.HBaseTesting
import org.scalatest.{FlatSpec, Matchers}
import hbase4s.utils.HBaseImplicitUtils._

/**
  * Created by Volodymyr.Glushak on 21/05/2017.
  */
class GettingStartedTest extends FlatSpec with Matchers {

  // set of helpers for test
  private[this] val utility = HBaseTesting.hBaseServer
  private[this] val Table = "transactions"
  private[this] val Family = "event"

  // family column name should have the same name as case class (lowercase) we want to store in this table.
  utility.createTable(Table, Family, 1)

  case class Event(index: Int, id: Long, enabled: Boolean, description: String)

  // establish connection to HBase server, point HBaseClient to work with transactions table
  val client = new HBaseClient(new HBaseConnection(new HBaseExternalConfig(utility.getConfiguration)), Table)

  "Sample" should "show how to work with HBase" in {
    val e = Event(546, 10L, enabled = true, "oh-oh")
    val rowId = "oh-event-id-string"

    // store case class in hbase table, under provided columns family
    client.put(rowId, e)

    // retrieve data from HBase by key and transform it to instance of event class
    val eventInDb = client.get(rowId).map(_.typed[Event].asClass)

    eventInDb shouldBe Some(e)

    // two version of querying data from HBase table
    // results are represented as List of Event class
    // string-based DSL
    val e1 = client.scan[String](
      "(event:description = \"oh-oh\") AND (event:index > int(18))"
    ).map(_.typed[Event].asClass)

    // scala static type DSL
    import hbase4s.filter.FilterDsl._
    val e2 = client.scan[String](
      c("event", "description") === "oh-oh" & c("event", "index") > 18
    ).map(_.typed[Event].asClass)

    e2 shouldBe List(e)
    e2 shouldBe e1

    // remove by key
    client.delete(rowId)
  }
}
