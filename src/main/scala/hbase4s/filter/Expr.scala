package hbase4s.filter

/**
  * Created by Volodymyr.Glushak on 11/05/2017.
  */
sealed trait Expr

case class Column(family: String, name: String)

case class FilterOp(col: Column, op: CompareOp, value: String, setFilterIfMissing: Boolean = true) extends Expr

case class And(l: Expr, r: Expr) extends Expr

case class Or(l: Expr, r: Expr) extends Expr



