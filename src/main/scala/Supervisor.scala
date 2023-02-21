import akka.actor._
import scala.concurrent.duration._
import scala.io.StdIn

case object Check
case class MonitorInstruction(instruction: String)
case object PrintAlive

class MonitoredActor extends Actor {
  implicit val ec = context.dispatcher
  val tick = context.system.scheduler.schedule(0.seconds, 5.seconds, self, PrintAlive)

  def receive = {
    case Check =>
      println("I'm still alive!")
    case MonitorInstruction(instruction) =>
      println(s"Received instruction: $instruction")
    case PrintAlive =>
      println("I'm still alive!")
  }

  override def postStop() {
    tick.cancel()
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

  while (true) {
    val input = StdIn.readLine("Enter an instruction for the monitored actor, or 'quit' to exit: ")
    if (input == "quit") {
      monitoredActor ! PoisonPill
      system.terminate()
      sys.exit()
    } else {
      monitoredActor ! MonitorInstruction(input)
    }
  }
}