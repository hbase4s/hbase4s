package hbase4s

import hbase4s.utils.HBaseImplicitUtils.{anyToBytes, _}
import shapeless.ops.traversable.FromTraversable
import shapeless.{Generic, HList}

import scala.reflect.runtime.universe._

/**
  * Created by Volodymyr.Glushak on 19/05/2017.
  */
object RecordFactory {


  def classAccessors[T: TypeTag]: List[CCField] = typeOf[T].members.sorted.collect {
    case m: MethodSymbol if m.isCaseAccessor =>
      val fieldType = m.typeSignature.baseType(m.typeSignature.typeSymbol)
      val fieldName = m.name.decodedName.toString
      CCField(fieldType, fieldName)
  }

  def getCCParams(cc: Product): Map[String, Any] =
    cc.getClass.getDeclaredFields.filterNot(_.isSynthetic).map(_.getName).zip(cc.productIterator.toList).toMap

  def build[K, T <: AnyRef with Product](key: K, cc: T): CCRecord = {
    val family = colFamilyName(cc.getClass)
    val fields = getCCParams(cc).map { case (name, value) =>
      Field[Array[Byte]](family, name, anyToBytes(value))
    }.toList
    CCRecord(anyToBytes(key), fields)
  }

  def colFamilyName(clazz: Class[_]): String = clazz.getSimpleName.toLowerCase


  // not used
  def fromHListToCC[T](hlist: HList)(implicit gen: Generic[T]): T =
    hlist match {
      case hl: gen.Repr => gen.from(hl)
    }

  class FromListToCC[T: TypeTag](fields: List[Field[Array[Byte]]]) {

    // magic below: http://stackoverflow.com/questions/33840564/converting-a-list-to-a-case-class
    def asClass[R <: HList](implicit gen: Generic.Aux[T, R], tl: FromTraversable[R]): Option[T] = {
      tl(asList).map(gen.from)
    }

    def asList: List[Any] = {
      val fieldsDef = classAccessors[T]
      val familyName = typeOf[T].typeSymbol.name.toString.toLowerCase

      val data = fields.map(f => s"${f.family}:${f.name}" -> f).toMap

      fieldsDef.map(fd => {
        val fullName = s"$familyName:${fd.name}"
        val field = data.getOrElse(fullName, sys.error(s"Can't find field with name $fullName. Available fields ${data.keys.toList}"))
        // type based mapping
        field.value.from(fd.cType)
      })
    }
  }

  def typed[T: TypeTag](data: List[Field[Array[Byte]]]) = new FromListToCC[T](data)
}


trait Record {

  def key: Array[Byte]

  def values: List[Field[Array[Byte]]]

}

case class CCRecord(key: Array[Byte], values: List[Field[Array[Byte]]]) extends Record

case class CCField(cType: Type, name: String)