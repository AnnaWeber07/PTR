import akka.actor.{Actor, ActorSystem, Props}

class Printer extends Actor {
  override def receive: Receive = {
    case msg: Any =>
      println(msg)
  }
}

object Print extends App {
  //PREVIOUS WEEKS INTERACTION MOVED TO "MARKED AS DONE" CLASS
  //week3

  //task 1: actor that prints any message it receives
  val system = ActorSystem("example-system")
  val printer = system.actorOf(Props[Printer], "printer")
  var input = "" //here's the input
  do {
    input = scala.io.StdIn.readLine()
    printer ! input
  } while (input != "quit") //exit command
  system.terminate()
}