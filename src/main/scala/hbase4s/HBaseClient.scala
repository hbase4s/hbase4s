package hbase4s

import hbase4s.config.HBaseConfig
import hbase4s.filter.{FilterParser, FilterTranslator}
import hbase4s.utils.HBaseImplicitUtils._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.{Cell, CellUtil, TableName}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class HBaseConnection(conf: HBaseConfig) {
  val conn: Connection = ConnectionFactory.createConnection(conf.configuration)
}

class HBaseClient(connection: HBaseConnection, tableName: String) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  private[this] val table = connection.conn.getTable(TableName.valueOf(tableName))

  def put[K, T <: AnyRef with Product](key: K, cc: T): Unit = putFields(RecordFactory.build(key, cc))

  def putFields(r: Record): Unit = putFields(r.key, r.values)

  def putFields(key: Array[Byte], values: List[Field[Array[Byte]]]): Unit = {
    val p = new Put(key)
    values.foreach { v =>
      p.addColumn(v.family, v.name, v.value)
    }
    table.put(p)
  }

  /**
    * Retrieve all rows from table
    *
    * @tparam K type of rows keys
    * @return map of rows [row key, list of fields]
    */
  def scanAll[K: TypeTag]: Map[K, List[Field[Array[Byte]]]] = transformResult(table.getScanner(new Scan()))

  def scanAllAsStr: Map[String, List[Field[String]]] = {
    scanAll[String].map { case (k, v) =>
      k -> v.map { f =>
        f.copy(value = asString(f.value))
      }
    }
  }

  def scan[K: TypeTag](filter: String): ResultTraversable[K] = {
    val s = new Scan()
    val fl = FilterTranslator.fromExpr(FilterParser.parse(filter))
    logger.debug(s"Searching with filter $fl")
    s.setFilter(fl)
    val scan = table.getScanner(s)
    new ResultTraversable[K](transformResult(scan))
  }

  private[this] def transformResult[K: TypeTag](scan: ResultScanner) = scan.asScala.map { res =>
    val key = res.getRow
    val map = res.listCells().asScala.map(cellToField).toList
    key.as[K] -> map
  }.toMap

  private[this] def cellToField(cell: Cell) = Field(CellUtil.cloneFamily(cell), CellUtil.cloneQualifier(cell), CellUtil.cloneValue(cell))

}

case class Field[T](family: String, name: String, value: T)


