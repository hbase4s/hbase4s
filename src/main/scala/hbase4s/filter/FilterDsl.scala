package hbase4s.filter

/**
  * Created by Volodymyr.Glushak on 21/05/2017.
  */
object FilterDsl {

  def c(fam: String, name: String) = Column(fam, name)

  implicit class RichCol(a: Column) {

    def ===[V](b: V) = SingleColVal(a, Eq, b)

    def !==[V](b: V) = SingleColVal(a, NonEq, b)

    def <[V](b: V) = SingleColVal(a, Less, b)

    def <=[V](b: V) = SingleColVal(a, LessOrEq, b)

    def >[V](b: V) = SingleColVal(a, Greater, b)

    def >=[V](b: V) = SingleColVal(a, GreaterOrEq, b)

  }

  implicit class RichExpr(a: Expr) {

    def &(b: Expr) = And(a, b)

    def |(b: Expr) = Or(a, b)

  }

  def keys = KeyOnly

  def firstKeys = FirstKeyOnly

  object rowPrefix {
    def ===(s: String) = RowPrefix(s)
  }

  object columnPrefix {
    def ===(s: String) = ColumnPrefix(s)
  }

}