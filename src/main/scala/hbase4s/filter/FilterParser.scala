package hbase4s.filter

import hbase4s.filter.FilterParser._
import org.parboiled2._

import scala.util.{Failure, Success}

/**
  * Created by Volodymyr.Glushak on 11/05/2017.
  */
class FilterParser(val input: ParserInput) extends Parser {

  def InputLine: Rule1[Expr] = rule {
    MultiExpr ~ EOI
  }

  private[this] def MultiExpr: Rule1[Expr] = rule {
    Factor ~ zeroOrMore(
      ignoreCase("or") ~ Factor ~> Or
        | ignoreCase("and") ~ Factor ~> And
    )
  }

  private[this] def Factor: Rule1[Expr] = rule {
    SingleExprWrapped | SingleExpr | MultiExprWrapped | MultiExpr
  }

  private[this] def MultiExprWrapped = rule {
    openBracket ~ MultiExpr ~ closeBracket
  }

  private[this] def SingleExprWrapped = rule {
    openBracket ~ SingleExpr ~ closeBracket
  }

  private[this] def SingleExpr: Rule1[FilterOp] = rule {
    FieldName ~ ws ~ OpExpr ~ ws ~ QuotedText ~> ((a: Column, b: CompareOp, c: String) => FilterOp(a, b, c))
  }

  private[this] def OpExpr: Rule1[CompareOp] = rule {
    capture(oneOrMore(anyOf("=<>!"))) ~> compareOp _
  }

  private[this] def FieldName: Rule1[Column] = rule {
    Keyword ~ ":" ~ Keyword ~> Column
  }

  private[this] def Keyword: Rule1[String] = rule {
    zeroOrMore(Quote) ~ capture(oneOrMore(CharPredicate.AlphaNum | anyOf("_"))) ~ zeroOrMore(Quote)
  }

  private[this] def QuotedText: Rule1[String] = rule {
    Quote ~ capture(oneOrMore(CharPredicate.All -- '"' | EscapedQuote)) ~ Quote | Keyword
  }

  private[this] def EscapedQuote = "\\\""

  private[this] def Quote = rule {
    "\""
  }

  private[this] def openBracket = rule {
    ws ~ "(" ~ ws
  }

  private[this] def closeBracket = rule {
    ws ~ ")" ~ ws
  }

  private[this] def ws = rule {
    zeroOrMore(WhiteSpaceChar)
  }
}

object FilterParser {

  protected val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  protected val QuoteBackslash = CharPredicate("\"\\")

  protected def compareOp(s: String): CompareOp = s match {
    case "=" | "==" => Eq
    case ">" => Greater
    case ">=" => GreaterOrEq
    case "<" => Less
    case "<=" => LessOrEq
    case "!=" | "<>" => NonEq
    case x => sys.error(s"Unsupported operation: $x")
  }

  def parse(in: String): Expr = {
    val parser = new FilterParser(in)
    parser.InputLine.run() match {
      case Success(s) => s
      case Failure(x) => x match {
        case pe: ParseError => sys.error(parser.formatError(pe))
        case z => throw z
      }
    }
  }
}
