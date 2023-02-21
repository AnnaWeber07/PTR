import Main.timeout
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.io.StdIn
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask

case object Traverse
case object Inverse

class Node(value: Int, var prev: Option[ActorRef], var next: Option[ActorRef]) extends Actor {
  def receive = {
    case Traverse =>
      val nextNode = next.getOrElse(sender())
      val list = Await.result(nextNode ? Traverse, 5.seconds).asInstanceOf[List[Int]]
      sender() ! (value :: list)

    case Inverse =>
      val prevNode = prev.getOrElse(sender())
      val list = Await.result(prevNode ? Inverse, 5.seconds).asInstanceOf[List[Int]]
      sender() ! (value :: list)
  }
}

class DLList(head: ActorRef, tail: ActorRef) {
  def traverse(): List[Int] = {
    val result = Await.result(head ? Traverse, 5.seconds).asInstanceOf[List[Int]]
    result.reverse
  }

  def inverse(): List[Int] = {
    val result = Await.result(tail ? Inverse, 5.seconds).asInstanceOf[List[Int]]
    result.reverse
  }
}

object DLList {
  def apply(values: List[Int])(implicit system: ActorSystem): DLList = {
    val nodes = values.map { value => system.actorOf(Props(new Node(value, None, None))) }
    for (i <- nodes.indices) {
      nodes(i) ! Inverse -> (if (i == 0) None else Some(nodes(i-1)))
      nodes(i) ! Traverse -> (if (i == nodes.length - 1) None else Some(nodes(i+1)))
    }
    val head = nodes.head
    val tail = nodes.last
    DLList(head, tail)
  }
}

object DLListApp extends App {
  implicit val system = ActorSystem("DLListSystem")

  print("Enter a list of integers, separated by spaces: ")
  val values = StdIn.readLine().trim.split(" ").map(_.toInt).toList

  val list = DLList(values)

  while (true) {
    println("Enter a command (traverse, inverse, quit):")
    val input = StdIn.readLine().trim.toLowerCase

    input match {
      case "traverse" =>
        val result = list.traverse()
        println(s"List: $result")

      case "inverse" =>
        val result = list.inverse()
        println(s"Inverse list: $result")

      case "quit" =>
        system.terminate()
        System.exit(0)

      case _ =>
        println(s"Invalid command: $input")
    }
  }
}