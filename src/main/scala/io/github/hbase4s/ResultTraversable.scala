package io.github.hbase4s

/**
  * Created by Volodymyr.Glushak on 18/05/2017.
  */
class ResultTraversable[K](data: Map[K, List[Field[Array[Byte]]]]) extends Traversable[WrappedResult[K]] {

  override def foreach[U](f: WrappedResult[K] => U): Unit = {
    data.foreach { case (k, d) => f(WrappedResult(k, d)) }
  }
}
