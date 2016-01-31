package scalamatsuri.iteration3

import org.joda.time.DateTime
import org.scalacheck.{Gen, Arbitrary}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scalaz.stream._


class test extends Specification with ScalaCheck {

  import shapeless.contrib.scalacheck._

  "summarizing is groupby and sum" >> {
    prop { (trs: List[TradeReport]) => (trs.nonEmpty) ==> {

      trs.groupBy(_.contract)
        .mapValues(_.foldLeft(EndOfDayPosition("", 0)) { (e, tr) =>
          EndOfDayPosition(tr.contract, tr.direction match {
            case Buy => tr.lots + e.position
            case Sell => e.position - tr.lots
          }  )

        }).values must
        containTheSameElementsAs(
          Process
            .emitAll(trs)
            .pipe(demo.summarize)
            .toList)
    }}
  }

  implicit def abStrings: Arbitrary[DateTime] =
    Arbitrary(Gen.posNum[Long].map(new DateTime(_)))
}
