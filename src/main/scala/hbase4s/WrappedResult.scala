package hbase4s

import hbase4s.RecordFactory.FromListToCC
import hbase4s.utils.{HBaseImplicitUtils => hbf}

import scala.reflect.runtime.universe._

/**
  * Created by Volodymyr.Glushak on 18/05/2017.
  */
case class WrappedResult[K](key: K, data: List[Field[Array[Byte]]]) {

  private[this] val cache = data.map(f => s"${f.family}:${f.name}" -> f).toMap

  def allColumnNames: List[String] = cache.keys.toList

  private[this] def get(name: String) = cache.getOrElse(name, sys.error(s"Column $name not found. Available columns $allColumnNames")).value

  def asString(name: String): String = hbf.asString(get(name))

  def asInt(name: String): Int = hbf.asInt(get(name))

  def asLong(name: String) = hbf.asLong(get(name))

  def asFloat(name: String) = hbf.asFloat(get(name))

  def asDouble(name: String) = hbf.asDouble(get(name))

  def asShort(name: String) = hbf.asShort(get(name))

  def asBoolean(name: String) = hbf.asBoolean(get(name))

  def asBigDecimal(name: String) = hbf.asBigDecimal(get(name))

  @deprecated // TODO: should it be part of API? No - non String fields will contain invalid values.
  def allAsString: Map[String, Field[String]] = cache.map {
    case (id, fields) => id -> fields.copy(value = hbf.asString(fields.value))
  }

  def typed[T: TypeTag]: FromListToCC[T] = RecordFactory.typed[T](data)

}
