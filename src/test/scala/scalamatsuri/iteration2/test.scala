package scalamatsuri.iteration2

import org.scalacheck.Gen
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import scalamatsuri.iteration3.EndOfDayPosition
import scalaz.concurrent.Task
import scalaz.{\/-, \/, Catchable, Id, ~>}
import scalaz.Id._
import scalaz.stream._
import Pagination._


class test extends Specification with ScalaCheck {

  "pagination is flattening" >>
    prop { (is: List[List[Int]]) =>

      val f: (Offset) => Task[Page[Int]] = // ..
        (o:Offset) => Task.delay {
          val length = is.size

          val fs = is.zipWithIndex.map { case (i: List[Int], idx: Int) =>
            idx -> Page(i, if (idx < length-1) Some(idx+1) else None)
          }.toMap

          o.map(fs(_)).get
        }

      is.flatten must_== pagedRequest(f, Some(0)).runLog.run
    }.setGen(Gen.nonEmptyListOf(Gen.listOf(Gen.posNum[Int])))

}
