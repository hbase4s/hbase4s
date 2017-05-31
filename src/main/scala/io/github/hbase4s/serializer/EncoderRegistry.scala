package io.github.hbase4s.serializer

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe._

/**
  * Encoders (serializers) registry that allow implicitly transform different types
  * to and from byte array (that is necessary by HBase).
  *
  * Created by Volodymyr.Glushak on 31/05/2017.
  */
object EncoderRegistry {

  private val cache = new TrieMap[Type, Encoder[_]]()
  private val queryCache = new TrieMap[String, Encoder[_]]()

  def add(t: Type, enc: Encoder[_]): Option[Encoder[_]] = {
    cache.put(t, enc)
    enc match {
      case qenc: QueryEncoder[_] => queryCache.put(qenc.name, qenc)
    }
  }

  // there are two ways to encode - by type or by value (extracting type from it
  def encodeByType(tt: Type): Encoder[_] = {
    cache.getOrElse(tt,
      cache.find(t => tt =:= t._1).getOrElse(
        cache.find(t => tt <:< t._1).getOrElse(// try to use encoder for super type. is that correct? // do not implement global encoders...
          sys.error(s"Can't find encoder for type $tt.")
        )
      )._2
    )
  }

  // this method was introduced to handle cases when information about type lost in runtime
  // (for ex.: value of some specific type defined as Any)
  def encodeByValue[T: TypeTag](value: T): Encoder[_] = {
    val tValue = typeOf[T]
    cache.find(t => tValue =:= t._1 || tValue <:< t._1).getOrElse(
      cache.find(t => {
        t._1.typeSymbol.fullName == value.getClass.getTypeName
      }).getOrElse(
        sys.error(s"Can't find encoder for value with unexpected type $value.")
      )
    )._2
  }

  // some option encoders below

  add(typeOf[scala.Some[_]], new OptionEncoder[String]("option") {
    override def fromString(s: String): Option[String] = Option(s)
  })
  add(typeOf[Option[_]], new OptionEncoder[String]("option") {
    override def fromString(s: String): Option[String] = Option(s)
  })

  add(typeOf[Option[String]], new OptionEncoder[String]("option_str") {
    override def fromString(s: String): Option[String] = Option(s)
  })
  add(typeOf[Option[Int]], new OptionEncoder[Int]("option_int") {
    override def fromString(s: String): Option[Int] = Option(s.toInt)
  })
  add(typeOf[Option[Long]], new OptionEncoder[Long]("option_long") {
    override def fromString(s: String): Option[Long] = Option(s.toLong)
  })
  add(typeOf[Option[Short]], new OptionEncoder[Short]("option_short") {
    override def fromString(s: String): Option[Short] = Option(s.toShort)
  })
  add(typeOf[Option[Float]], new OptionEncoder[Float]("option_float") {
    override def fromString(s: String): Option[Float] = Option(s.toFloat)
  })
  add(typeOf[Option[Double]], new OptionEncoder[Double]("option_double") {
    override def fromString(s: String): Option[Double] = Option(s.toDouble)
  })
  add(typeOf[Option[Boolean]], new OptionEncoder[Boolean]("option_bool") {
    override def fromString(s: String): Option[Boolean] = Option(s.toBoolean)
  })
  add(typeOf[Option[BigDecimal]], new OptionEncoder[BigDecimal]("option_bigdecimal") {
    override def fromString(s: String): Option[BigDecimal] = Option(BigDecimal(s))
  })
  add(typeOf[None.type], new OptionEncoder[String]("none") {
    override def fromString(s: String): Option[String] = None
  })
}