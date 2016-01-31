package scalamatsuri.iteration1

import org.joda.time.DateTime

//import scalaz._, Scalaz._
import scalaz.stream._
import scalaz.concurrent.Task

case class TradeReport(
  datetime: DateTime,
  contract: String,
  lots: Int,
  price: BigDecimal,
  direction: Direction
)

sealed trait Direction
case object Buy extends Direction
case object Sell extends Direction

case class Page[A](results: Seq[A], offset: Offset)

object demo {

  val tradeReports =
    pagedRequest(fetchTradeReports(DateTime.now))
      .map(_.productIterator.mkString(","))
      .prepend(Seq("datetime,contract,lots,price,direction"))
      .intersperse("\n")
      .pipe(text.utf8Encode)
      .to(io.fileChunkW("trade_reports.csv"))
}
