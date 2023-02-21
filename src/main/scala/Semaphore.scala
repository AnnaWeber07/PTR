import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._

case object Acquire
case object Release
case object Acquired
case object SemaphoreStatus

class SemaphoreActor(var permits: Int) extends Actor {
  override def receive: Receive = {
    case Acquire =>
      if (permits > 0) {
        permits -= 1
        sender() ! Acquired
      } else {
        sender() ! false
      }
    case Release =>
      permits += 1
    case SemaphoreStatus =>
      println(s"Current number of permits available: $permits")
  }
}

object SemaphoreApp extends App {
  implicit val timeout: Timeout = 5.seconds
  val system = ActorSystem("SemaphoreSystem")
  val semaphoreActor: ActorRef = system.actorOf(Props(new SemaphoreActor(2)))

  var done = false
  while (!done) {
    print("Enter a command (acquire, release, quit): ")
    val input = scala.io.StdIn.readLine().toLowerCase
    input match {
      case "acquire" =>
        val future = semaphoreActor ? Acquire
        val result = Await.result(future, timeout.duration)
        result match {
          case Acquired =>
            println("Acquire result: true")
          case false =>
            println("Acquire result: false")
        }
        semaphoreActor ! SemaphoreStatus
      case "release" =>
        semaphoreActor ! Release
        semaphoreActor ! SemaphoreStatus
      case "quit" =>
        done = true
        system.terminate()
      case _ =>
        println("Invalid command, please try again.")
    }
  }
}