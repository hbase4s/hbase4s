package io.github.hbase4s

import io.github.hbase4s.utils.HBaseImplicitUtils.asBytes
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Volodymyr.Glushak on 23/05/2017.
  */
class WrappedResultTest extends FlatSpec with Matchers {

  "It" should "produce correct results" in {
    val data = List(
      Field("e", "create_date", asBytes("2029-01-01")),
      Field("e", "int", asBytes(1)),
      Field("e", "long", asBytes(1l)),
      Field("e", "double", asBytes(1.0d)),
      Field("e", "float", asBytes(1.0f)),
      Field("e", "short", asBytes(1.toShort)),
      Field("e", "bd", asBytes(BigDecimal(1)))
    )
    val res = new WrappedResult[String]("row", data)

    res.asBigDecimal("e:bd") shouldBe BigDecimal(1)
    res.asShort("e:short") shouldBe 1.toShort
    res.asFloat("e:float") shouldBe 1.0f
    res.asDouble("e:double") shouldBe 1.0d
    res.asLong("e:long") shouldBe 1l
    res.asInt("e:int") shouldBe 1
    res.asString("e:create_date") shouldBe "2029-01-01"
  }

}
