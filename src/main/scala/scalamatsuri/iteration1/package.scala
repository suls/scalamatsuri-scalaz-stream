package scalamatsuri

import org.joda.time.{DateTimeZone, DateTime}

//import scalaz._, Scalaz._
import scalaz.stream._
import scalaz.concurrent.Task

import Predef.???


package object iteration1 {
type Offset = Option[Int]
  def fetchTradeReports(dateTime: DateTime)
                       (offset: Offset) : Task[Page[TradeReport]] =
  Task.delay {
    Page(Seq(TradeReport(DateTime.now(DateTimeZone.UTC), "ES1", 4, 1875.0, Sell)), None)
  }

def pagedRequest[A] (
  f: Offset => Task[Page[A]],offset: Offset = None
): Process[Task, A] =
  Process
        .eval(f(offset))
        .flatMap { response : Page[A] =>
          Process.emitAll(response.results) ++
            response.offset.map { o =>
              pagedRequest(f, Option(o))
            }.getOrElse(Process.empty[Task, A])
        }

}
