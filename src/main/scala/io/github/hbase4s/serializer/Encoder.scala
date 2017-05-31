package io.github.hbase4s.serializer

/**
  * Interface that describes rules for transformation to/from byte array
  * Created by Volodymyr.Glushak on 31/05/2017.
  */
trait Encoder[T] {

  def fromBytes(a: Array[Byte]): T

  // TODO: fix this Any???
  def toBytes(b: Any): Array[Byte]
}

/**
  * If encoder is expected to be supported by query DSL, it should implement this interface
  */
trait QueryEncoder[T] extends Encoder[T] {

  /**
    * name of type that will be used by string query DSL to parse value properly
    * @return name
    */
  def name: String

  def fromString(s: String): T

}