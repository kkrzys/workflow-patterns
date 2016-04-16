package pl.edu.agh.main

import pl.edu.agh.actions.NamedMultipleAction
import pl.edu.agh.flows._
import pl.edu.agh.workflow.Workflow
import pl.edu.agh.workflow_patterns.merge.Merge
import pl.edu.agh.workflow_patterns.split.Split
import pl.edu.agh.utils.ActorUtils.Implicits._
import pl.edu.agh.dsl.WorkFlowDsl._
import pl.edu.agh.workflow_patterns.synchronization.Sync

/** Prosty test majacy sprawdzic czy mozna uzyc roznych typow danych dla wejsc */
object AnyTypeInputTest extends App {

  val act = identity(_: Any)

  val sum = { in: List[Any] =>
    val res = in match {
      case i: List[Double] => i.reduceLeft[Double](_+_)
      case i: List[Int] => i.reduceLeft[Int](_+_)
      case _ => identity(in)
    }
    res
  }

  val sumOnlyNumbers = NamedMultipleAction[Any, Any] { ins =>
    val res = (ins("firstIn"), ins("secondIn"), ins("thirdIn")) match {
      case (i1: Int, i2: Int, i3: Int) => i1 + i2 + i3
      case (i1: Int, i2: Double, i3: Int) => i1 + i2 + i3
      case (i1: Int, i2: Int, i3: Double) => i1 + i2 + i3
      case (i1: Int, i2: String, i3: Double) => i1 + i3
      case _ => identity(ins("firstIn"), ins("secondIn"), ins("thirdIn"))
    }
    res
  }

  val sumSyncProc = Sync (
    name = "sumProc",
    ins = Seq("firstIn", "secondIn", "thirdIn"),
    outs = Seq("sumOut_1", "sumOut_2"),
    action = sumOnlyNumbers,
    sendTo = "sumOut_1"
  )

  val splitProc = Split (
    name = "splitProc",
    numOfOuts = 3,
    action = act
  )

  val mergeProc = Merge (
    name = "mergeProc",
    outs = Seq("firstOut", "secondOut"),
    action = sum,
    sendTo = "secondOut"
  )

  val w = Workflow (
    "Any type input test",
    numOfIns = 3,
    numOfOuts = 3,
    (ins: Seq[In[Any]], outs: Seq[Out[Any]]) => {
      ins(0) ~>> sumSyncProc
      ins(1) ~>> sumSyncProc
      ins(2) ~>> sumSyncProc

      sumSyncProc.outs("sumOut_1").grouped(10) ~> mergeProc

      mergeProc.outs("secondOut") ~> splitProc

      splitProc.outs(0) ~>> outs(0)
      splitProc.outs(1) ~>> outs(1)
      splitProc.outs(2) ~>> outs(2)
    }
  )

  AnyRangeSource(1 to 10) ~> w.ins(0)
  AnySource("java", "scala", "akka", "drzewo", "lato", 1, 2, 3, 4, 5) ~> w.ins(1)
  AnySource(1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 100.0) ~> w.ins(2)

  val res = w.run
  println(res)
  println(w)
}