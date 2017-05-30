package io.github.hbase4s.filter

import io.github.hbase4s._
import io.github.hbase4s.utils.HBaseImplicitUtils._
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp._
import org.apache.hadoop.hbase.filter._


/**
  * Created by Volodymyr.Glushak on 10/05/2017.
  */
object FilterTranslator {

  private[this] def compareOpHBase(s: CompareOp) = s match {
    case Eq => EQUAL
    case Greater => GREATER
    case GreaterOrEq => GREATER_OR_EQUAL
    case Less => LESS
    case LessOrEq => LESS_OR_EQUAL
    case NonEq => NOT_EQUAL
  }

  def scanFromExpr(e: Expr): Scan = {
    val scan = new Scan()
    e match {
      case TopLevelExpr(filter, op) =>
        scan.setFilter(fromExpr(filter))
        op.foreach {
          case StartRowId(rid) => scan.setStartRow(rid)
          case StopRowId(rid) => scan.setStopRow(rid)
        }
      case filter: FilterExpr =>
        scan.setFilter(fromExpr(filter))
    }
    scan
  }

  // this should produce Scan object instead of Filter (there are conditions that applied on Scan directly).
  def fromExpr(e: FilterExpr): Filter = e match {
    case KeyOnly => new KeyOnlyFilter
    case FirstKeyOnly => new FirstKeyOnlyFilter
    case RowPrefix(s) => new PrefixFilter(s)
    case ColumnPrefix(cp) => new ColumnPrefixFilter(cp)
    case MultipleColumnPrefix(pfxs) => new MultipleColumnPrefixFilter(pfxs.map(asBytes).toArray)
    case ColumnCountGet(i) => new ColumnCountGetFilter(i)
    case Page(p) => new PageFilter(p)
    case InclusiveStop(r) => new InclusiveStopFilter(r)
    case Qualifier(op, q) => new QualifierFilter(compareOpHBase(op), new BinaryComparator(q))
    case Value(op, q) => new ValueFilter(compareOpHBase(op), new BinaryComparator(anyToBytes(q)))

    case SingleColVal(col, op, v, ifMissing) =>
      val scvf = new SingleColumnValueFilter(col.family, col.name, compareOpHBase(op), anyToBytes(v))
      scvf.setFilterIfMissing(ifMissing)
      scvf
    case And(l, r) => new FilterList(FilterList.Operator.MUST_PASS_ALL, fromExpr(l), fromExpr(r))
    case Or(l, r) => new FilterList(FilterList.Operator.MUST_PASS_ONE, fromExpr(l), fromExpr(r))
  }
}



