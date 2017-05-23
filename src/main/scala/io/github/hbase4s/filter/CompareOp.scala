package io.github.hbase4s.filter

/**
  * Created by Volodymyr.Glushak on 11/05/2017.
  */
sealed trait CompareOp

case object Eq extends CompareOp

case object NonEq extends CompareOp

case object Less extends CompareOp

case object LessOrEq extends CompareOp

case object Greater extends CompareOp

case object GreaterOrEq extends CompareOp
