package io.github.hbase4s.serializer

import io.github.hbase4s.utils.HBaseImplicitUtils._
import io.github.hbase4s._
import scala.reflect.runtime.universe._

/**
  * Created by Volodymyr.Glushak on 31/05/2017.
  */
class OptionEncoder[V: TypeTag] extends Encoder[Option[V]] {


  override def fromBytes(a: Array[Byte]): Option[V] = {
    Option(a.from(typeOf[V])).asInstanceOf[Option[V]]
  }

  override def toBytes(b: Any): Array[Byte] = b match {
    case None => null
    case Some(x) => anyToBytes(x)
  }
}
