import akka.actor.{Actor, ActorSystem, OneForOneStrategy, Props}
import akka.actor.SupervisorStrategy._

import scala.concurrent.duration._
import scala.language.postfixOps

object PulpFictionSupervision extends App {

  // Define the messages that will be passed between actors
  case object StartConversation
  case object WhatDoesMarcellusWallaceLookLike
  case object WhatCountryAreYouFrom
  case object TheySpeakEnglishAndWhat
  case object DoYouSpeakIt
  case object SayWhatAgain
  case object DescribeMarcellusWallace
  case object DoesHeLookLikeABitch

  case object English
  case object Bald
  case object Yes

  // Define the actors
  class Questioner extends Actor {
    val responder = context.actorOf(Props[Responder], "responder")
    def receive = {
      case StartConversation =>
        responder ! WhatDoesMarcellusWallaceLookLike
      case WhatDoesMarcellusWallaceLookLike =>
        println("Questioner: What does Marcellus Wallace look like?")
        responder ! SayWhatAgain
      case SayWhatAgain =>
        println("Questioner: What? Say what again?")
        responder ! DoYouSpeakIt
      case TheySpeakEnglishAndWhat =>
        println("Questioner: They speak English and what?")
        responder ! English
      case DoYouSpeakIt =>
        println("Questioner: Say 'what' again. I dare you, I double dare you, motherfucker.")
        throw new Exception("Responder: What?") // Simulate an error
      case DescribeMarcellusWallace =>
        println("Questioner: Describe what Marcellus Wallace looks like.")
        responder ! Bald
      case DoesHeLookLikeABitch =>
        println("Questioner: Does he look like a bitch?")
        throw new Exception("Responder: What?") // Simulate an error
      case WhatCountryAreYouFrom =>
        println("Questioner: What country are you from?")
        throw new Exception("Responder: What?") // Simulate an error
    }
  }

  class Responder extends Actor {
    def receive = {
      case WhatDoesMarcellusWallaceLookLike =>
        println("Responder: He's black.")
        sender() ! SayWhatAgain
      case SayWhatAgain =>
        println("Responder: He's bald.")
        sender() ! TheySpeakEnglishAndWhat
      case English =>
        println("Responder: Yes.")
        sender() ! DescribeMarcellusWallace
      case Bald =>
        println("Responder: He's black.")
        sender() ! DoesHeLookLikeABitch
      case DoesHeLookLikeABitch =>
        println("Responder: What?")
        throw new Exception("Questioner: I jail. You not jail.") // Simulate an error
    }
  }

  class Supervisor extends Actor {
    override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }

    val questioner = context.actorOf(Props[Questioner], "questioner")

    def receive = {
      case StartConversation =>
        questioner ! StartConversation
    }
  }

  // Create the system and start the conversation
  val system = ActorSystem("PulpFictionSupervision")
  val supervisor = system.actorOf(Props[Supervisor], "supervisor")
  supervisor ! StartConversation
}