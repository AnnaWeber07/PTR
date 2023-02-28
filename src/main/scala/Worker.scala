import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.io.StdIn

object WorkerActor {
  def props: Props = Props(new WorkerActor)
}

class WorkerActor extends Actor {
  override def preStart(): Unit = {
    println(s"${self.path.name} is starting up")
  }

  override def receive: Receive = {
    case message: Any =>
      println(s"${self.path.name} received message: $message")
      sender() ! message
    case "kill" =>
      println(s"${self.path.name} received kill message, restarting...")
      context.stop(self)
      context.system.actorOf(WorkerActor.props, self.path.name)
  }

  override def postRestart(reason: Throwable): Unit = {
    println(s"${self.path.name} is restarting")
    println(s"${self.path.name} says hello!")
  }
}

object SupervisorActor {
  case object GetAllWorkers
  case class GetWorker(name: String)
}

class SupervisorActor(numWorkers: Int) extends Actor {
  import SupervisorActor._

  private val workers = (1 to numWorkers).map { i =>
    context.actorOf(WorkerActor.props, s"worker-$i")
  }

  override def receive: Receive = {
    case GetAllWorkers =>
      sender() ! workers
    case GetWorker(name) =>
      val worker = workers.find(_.path.name == name)
      sender() ! worker
    case message: Any =>
      workers.foreach(_ ! message)
  }
}

object WorkingStuff extends App {
  val system = ActorSystem("worker-pool")

  val supervisor = system.actorOf(Props(new SupervisorActor(5)), "supervisor")

  while (true) {
    val message = StdIn.readLine()
    supervisor ! message
    if (message == "kill") {
      Thread.sleep(1000)
    }
  }

  system.terminate()
}