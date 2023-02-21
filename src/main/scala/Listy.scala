import akka.actor._

case class Add(actor: ActorRef)
case class Next(actor: ActorRef)
case class Prev(actor: ActorRef)
case class Traverse()
case class Inverse()

class NodeActor(val value: Int) extends Actor {
  var next: Option[ActorRef] = None
  var prev: Option[ActorRef] = None

  def receive = {
    case Add(actor) =>
      next = Some(actor)
    case Next(actor) =>
      next = Some(actor)
      actor ! Prev(self)
    case Prev(actor) =>
      prev = Some(actor)
    case Traverse() =>
      var current = self
      var values = List[Int]()
      while (current != null) {
        values = values :+ current.asInstanceOf[NodeActor].value
        current = current.asInstanceOf[NodeActor].next.getOrElse(null)
      }
      println(values)
    case Inverse() =>
      var current = self
      var values = List[Int]()
      while (current != null) {
        values = values :+ current.asInstanceOf[NodeActor].value
        current = current.asInstanceOf[NodeActor].prev.getOrElse(null)
      }
      println(values)
  }
}

object DoublyLinkedListActor {
  def main(args: Array[String]) {
    val system = ActorSystem("DoublyLinkedListSystem")

    println("Enter the number of doubly linked lists to create:")
    val numLists = scala.io.StdIn.readInt()

    for (i <- 0 until numLists) {
      println(s"Enter the values for list $i, separated by spaces:")
      val values = scala.io.StdIn.readLine().split(" ").map(_.toInt)

      var prevActor: Option[ActorRef] = None
      var firstActor: Option[ActorRef] = None

      for (value <- values) {
        val nodeActor = system.actorOf(Props(new NodeActor(value)), s"NodeActor-$i-$value")

        if (prevActor.isDefined) {
          prevActor.get ! Next(nodeActor)
          nodeActor ! Prev(prevActor.get)
        } else {
          firstActor = Some(nodeActor)
        }

        prevActor = Some(nodeActor)
      }

      if (prevActor.isDefined && firstActor.isDefined) {
        prevActor.get ! Next(firstActor.get)
        firstActor.get ! Prev(prevActor.get)

        val traverseMsg = Traverse()
        firstActor.get ! traverseMsg
        val inverseMsg = Inverse()
        prevActor.get ! inverseMsg
      }
    }

    system.terminate()
  }
}