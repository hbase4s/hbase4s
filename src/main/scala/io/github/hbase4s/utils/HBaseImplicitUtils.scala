package io.github.hbase4s.utils

import io.github.hbase4s.serializer.EncoderRegistry
import org.apache.hadoop.hbase.util.Bytes

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
        case t => EncoderRegistry.encodeByType(t).fromBytes(b)
      }
    }
  }

}




