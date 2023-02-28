import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

object CarSensorSystem {
  // Message sent to a sensor actor to get its current measurement
  case object GetMeasurement

  // Message sent to a sensor actor to update its measurement
  case class UpdateMeasurement(value: Double)

  // Message sent to the supervisor actor to report a sensor crash
  case class SensorCrash(sensor: ActorRef)

  // Message sent to the supervisor actor to report a sensor restart
  case class SensorRestart(sensor: ActorRef)

  // Message sent to the supervisor actor to deploy the airbags
  case object DeployAirbags

  // Base class for sensor actors
  abstract class SensorActor(supervisor: ActorRef) extends Actor {
    // Initialize the measurement to a random value between 0 and 100
    var measurement: Double = Random.nextDouble() * 100

    // Schedule a measurement update every 5 seconds
    context.system.scheduler.scheduleWithFixedDelay(
      initialDelay = 5.seconds,
      delay = 5.seconds,
      receiver = self,
      message = GetMeasurement
    )(context.system.dispatcher)

    // Handle incoming messages
    override def receive: Receive = {
      case GetMeasurement => sender() ! measurement
      case UpdateMeasurement(value) => measurement = value
    }

    // Simulate a sensor crash with a probability of 0.1%
    protected def simulateCrash(): Unit = {
      if (Random.nextDouble() < 0.001) {
        supervisor ! SensorCrash(self)
        throw new RuntimeException("Simulated sensor crash")
      }
    }

    // Simulate a sensor restart
    protected def simulateRestart(): Unit = {
      supervisor ! SensorRestart(self)
    }
  }

  // Cabin sensor actor
  class CabinSensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Wheel sensor actor
  class WheelSensorActor(supervisor: ActorRef) extends SensorActor(supervisor) {
    // Connect to the four wheel sensors
    val wheel1 = context.actorOf(Props(new Wheel1SensorActor(self)))
    val wheel2 = context.actorOf(Props(new Wheel2SensorActor(self)))
    val wheel3 = context.actorOf(Props(new Wheel3SensorActor(self)))
    val wheel4 = context.actorOf(Props(new Wheel4SensorActor(self)))

    // Handle incoming messages
    override def receive: Receive = {
      case GetMeasurement =>
        simulateCrash()
        sender() ! Seq(wheel1, wheel2, wheel3, wheel4).map(_ ! GetMeasurement)
      case UpdateMeasurement(value) =>
        simulateCrash()
        Seq(wheel1, wheel2, wheel3, wheel4).foreach(_ ! UpdateMeasurement(value))
    }
  }

  // Wheel 1 sensor actor
  class Wheel1SensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Wheel 2 sensor actor
  class Wheel2SensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Wheel 3 sensor actor
  class Wheel3SensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Wheel 4 sensor actor
  class Wheel4SensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Motor sensor actor
  class MotorSensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Chassis sensor actor
  class ChassisSensorActor(supervisor: ActorRef) extends SensorActor(supervisor)

  // Supervisor actor
  class SupervisorActor extends Actor {
    // Connect to the sensors
    val cabin = context.actorOf(Props(new CabinSensorActor(self)))
    val wheel = context.actorOf(Props(new WheelSensorActor(self)))
    val motor = context.actorOf(Props(new MotorSensorActor(self)))
    val chassis = context.actorOf(Props(new ChassisSensorActor(self)))

    // Number of crashes since the last restart
    var crashCount = 0

    // Handle incoming messages
    override def receive: Receive = {
      case SensorCrash(sensor) =>
        crashCount += 1
        sensor ! UpdateMeasurement(Random.nextDouble() * 100)
        if (crashCount >= 3) {
          self ! DeployAirbags
          crashCount = 0
        }
      case SensorRestart(sensor) =>
        sensor ! UpdateMeasurement(Random.nextDouble() * 100)
        crashCount = 0
      case DeployAirbags =>
        println("Deploying airbags")
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  val system = ActorSystem("CarSensorSystem")

  system.scheduler.scheduleOnce(10.seconds) {
    // your code here
  }

  // Create the actor system
  //val system = ActorSystem("CarSensorSystem")

  // Create and start the supervisor actor
  val supervisor = system.actorOf(Props[SupervisorActor])

  // Terminate the system after 10 seconds
  system.scheduler.scheduleOnce(10.seconds) {
    system.terminate()
  }
}