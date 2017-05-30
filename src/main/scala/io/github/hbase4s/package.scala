package io.github

import io.github.hbase4s.config.HBaseConfig
import io.github.hbase4s.utils.HBaseImplicitUtils._

/**
  * Created by Volodymyr.Glushak on 30/05/2017.
  */
package object hbase4s {

  def hBaseClient(conf: HBaseConfig, table: String) = new HBaseClient(new HBaseConnection(conf), table)


//  // be careful with overflowing // need weak collection with expiration policy
//  private val bytesCache = new TrieMap[Any, Array[Byte]]
//
//  def anyToBytesCached[T](a: T): Array[Byte] = bytesCache.getOrElseUpdate(a, anyToBytes(a))

  def anyToBytes[T](a: T): Array[Byte] = a match {
    case s: String => s
    case i: Int => i
    case l: Long => l
    case d: Double => d
    case f: Float => f
    case b: Boolean => b
    case sh: Short => sh
    case bd: BigDecimal => bd
    case ab: Array[Byte] => ab
    case x => sys.error(s"Type ${x.getClass.getSimpleName} of value $a is not supported.")
  }

}
