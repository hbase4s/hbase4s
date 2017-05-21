package hbase4s.filter


import hbase4s.utils.HBaseImplicitUtils._
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp._
import org.apache.hadoop.hbase.filter._

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
    case KeyOnly => new KeyOnlyFilter
    case FirstKeyOnly => new FirstKeyOnlyFilter
    case RowPrefix(s) => new PrefixFilter(s)
    case ColumnPrefix(cp) => new ColumnPrefixFilter(cp)
    case MultipleColumnPrefix(pfxs) => new MultipleColumnPrefixFilter(pfxs.map(asBytes).toArray)
    case ColumnCountGet(i) => new ColumnCountGetFilter(i)
    case Page(p) => new PageFilter(p)
    case InclusiveStop(r) => new InclusiveStopFilter(r)
    // TODO: prefix is smth else - those filters need to be revisited...
    // case Qualifier(op, q) => new QualifierFilter(compareOpHbase(op), q)
    // case Value(op, q) => new ValueFilter(compareOpHbase(op), q)

    case SingleColVal(col, op, v, ifMissing) =>
      val scvf = new SingleColumnValueFilter(col.family, col.name, compareOpHbase(op), v)
      scvf.setFilterIfMissing(ifMissing)
      scvf
    case And(l, r) => new FilterList(FilterList.Operator.MUST_PASS_ALL, fromExpr(l), fromExpr(r))
    case Or(l, r) => new FilterList(FilterList.Operator.MUST_PASS_ONE, fromExpr(l), fromExpr(r))
  }
}



