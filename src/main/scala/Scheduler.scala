import akka.actor._
import scala.io.StdIn

object Scheduler {
  case class Task(data: String)
  case class TaskResult(result: String)
  case object WorkerCrashed

  def createScheduler(): ActorRef = {
    val system = ActorSystem("schedulerSystem")
    system.actorOf(Props[Scheduler], "scheduler")
  }
}

class Scheduler extends Actor {
  import Scheduler._

  def receive: Receive = {
    case Task(data) =>
      val worker = context.actorOf(Props[WorkerClass])
      worker ! Worker.DoTask(data)
    case TaskResult(result) =>
      println(s"Task successful: $result")
    case WorkerCrashed =>
      println("Task failed")
  }
}

class Worker extends Actor {
  import Scheduler._

  def receive: Receive = {
    case Worker.DoTask(data) =>
      if (math.random() < 0.5) {
        sender() ! WorkerCrashed
      } else {
        sender() ! TaskResult(s"Miau $data")
      }
  }
}

object Worker {
  case class DoTask(data: String)
}

object Schedule {
  def main(args: Array[String]): Unit = {
    val scheduler = Scheduler.createScheduler()

    while (true) {
      println("Enter a task to perform (or 'q' to quit):")
      val input = StdIn.readLine()

      if (input == "q") {
        println("Exiting...")
        System.exit(0)
      } else {
        scheduler ! Scheduler.Task(input)
      }
    }
  }
}