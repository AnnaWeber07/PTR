import akka.actor._
import scala.io.StdIn

case object Check
case class MonitorInstruction(instruction: String)

class MonitoredActor extends Actor {
  def receive = {
    case Check =>
      println("I'm still alive!")
    case MonitorInstruction(instruction) =>
      println(s"Received instruction: $instruction")
  }
}

class MonitoringActor(monitored: ActorRef) extends Actor {
  def receive = {
    case Terminated(_) =>
      println("The monitored actor has stopped!")
      context.system.terminate()
    case MonitorInstruction(instruction) =>
      monitored ! MonitorInstruction(instruction)
    case _ =>
      monitored ! Check
  }

  override def preStart() {
    context.watch(monitored)
  }
}

object Supervisor extends App {
  val system = ActorSystem("MonitoringSystem")

  val monitoredActor = system.actorOf(Props[MonitoredActor], "monitoredActor")
  val monitoringActor = system.actorOf(Props(new MonitoringActor(monitoredActor)), "monitoringActor")

  var input = ""
  while (input != "quit") {
    print("Enter an instruction for the monitored actor, or 'quit' to exit: ")
    input = StdIn.readLine()
    if (input != "quit") {
      monitoredActor ! MonitorInstruction(input)
    }
  }

  monitoredActor ! PoisonPill
  system.terminate()
}