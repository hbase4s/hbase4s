package hbase4s.utils

import hbase4s.Field
import hbase4s.RecordFactory._
import hbase4s.utils.HBaseImplicitUtils._
import org.scalatest.{FlatSpec, Matchers}
import shapeless.HNil

/**
  * Created by Volodymyr.Glushak on 18/05/2017.
  */
class ShapelessUtilsTest extends FlatSpec with Matchers {


  case class Foo(i: Int, str: String, b: Boolean)

  val foo = Foo(23, "STRI", b = true)

  "It" should "build case class from HList" in {

    val hList = 23 :: "STRI" :: true :: HNil
    fromHListToCC[Foo](hList) shouldBe foo

  }

  "It" should "build case class from Fields" in {
    val hbaseFields = List(
      Field[Array[Byte]]("foo", "i", 23),
      Field[Array[Byte]]("foo", "str", "STRI"),
      Field[Array[Byte]]("foo", "b", true)
    )
    typed[Foo](hbaseFields).asClass shouldBe foo
  }

}
