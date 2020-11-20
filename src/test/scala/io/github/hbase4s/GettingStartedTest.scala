package io.github.hbase4s

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import io.github.hbase4s.config.HBaseExternalConfig
import io.github.hbase4s.utils.HBaseTesting
import org.scalatest.{FlatSpec, Matchers}
import io.github.hbase4s.utils.HBaseImplicitUtils._
import io.github.hbase4s.filter._

import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{TableName}

/**
  * Created by Volodymyr.Glushak on 21/05/2017.
  */
class GettingStartedTest extends FlatSpec with Matchers {

  // set of helpers for test
  private[this] val utility = HBaseTesting.hBaseServer
  var tableName = "transactions"
  private[this] val Table = TableName.valueOf(Bytes.toBytes("transactions"))
  private[this] val Family = Bytes.toBytes("event")

  // family column name should have the same name as case class (lowercase) we want to store in this table.
  utility.createTable(Table, Family)

  case class Event(index: Int, id: Long, enabled: Boolean, description: String)

  // establish connection to HBase server, point HBaseClient to work with "transactions" table
  val client: HBaseClient = hBaseClient(new HBaseExternalConfig(utility.getConfiguration), tableName)

  "Sample 1" should "show how to work with library using case classes" in {
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
    val e2 = client.scan[String](
      c("event", "description") === "oh-oh" & c("event", "index") > 18
    ).map(_.typed[Event].asClass)

    e2 shouldBe List(e)
    e2 shouldBe e1

    // remove by key
    client.delete(rowId)
  }

  "Sample 2" should "show how to work with custom data types" in {

    val id = "row_id_1"
    // records with just one field - date
    // create list of columns (fields) manually, providing family and name
    // value has to be array of bytes or one of supported type
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
    sdf.setTimeZone(TimeZone.getDefault)
    val date = sdf.format(new Date)
    val dateField = Field("event", "create_date", asBytes(date))

    client.putFields(id, List(dateField))

    val outDate = client.scan[String](c("event", "create_date") === date).map { wr =>
      wr.asString("event:create_date")
    }.head

    require(date == outDate)
    println(sdf.parse(outDate))
  }
}
