package week3

import scala.annotation.tailrec

// Provide the necessary tools to do any kind of discrete event simulation.
// Subclasses will provide the ability to do something more specific, e.g. simulate circuits.
trait Simulation {
  type Action = () => Unit

  case class Event(time: Int, action: Action)

  private var curtime = 0
  def currentTime: Int = curtime

  private type Agenda = List[Event]
  private var agenda: Agenda = Nil

  def afterDelay(delay: Int)(block: => Unit): Unit = {
    val item = Event(currentTime + delay, () => block) // I initially omitted adding currentTime to delay.
    agenda = insert(agenda, item)
  }

  private def insert(agenda: Agenda, item: Event): Agenda = agenda match {
    case first :: rest if first.time <= item.time =>
      first :: insert(rest, item)
    case _ =>
      item :: agenda
  }

  @tailrec
  private def loop(): Unit = agenda match {
    case first :: rest =>
      agenda = rest
      curtime = first.time
      first.action()
      loop()
    case Nil =>
  }

  def run(): Unit = {
    afterDelay(0) {
      println(s"*** simulation started, time = $currentTime ***")
    }
    loop()
  }
}
