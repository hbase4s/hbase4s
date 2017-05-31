package io.github.hbase4s.serializer

/**
  * Created by Volodymyr.Glushak on 31/05/2017.
  */
trait Encoder[T] {
  def fromBytes(a: Array[Byte]): T

  // TODO: fix this Any???
  def toBytes(b: Any): Array[Byte]
}