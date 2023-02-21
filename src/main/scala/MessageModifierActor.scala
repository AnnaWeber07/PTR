import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

class MessageModifierActor extends Actor {
  def receive = {
    case i: Int =>
      val modified = i + 1
      sender() ! s"Received: $modified"
    case s: String =>
      val modified = s.toLowerCase()
      sender() ! s"Received: $modified"
    case _ =>
      sender() ! "Received: I don't know how to handle this!"
  }
}

object Main extends App {
  implicit val timeout: Timeout = Timeout(5 seconds)
  val system = ActorSystem("MessageModifierSystem")
  val actor = system.actorOf(Props[MessageModifierActor], "messageModifierActor")

  while (true) {
    val message = StdIn.readLine("Enter a message: ")
    if (message == "exit") {
      val responseFuture = actor ? message
      val response = Await.result(responseFuture, timeout.duration).asInstanceOf[String]
      println(response)
      system.terminate()
      sys.exit(0)
    } else {
      val responseFuture = actor ? message
      val response = Await.result(responseFuture, timeout.duration).asInstanceOf[String]
      println(response)
    }
  }
}