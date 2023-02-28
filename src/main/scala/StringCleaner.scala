import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, SupervisorStrategy}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

// Define messages to be sent between actors
case class CleanString(input: String)

case class SplitString(words: Array[String])

case class LowercaseAndSwap(words: Array[String])

case class JoinString(cleanedString: String)

// Define actors
class SplitStringActor(nextActor: ActorRef) extends Actor with ActorLogging {
  override def receive: Receive = {
    case CleanString(input) =>
      val words = Try(input.split("\\s+")) match {
        case Success(value) => value
        case Failure(exception) =>
          log.error(s"Error splitting string: ${exception.getMessage}")
          throw exception
      }
      nextActor ! SplitString(words)
  }
}


class LowercaseAndSwapActor(nextActor: ActorRef) extends Actor with ActorLogging {

  def switcher(arr: Array[String]): Array[String] = {
    for (i <- 0 until arr.length) {
      var str = arr(i)
      for (j <- 0 until str.length) {
        if (str(j) == 'm') {
          str = str.updated(j, 'n')
        } else if (str(j) == 'n') {
          str = str.updated(j, 'm')
        } else {
          // If the current character is neither 'n' nor 'm',
          // move on to the next character

        }
      }
      arr(i) = str
    }
    arr
  }

  override def receive: Receive = {
    case SplitString(words) =>
      //val cleanedWords =  words.map(word => word.toLowerCase().replace('m', 'n').replace('n', 'm'))
      val cleanedWords = switcher(words)
      nextActor ! LowercaseAndSwap(cleanedWords)
  }
}


class JoinStringActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case LowercaseAndSwap(words) =>
      val cleanedString = Try(words.mkString(" ")) match {
        case Success(value) => value
        case Failure(exception) =>
          log.error(s"Error joining string: ${exception.getMessage}")
          throw exception
      }
      log.info(s"Cleaned string: $cleanedString")
  }
}

// Define supervisor strategy for actors
class StringCleaningSupervisor extends Actor with ActorLogging {

  import akka.actor.OneForOneStrategy
  import scala.concurrent.duration._

  // Restart the child actor in case of a failure
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5, withinTimeRange = 1 minute) {
    case _: Exception => SupervisorStrategy.Restart
  }

  // Create child actors and supervise them
  val joinStringActor = context.actorOf(Props[JoinStringActor], "joinStringActor")
  val lowercaseAndSwapActor = context.actorOf(Props(new LowercaseAndSwapActor(joinStringActor)), "lowercaseAndSwapActor")
  val splitStringActor = context.actorOf(Props(new SplitStringActor(lowercaseAndSwapActor)), "splitStringActor")

  // Send initial message to start the processing line
  override def preStart(): Unit = {
    splitStringActor ! CleanString("Messy String To Be Cleaned")
  }

  override def receive: Receive = {
    case _ =>
  }
}

// Create actor system and start supervisor actor
object StringCleaningApp extends App {
  val system = ActorSystem("StringCleaningSystem")
  val supervisor = system.actorOf(Props[StringCleaningSupervisor], "supervisor")
  Thread.sleep(1000)
  system.terminate()
}