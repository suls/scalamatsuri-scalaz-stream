package scalamatsuri

import org.joda.time.{DateTimeZone, DateTime}

import scalaz.concurrent.Task

package object iteration3 {
  type Offset = Option[Int]
  type Contract = String

  def fetchTradeReports(dateTime: DateTime)
                       (offset: Offset) : Task[Page[TradeReport]] =
  Task.delay {
    Page(Seq(
      TradeReport(DateTime.now(DateTimeZone.UTC), "ES1", 4, 1875.0, Sell),
      TradeReport(DateTime.now(DateTimeZone.UTC), "N225", 100, 17275, Sell),
      TradeReport(DateTime.now(DateTimeZone.UTC), "ES1", 5, 1875.0, Buy),
      TradeReport(DateTime.now(DateTimeZone.UTC), "ES1", 4, 1875.0, Buy)
    ), None)
  }


}
