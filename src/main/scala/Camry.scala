import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}

import scala.util.Random

case class RestartSensor(sensor: ActorRef)

case object DeployAirbags

class Sensor extends Actor with ActorLogging {
  val rand = new Random()

  override def receive: Receive = {
    case "measure" =>
      if (rand.nextInt(10) == 0) {
        // Invalid measurement, restart the sensor
        log.warning(s"${self.path.name}: Invalid measurement")
        context.parent ! RestartSensor(self)
      } else {
        // Valid measurement, send it to the supervisor
        val measurement = rand.nextInt(100)
        log.info(s"${self.path.name}: Measurement = $measurement")
        context.parent ! measurement
      }
    case _ =>
      log.warning(s"${self.path.name}: Unknown message")
  }
}

class WheelSensorSupervisor extends Actor with ActorLogging {
  val wheel1 = context.actorOf(Props[Sensor], "wheel1")
  val wheel2 = context.actorOf(Props[Sensor], "wheel2")
  val wheel3 = context.actorOf(Props[Sensor], "wheel3")
  val wheel4 = context.actorOf(Props[Sensor], "wheel4")

  var validMeasurementsCount = 0

  override def receive: Receive = {
    case "measure" =>
      wheel1 ! "measure"
      wheel2 ! "measure"
      wheel3 ! "measure"
      wheel4 ! "measure"
    case measurement: Int =>
      validMeasurementsCount += 1
      if (validMeasurementsCount == 4) {
        log.info("All wheel sensors have reported valid measurements")
        validMeasurementsCount = 0
      }
    case RestartSensor(sensor) =>
      log.warning(s"${sensor.path.name}: Restarting sensor")
      sensor ! PoisonPill
      context.actorOf(Props[Sensor], sensor.path.name)
    case _ =>
      log.warning(s"${self.path.name}: Unknown message")
  }
}

class CabinSensor extends Actor with ActorLogging {
  override def receive: Receive = {
    case "measure" =>
      val measurement = new Random().nextInt(100)
      log.info(s"${self.path.name}: Measurement = $measurement")
      context.parent ! measurement
    case RestartSensor(sensor) =>
      log.warning(s"${sensor.path.name}: Restarting sensor")
      sensor ! PoisonPill
      context.actorOf(Props[Sensor], sensor.path.name)
    case _ =>
      log.warning(s"${self.path.name}: Unknown message")
  }
}

class MotorSensor extends Actor with ActorLogging {
  override def receive: Receive = {
    case "measure" =>
      val measurement = new Random().nextInt(100)
      log.info(s"${self.path.name}: Measurement = $measurement")
      context.parent ! measurement
    case RestartSensor(sensor) =>
      log.warning(s"${sensor.path.name}: Restarting sensor")
      sensor ! PoisonPill
      context.actorOf(Props[Sensor], sensor.path.name)
    case _ =>
      log.warning(s"${self.path.name}: Unknown message")
  }
}

class ChassisSensor extends Actor with ActorLogging {
  override def receive: Receive = {
    case "measure" =>
      val measurement = new Random().nextInt(100)
      log.info(s"${self.path.name}: Measurement = $measurement")
      context.parent ! measurement
    case RestartSensor(sensor) =>
      log.warning(s"${sensor.path.name}: Restarting sensor")
      sensor ! PoisonPill
      context.actorOf(Props[Sensor], sensor.path.name)
    case _ =>
      log.warning(s"${self.path.name}: Unknown message")
  }
}

class MainSensorSupervisor extends Actor with ActorLogging {
  val wheelSensorSupervisor = context.actorOf(Props[WheelSensorSupervisor], "wheelSensorSupervisor")
  val cabinSensor = context.actorOf(Props[CabinSensor], "cabinSensor")
  val motorSensor = context.actorOf(Props[MotorSensor], "motorSensor")
  val chassisSensor = context.actorOf(Props[ChassisSensor], "chassisSensor")

  var crashesCount = 0

  override def receive: Receive = {
    case "measure" =>
      wheelSensorSupervisor ! "measure"
      cabinSensor ! "measure"
      motorSensor ! "measure"
      chassisSensor ! "measure"
    case RestartSensor(sensor) =>
      log.warning(s"${sensor.path.name}: Restarting sensor")
      sensor ! PoisonPill
      context.actorOf(Props[Sensor], sensor.path.name)
    case _: Int =>
    // Valid measurement, do nothing
    case _ =>
      log.warning(s"${self.path.name}: Unknown message")
  }
}

object SensorSystemExample extends App {
  val system = ActorSystem("SensorSystem")
  val supervisor = system.actorOf(Props[MainSensorSupervisor], "mainSensorSupervisor")
  supervisor ! "measure"
}