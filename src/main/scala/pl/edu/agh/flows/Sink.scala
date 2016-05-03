package pl.edu.agh.flows

import akka.actor.{ActorContext, ActorLogging, Props, Actor}
import pl.edu.agh.messages.{GetGroupedOut, GetOut, Get, ResultMessage}

class Sink[R] extends Actor with ActorLogging {

  var out = List.empty[R]

  def receive = {
    case ResultMessage(data: R) =>
      //log.info("CHILD")
      out :+= data
    case GetOut =>
      val o = out
      out = List.empty[R]
      sender ! o
    case GetGroupedOut(size: Int) =>
      val o = out
      out = List.empty[R]
      sender ! o.grouped(size)
    case Get =>
      sender ! this
  }

}

object Sink {
  def apply[R](context: ActorContext) = context.actorOf(Sink.props)
  def apply[R](name: String, context: ActorContext) = context.actorOf(Sink.props, name)
  def props[R] = Props[Sink[R]]
}
