package io.github.hbase4s.filter

/**
  * Created by Volodymyr.Glushak on 11/05/2017.
  */
sealed trait Expr

sealed trait FilterExpr extends Expr

sealed trait ExprOpts extends Expr

case class TopLevelExpr(f: FilterExpr, eop: Seq[ExprOpts]) extends Expr

case class Column(family: String, name: String)

case class And(l: FilterExpr, r: FilterExpr) extends FilterExpr

case class Or(l: FilterExpr, r: FilterExpr) extends FilterExpr

/**
  * Filter returns only the key component of each key-value.
  */
case object KeyOnly extends FilterExpr

/**
  * Filter returns only the first key-value from each row.
  */
case object FirstKeyOnly extends FilterExpr

/**
  * Filter returns only those key-values present in a row that starts with the specified row prefix
  * @param p a prefix of a row key
  */
case class RowPrefix[T](p: T) extends FilterExpr

/**
  * Filter returns only those key-values present in a column that starts with the specified column prefix
  * @param cp a prefix of a column qualifier
  */
case class ColumnPrefix[T](cp: T) extends FilterExpr

/**
  * Filter returns key-values that are present in a column that starts with any of the specified column prefixes.
  * @param cp list of column qualifier prefixes
  */
case class MultipleColumnPrefix(cp: Seq[String]) extends FilterExpr

/**
  *  It returns the first limit number of columns in the table.
  * @param l limit
  */
case class ColumnCountGet(l: Int) extends FilterExpr

/**
  * It returns page size number of rows from the table.
  * @param l page size
  */
case class Page(l: Long) extends FilterExpr

/**
  * Filter returns all key-values present in rows up to and including the specified row.
  * @param rk a row key on which to stop scanning.
  */
case class InclusiveStop(rk: String) extends FilterExpr


/**
  * It compares each qualifier name with the comparator using the compare operator and if the comparison returns true,
  * it returns all the key-values in that column.
  * @param cp compare operator
  * @param q comparator - column qualifier to compare
  */
case class Qualifier(cp: CompareOp, q: String) extends FilterExpr


/**
  * It compares each column value with the comparator using the compare operator and if the comparison returns true,
  * it returns all the key-values in that column.
  * @param cp compare operator
  * @param q comparator - column value to compare
  */
case class Value[T](cp: CompareOp, q: T) extends FilterExpr

/**
  * If the column is found and the comparison with the comparator returns true, all the columns of the row will be emitted.
  * If the condition fails, the row will not be emitted.
  * @param col type that describes column family and name
  * @param op compare operator
  * @param value comparator - column value to compare
  * @param setFilterIfMissing - if false returns rows where column with such name missed, default is true
  */
case class SingleColVal[T](col: Column, op: CompareOp, value: T, setFilterIfMissing: Boolean = true) extends FilterExpr


case class StartRowId[T](q: T) extends ExprOpts

case class StopRowId[T](q: T) extends ExprOpts