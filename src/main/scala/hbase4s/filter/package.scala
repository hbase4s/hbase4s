package hbase4s

/**
  * Created by Volodymyr.Glushak on 22/05/2017.
  */
package object filter {

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
    def is(s: String) = RowPrefix(s)
  }

  object columnPrefix {
    def is(s: String) = ColumnPrefix(s)

    def in(ss: String*) = MultipleColumnPrefix(ss)
  }

  object columnLimit {
    def ===(i: Int) = ColumnCountGet(i)
  }

  object pageLimit {
    def ===(i: Int) = Page(i)
  }

  object stop {
    def on(s: String) = InclusiveStop(s)
  }

  object columnName {
    def is(s: String) = Qualifier(Eq, s)
  }

  object columnValue {
    def is(s: String) = Value(Eq, s)
  }

  implicit class FilterStr(s: String) {
    def f: Expr = FilterParser.parse(s)
  }

}
