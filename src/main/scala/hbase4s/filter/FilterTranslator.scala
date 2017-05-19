package hbase4s.filter


import hbase4s.utils.HBaseImplicitUtils._
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp._
import org.apache.hadoop.hbase.filter.{Filter, FilterList, SingleColumnValueFilter}

/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
object FilterTranslator {

  private[this] def compareOpHbase(s: CompareOp) = s match {
    case Eq => EQUAL
    case Greater => GREATER
    case GreaterOrEq => GREATER_OR_EQUAL
    case Less => LESS
    case LessOrEq => LESS_OR_EQUAL
    case NonEq => NOT_EQUAL
  }

  def fromExpr(e: Expr): Filter = e match {
    case FilterOp(col, op, v, ifMissing) =>
      val scvf = new SingleColumnValueFilter(col.family, col.name, compareOpHbase(op), v)
      scvf.setFilterIfMissing(ifMissing)
      scvf
    case And(l, r) => new FilterList(FilterList.Operator.MUST_PASS_ALL, fromExpr(l), fromExpr(r))
    case Or(l, r) => new FilterList(FilterList.Operator.MUST_PASS_ONE, fromExpr(l), fromExpr(r))
  }
}



