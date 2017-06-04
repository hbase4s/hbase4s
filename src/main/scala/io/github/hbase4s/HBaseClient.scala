package io.github.hbase4s

import io.github.hbase4s.config.HBaseConfig
import io.github.hbase4s.filter.{FilterParser, FilterTranslator}
import io.github.hbase4s.utils.HBaseImplicitUtils._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.{Cell, CellUtil, TableName}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
class HBaseConnection(conf: HBaseConfig) {
  val conn: Connection = ConnectionFactory.createConnection(conf.configuration)
}

class HBaseClient(connection: HBaseConnection, tableName: String) {

  protected val logger: Logger = LoggerFactory.getLogger(getClass)

  protected val table: Table = connection.conn.getTable(TableName.valueOf(tableName))

  def put[K: TypeTag, T <: AnyRef with Product](key: K, cc: T): Unit = putFields(RecordFactory.build(key, cc))

  def putAll[K: TypeTag, T <: AnyRef with Product](keyValueData: Map[K, T]): Unit =
    putBatch(keyValueData.map { case (key, cc) =>
      val record = RecordFactory.build(key, cc)
      record.key -> record.values
    })

  def putFields(r: Record): Unit = putFields(r.key, r.values)

  def putFields(key: Array[Byte], values: List[Field[Array[Byte]]]): Unit = putBatch(Map(key -> values))

  def putBatch(keyValueData: Map[Array[Byte], List[Field[Array[Byte]]]]): Unit = {
    val puts = keyValueData.map { case (key, fields) =>
      val p = new Put(key)
      fields.foreach(f => p.addColumn(f.family, f.name, f.value))
      p
    }.toList.asJava
    table.put(puts)
  }

  /**
    * Retrieve all rows from table
    *
    * @tparam K type of rows keys
    * @return map of rows [row key, list of fields]
    */
  def scanAll[K: TypeTag]: Map[K, List[Field[Array[Byte]]]] = {
    val scanner = table.getScanner(new Scan())
    val res = transformResults[K](scanner)
    scanner.close()
    res
  }

  @deprecated("unreliable api")
  def scanAllAsStr: Map[String, List[Field[String]]] = {
    scanAll[String].map { case (k, v) =>
      k -> v.map { f =>
        f.copy(value = asString(f.value))
      }
    }
  }

  def scan[K: TypeTag](filter: String): ResultTraversable[K] = scan[K](FilterParser.parse(filter))

  def scan[K: TypeTag](f: filter.Expr): ResultTraversable[K] = {
    val sc = FilterTranslator.scanFromExpr(f)
    logger.debug(s"Searching with scan-filter $sc")
    scan[K](sc)
  }

  def scan[K: TypeTag](s: Scan): ResultTraversable[K] = {
    val scan = table.getScanner(s)
    val res = new ResultTraversable[K](transformResults(scan))
    scan.close()
    res
  }

  def get[K: TypeTag](r: K): Option[WrappedResult[K]] = get(List(r)).headOption

  def get[K: TypeTag](r: List[K]): ResultTraversable[K] = new ResultTraversable[K](
    table.get(r.map(req => new Get(anyToBytes(req))).asJava).flatMap(r => transformResult[K](r)).map { case (k, value) =>
      k -> value
    }.toMap
  )

  def delete[K: TypeTag](r: K): Unit = table.delete(new Delete(anyToBytes(r)))

  def delete[K: TypeTag](r: List[K]): Unit = table.delete(r.map(req => new Delete(anyToBytes(r))).asJava)

  private[this] def transformResults[K: TypeTag](scan: ResultScanner) = scan.asScala.flatMap { x =>
    transformResult[K](x)
  }.toMap[K, List[Field[Array[Byte]]]]

  private[this] def transformResult[K: TypeTag](res: Result) = {
    if (res.isEmpty) None
    else Some(res.getRow.as[K] -> res.listCells().asScala.map(cellToField).toList)
  }

  private[this] def cellToField(cell: Cell) = Field(CellUtil.cloneFamily(cell), CellUtil.cloneQualifier(cell), CellUtil.cloneValue(cell))

}

case class Field[T](family: String, name: String, value: T)


