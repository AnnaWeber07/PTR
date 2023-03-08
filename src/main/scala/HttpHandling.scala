import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.stream.ActorMaterializer
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration.DurationInt

//1
object AkkaHttpClientExample extends App {
  implicit val system = ActorSystem("akka-http-client-example")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val url = "https://www.example.com"
  val request = HttpRequest(uri = url)

  val responseFuture: Future[HttpResponse] = Http().singleRequest(request)

  responseFuture onComplete {
    case Success(response) =>
      response.status.isSuccess() match {
        case true =>
          response.entity.toStrict(1.second).map(_.data.utf8String).foreach { body =>
            println(s"Response body: $body")
          }
        case false =>
          println(s"Request failed with status code ${response.status}")
      }
      println(s"Response headers: ${response.headers}")
    case Failure(ex) =>
      println(s"Request failed with error: ${ex.getMessage}")
  }

  // Shutdown the system after a delay
  system.scheduler.scheduleOnce(5.seconds) {
    system.terminate()
  }
}