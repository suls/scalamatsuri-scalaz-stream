package scalamatsuri.iteration3

import org.joda.time.{DateTimeZone, DateTime}
import scodec.bits.ByteVector

import scala.annotation.tailrec
import scalaz._
import scalaz.stream.Process.Env

//import scalaz._, Scalaz._
import scalaz.stream._
import scalaz.concurrent.Task

case class TradeReport(
  datetime: DateTime,
  contract: Contract,
  lots: Int,
  price: BigDecimal,
  direction: Direction
)

sealed trait Direction
case object Buy extends Direction
case object Sell extends Direction

case class EndOfDayPosition(
  contract: Contract,
  position: Int
)

case class Page[A](results: Seq[A], offset: Offset)

object demo {

  import Pagination._

  val q1 = async.boundedQueue[TradeReport](1)
  val q2 = async.boundedQueue[TradeReport](1)

  def tradeReports[F[_]] (
    f: Offset => F[Page[TradeReport]]
  ) : Process[F, TradeReport] =
    pagedRequest(f, Some(0))

  def writingTo(fileName: String)
               (data: Process[Task, String])  =
   data
     .pipe(text.utf8Encode)
     .to(io.fileChunkW(fileName))

  val fetcher: Process[Task, Unit] =
    tradeReports(fetchTradeReports(DateTime.now))
      .observe(q2.enqueue)
      .to(q1.enqueue)
      .onComplete(Process eval q1.close)
      .onComplete(Process eval q2.close)


  val tradeReportsCsv: Process[Task, Unit] =
    writingTo("trade_reports.csv") {
      q1.dequeue
        .map(_.productIterator.mkString(","))
        .prepend(Seq("datetime,contract,lots,price,direction"))
        .intersperse("\n")
    }

  val summarize =
    process1.fold(
      Map
        .empty[Contract, EndOfDayPosition]
        .withDefault(EndOfDayPosition(_, 0))
    ) { (s, tr:TradeReport) =>
      val eod = s(tr.contract)
      s + (tr.contract -> eod.copy(position = tr.direction match {
        case Buy => eod.position + tr.lots
        case Sell => eod.position - tr.lots
      }))
    }.flatMap(m => Process.emitAll(m.values.toSeq))

  val endOfDaySummary = writingTo("summary.csv") {
    q2.dequeue
      .pipe(summarize)
      .map(_.productIterator.mkString(","))
      .prepend(Seq("contract,position"))
      .intersperse("\n")
  }


  val all = Nondeterminism[Task].gatherUnordered(
    fetcher.run ::
    tradeReportsCsv.run ::
    endOfDaySummary.run :: Nil
  )

}

object Pagination {

  def pagedRequest[F[_], A] (
    f: Offset => F[Page[A]],
    offset: Offset
  ): Process[F, A] = {
    Process
      .eval(f(offset))
      .flatMap { response: Page[A] =>
        Process.emitAll(response.results) ++
          response.offset.map { o =>
            pagedRequest(f, Option(o))
          }.getOrElse(Process.empty[F, A])
      }

  }
}
