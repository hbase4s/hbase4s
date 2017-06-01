package io.github.hbase4s.utils

import io.github.hbase4s.RecordFactory._
import io.github.hbase4s.{Field, _}
import io.github.hbase4s.utils.HBaseImplicitUtils._
import org.scalatest.{FlatSpec, Matchers}
import shapeless.HNil

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._


/**
  * Created by Volodymyr.Glushak on 18/05/2017.
  */
class TypeUtilsTest extends FlatSpec with Matchers {


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

  "It" should "extract option from types" in {

    val in = Option("optioned string")

    val ab = anyToBytes(in)

    ab.from(typeOf[Option[String]]) shouldBe in

    anyToBytes(None).from(typeOf[None.type]) shouldBe None

    val x: Any = Some("1") // issue is if we remove proper TypeTag, by defining variable as Any.
    anyToBytes(x).from(typeOf[Some[String]]) shouldBe Option("1")

    anyToBytes(Option(1)).from(typeOf[Option[Int]]) shouldBe Option(1)
    anyToBytes(Option(1.01)).from(typeOf[Option[Double]]) shouldBe Option(1.01)
    anyToBytes(Option(1L)).from(typeOf[Option[Long]]) shouldBe Option(1L)
    anyToBytes(Option(true)).from(typeOf[Option[Boolean]]) shouldBe Option(true)

  }

}
