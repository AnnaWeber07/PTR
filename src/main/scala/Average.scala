import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.io.StdIn
import java.text.DecimalFormat

class Averager extends Actor with ActorLogging {
  var sum: Double = 0
  var count: Int = 0
  val format = new DecimalFormat("#.##")

  def receive: Receive = {
    case n: Double =>
      count += 1
      sum += n
      val avg = sum / count
      val formattedAvg = format.format(avg)
      log.info(s"Current average is $formattedAvg")
  }
}

object Averager {
  def props: Props = Props[Averager]
}

object Average {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("averager-system")
    val averager = system.actorOf(Averager.props, "averager")

    while (true) {
      try {
        print("Enter a number: ")
        val n = StdIn.readDouble()
        averager ! n
      } catch {
        case _: Throwable => System.exit(0)
      }
    }
  }
}