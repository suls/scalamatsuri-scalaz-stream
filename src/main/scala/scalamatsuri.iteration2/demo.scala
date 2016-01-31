package scalamatsuri.iteration2

import org.joda.time.{DateTimeZone, DateTime}
import scodec.bits.ByteVector

import scala.annotation.tailrec
import scalaz.{Kleisli, Applicative, Bind}

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

  import Pagination._

  def tradeReports[F[_]] (
    f: Offset => F[Page[TradeReport]]
  ) : Process[F, String] =
    pagedRequest(f, None)
      .map(_.productIterator.mkString(","))
      .prepend(Seq("datetime,contract,lots,price,direction"))
      .intersperse("\n")

  def writingTo(fileName: String)
               (data: Process[Task, String])  =
   data
     .pipe(text.utf8Encode)
     .to(io.fileChunkW(fileName))

  val program =
    writingTo("trade_reports.csv") {
      tradeReports(fetchTradeReports(DateTime.now))
    }
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
