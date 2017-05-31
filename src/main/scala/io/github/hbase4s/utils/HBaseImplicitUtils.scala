package io.github.hbase4s.utils

import org.apache.hadoop.hbase.util.Bytes

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe._

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
object HBaseImplicitUtils {
  // All HBase API rely on this functions: it expect byte[] everywhere, instead of primitive types.
  implicit def asBytes(s: String): Array[Byte] = Bytes.toBytes(s)

  implicit def asBytes(s: Int): Array[Byte] = Bytes.toBytes(s)

  implicit def asBytes(s: Long): Array[Byte] = Bytes.toBytes(s)

  implicit def asBytes(s: Boolean): Array[Byte] = Bytes.toBytes(s)

  implicit def asBytes(s: Double): Array[Byte] = Bytes.toBytes(s)

  implicit def asBytes(s: BigDecimal): Array[Byte] = Bytes.toBytes(s.bigDecimal)

  implicit def asBytes(s: Float): Array[Byte] = Bytes.toBytes(s)

  implicit def asBytes(s: Short): Array[Byte] = Bytes.toBytes(s)

  implicit def asString(b: Array[Byte]): String = Bytes.toString(b)

  implicit def asInt(b: Array[Byte]): Int = Bytes.toInt(b)

  implicit def asLong(b: Array[Byte]): Long = Bytes.toLong(b)

  implicit def asBoolean(b: Array[Byte]): Boolean = Bytes.toBoolean(b)

  implicit def asDouble(b: Array[Byte]): Double = Bytes.toDouble(b)

  implicit def asFloat(b: Array[Byte]): Float = Bytes.toFloat(b)

  implicit def asBigDecimal(b: Array[Byte]): BigDecimal = Bytes.toBigDecimal(b)

  implicit def asShort(b: Array[Byte]): Short = Bytes.toShort(b)

  implicit class RichArrayBytes(b: Array[Byte]) {

    def as[K: TypeTag]: K = from(typeOf[K]).asInstanceOf[K]

    def from(x: Type): Any = {
      x match {
        case t if t =:= typeOf[String] => asString(b)
        case t if t =:= typeOf[Int] => asInt(b)
        case t if t =:= typeOf[Long] => asLong(b)
        case t if t =:= typeOf[Boolean] => asBoolean(b)
        case t if t =:= typeOf[Double] => asDouble(b)
        case t if t =:= typeOf[Float] => asFloat(b)
        case t if t =:= typeOf[Short] => asShort(b)
        case t if t =:= typeOf[BigDecimal] => asBigDecimal(b)
        case t if t =:= typeOf[Array[Byte]] => b
        // add support of other types via Encoder classes
        case t => EncoderRegistry.encoder(t).fromBytes(b)
      }
    }
  }

}

trait Encoder[T] {
  def fromBytes(a: Array[Byte]): T

  // TODO: fix this Any???
  def toBytes(b: Any): Array[Byte]
}

class OptionEncoder[V: TypeTag] extends Encoder[Option[V]] {

  import HBaseImplicitUtils._
  import io.github.hbase4s._

  override def fromBytes(a: Array[Byte]): Option[V] = {
    Option(a.from(typeOf[V])).asInstanceOf[Option[V]]
  }

  override def toBytes(b: Any): Array[Byte] = b match {
    case None => null
    case Some(x) => anyToBytes(x)
  }
}

object EncoderRegistry {

  val cache = new TrieMap[Type, Encoder[_]]()

  cache.put(typeOf[Option[String]], new OptionEncoder[String])
  cache.put(typeOf[Option[Int]], new OptionEncoder[Int])
  cache.put(typeOf[Option[Long]], new OptionEncoder[Long])
  cache.put(typeOf[Option[Short]], new OptionEncoder[Short])
  cache.put(typeOf[Option[Float]], new OptionEncoder[Float])
  cache.put(typeOf[Option[Double]], new OptionEncoder[Double])
  cache.put(typeOf[Option[Boolean]], new OptionEncoder[Boolean])
  cache.put(typeOf[Option[BigDecimal]], new OptionEncoder[BigDecimal])
  cache.put(typeOf[None.type], new OptionEncoder[String])

  def encoder(tt: Type): Encoder[_] = cache.getOrElse(tt,
    cache.find(t => tt =:= t._1).getOrElse(
      sys.error(s"Can't extract value from $tt type. Provide custom extractor.")
    )._2
  )

}