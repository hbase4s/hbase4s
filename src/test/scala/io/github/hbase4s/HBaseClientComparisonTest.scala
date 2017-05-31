package io.github.hbase4s

import io.github.hbase4s.config.HBaseExternalConfig
import io.github.hbase4s.utils.HBaseTesting
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put, Table}
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.{FlatSpec, Matchers}

/**
  *
  * This is high level performance test which in future should be rewritten with proper tools
  *
  * Created by Volodymyr.Glushak on 30/05/2017.
  */
class HBaseClientComparisonTest extends FlatSpec with Matchers {

  private[this] val utility = HBaseTesting.hBaseServer
  private val TableJava = "table_java"
  private val TableScala = "table_scala"

  {
    import io.github.hbase4s.utils.HBaseImplicitUtils._
    utility.createTable(TableJava, "f1")
    utility.createTable(TableScala, "f1")
  }

  private def runStoreJava(dsl: Table, count: Int) = {
    val f1 = Bytes.toBytes("f1")
    val tm = Bytes.toBytes("timestamp")
    val nm = Bytes.toBytes("name")
    (0 to count).foreach { id =>
      val millis = System.currentTimeMillis()
      val p = new Put(Bytes.toBytes(millis * id))
      p.addColumn(f1, tm, Bytes.toBytes(millis))
      p.addColumn(f1, nm, Bytes.toBytes(s"some_user_name_or_smth_else$id"))
      dsl.put(p)
    }
  }

  case class F1(timestamp: Long, name: String)

  private def runStoreScala(dsl: HBaseClient, count: Int) = {
    (0 to count).foreach { id =>
      val millis = System.currentTimeMillis()
      dsl.put(millis * id, F1(millis, s"some_user_name_or_smth_else$id"))
    }
  }

  private def withTime[T](f: => T): (Long, T) = {
    val start = System.currentTimeMillis()
    val res = f
    val end = System.currentTimeMillis()
    (end - start, res)
  }

  private val Warmup = 5000
  private val Max = 10000

  "Java/Scala DSL" should "store and query data" in {
    val javaDsl = ConnectionFactory.createConnection(utility.getConfiguration).getTable(TableName.valueOf(TableJava))
    val scalaDsl = new HBaseClient(new HBaseConnection(new HBaseExternalConfig(utility.getConfiguration)), TableScala)

    // warmup phase
    runStoreScala(scalaDsl, Warmup)
    runStoreJava(javaDsl, Warmup)

    val (timeJ, _) = withTime(runStoreJava(javaDsl, Max))
    println(s"Processed $Max with Java API in ${timeJ}ms.")

    val (timeS, _) = withTime(runStoreScala(scalaDsl, Max))
    println(s"Processed $Max with Scala API in ${timeS}ms.")
//    unrelialable - doesn't work as expected on travis - too many external factors
//    // scala API should not be more than 10% slower rather than Java API
//    timeS shouldBe <=((timeJ * 1.1).toLong)
  }

}
