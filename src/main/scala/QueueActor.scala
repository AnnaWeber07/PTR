import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.io.StdIn
import scala.language.postfixOps

case class Push(value: Any)
case object Pop
case class Popped(value: Option[Any])

class QueueActor extends Actor {
  var queue: List[Any] = Nil

  def receive = {
    case Push(value) =>
      queue = queue :+ value
      sender() ! "ok"
    case Pop =>
      val value = queue.headOption
      queue = queue.drop(1)
      sender() ! Popped(value)
  }
}

class QueueHelper {
  val system = ActorSystem("QueueSystem")
  val actor = system.actorOf(Props[QueueActor])

  def push(value: Any): Future[String] = {
    implicit val timeout = Timeout(5 seconds)
    (actor ? Push(value)).mapTo[String]
  }

  def pop(): Future[Option[Any]] = {
    implicit val timeout = Timeout(5 seconds)
    (actor ? Pop).mapTo[Popped].map(_.value)
  }

  def shutdown(): Future[Terminated] = {
    system.terminate()
  }
}

object QueueAction extends App {
  val helper = new QueueHelper()

  while (true) {
    print("Enter command (push/pop/quit): ")
    val input = StdIn.readLine()

    input match {
      case "push" =>
        print("Enter value: ")
        val value = StdIn.readLine()
        helper.push(value).onComplete {
          case Success("ok") => println("Push successful")
          case Success(_) => println("Unexpected response from server")
          case Failure(e) => println(s"Push failed with error: ${e.getMessage}")
        }
      case "pop" =>
        helper.pop().onComplete {
          case Success(Some(value)) => println(s"Popped value: $value")
          case Success(None) => println("Queue is empty")
          case Failure(e) => println(s"Pop failed with error: ${e.getMessage}")
        }
      case "quit" =>
        helper.shutdown().onComplete(_ => System.exit(0))
      case _ =>
        println("Invalid command")
    }
  }
}