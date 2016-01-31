package scalamatsuri

import org.joda.time.{DateTimeZone, DateTime}

import scalaz.{Bind, Applicative}

//import scalaz._, Scalaz._
import scalaz.stream._
import scalaz.concurrent.Task

import Predef.???


package object iteration2 {

  type Offset = Option[Int]

  def fetchTradeReports(dateTime: DateTime)
                       (offset: Offset) : Task[Page[TradeReport]] =
  Task.delay {
    Page(Seq(TradeReport(DateTime.now(DateTimeZone.UTC), "ES1", 4, 1875.0, Sell)), None)
  }


}
