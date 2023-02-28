import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import scala.io.StdIn.readLine

object MainWorker extends App {
  val system = ActorSystem("SupervisedPoolSystem")
  val supervisor = system.actorOf(Props[Supervisor], "supervisor")

  while (true) {
    val line = readLine()
    supervisor ! line
    if (line == "exit") {
      system.terminate()
      sys.exit()
    }
  }
}

class Supervisor extends Actor {
  // Create a pool of 5 worker actors
  val pool: Seq[ActorRef] = for (i <- 1 to 5) yield context.actorOf(Props[WorkerClass], s"worker-$i")

  override def receive: Receive = {
    case msg: String =>
      // Pick a random worker actor from the pool to handle the message
      val worker = pool(scala.util.Random.nextInt(pool.length))
      worker.forward(msg)
    case Kill =>
      // Kill a random worker actor from the pool and start a new one to replace it
      val index = scala.util.Random.nextInt(pool.length)
      val oldWorker = pool(index)
      pool.updated(index, context.actorOf(Props[WorkerClass], s"worker-${index+1}"))
      oldWorker ! PoisonPill
  }
}

class WorkerClass extends Actor {
  override def receive: Receive = {
    case msg: String =>
      println(s"Received message: $msg")
  }
}

case object Kill