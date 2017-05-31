package io.github.hbase4s.serializer

import scala.collection.concurrent.TrieMap
import scala.reflect.runtime.universe._

/**
  * Encoders (serializers) registry that allow implicitly transform different types
  * to and from byte array (that is necessary by HBase).
  * Created by Volodymyr.Glushak on 31/05/2017.
  */
object EncoderRegistry {

  private val cache = new TrieMap[Type, Encoder[_]]()

  cache.put(typeOf[Option[String]], new OptionEncoder[String])
  cache.put(typeOf[Option[Int]], new OptionEncoder[Int])
  cache.put(typeOf[Option[Long]], new OptionEncoder[Long])
  cache.put(typeOf[Option[Short]], new OptionEncoder[Short])
  cache.put(typeOf[Option[Float]], new OptionEncoder[Float])
  cache.put(typeOf[Option[Double]], new OptionEncoder[Double])
  cache.put(typeOf[Option[Boolean]], new OptionEncoder[Boolean])
  cache.put(typeOf[Option[BigDecimal]], new OptionEncoder[BigDecimal])
  cache.put(typeOf[None.type], new OptionEncoder[String])


  def add(t: Type, enc: Encoder[_]): Option[Encoder[_]] = cache.put(t, enc)

  def encoder(tt: Type): Encoder[_] = cache.getOrElse(tt,
    cache.find(t => tt =:= t._1).getOrElse(
      sys.error(s"Can't extract value from $tt type. Provide custom extractor.")
    )._2
  )

}