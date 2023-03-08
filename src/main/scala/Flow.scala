import java.io.PrintWriter
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.concurrent.Future
import scala.util.{Failure, Success}
import spray.json._
import DefaultJsonProtocol._

case class Quote2(author: String, text: String, tags: List[String])

object AkkaHttpClientExample3 extends App {
  implicit val system = ActorSystem("akka-http-client-example")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val url = "http://quotes.rest/qod.json"
  val request = HttpRequest(uri = url)

  val flow: Flow[HttpRequest, HttpResponse, Any] = Http().outgoingConnectionHttps("quotes.rest")

  val responseFuture: Future[HttpResponse] = Source.single(request).via(flow).runWith(Sink.head)

  implicit val quoteJsonFormat: JsonFormat[Quote] = jsonFormat3(Quote)
  implicit val listQuoteJsonFormat: RootJsonFormat[List[Quote]] = new RootJsonFormat[List[Quote]] {
    def write(list: List[Quote]): JsValue = JsArray(list.map(_.toJson).toVector)

    def read(value: JsValue): List[Quote] = value match {
      case JsArray(elements) => elements.map(_.convertTo[Quote]).toList
      case _ => throw new DeserializationException("Expected List[Quote]")
    }
  }

  responseFuture onComplete {
    case Success(response) =>
      response.status match {
        case OK =>
          response.entity.dataBytes.runFold("")((acc, curr) => acc + curr.utf8String).foreach { responseBody =>
            val json = responseBody.parseJson
            val quotes = json.asJsObject.fields("contents")
              .asJsObject.fields("quotes")
              .convertTo[List[JsObject]]
              .map { quote =>
                Quote(
                  quote.fields("author").convertTo[String],
                  quote.fields("quote").convertTo[String],
                  quote.fields("tags").convertTo[List[String]]
                )
              }
            println(quotes)

            // convert the list of quotes to JSON and write it to a file
            val quotesJson = quotes.toJson(listQuoteJsonFormat)

            val writer = new PrintWriter("quotes.json")
            writer.write(quotesJson.prettyPrint)
            writer.close()
          }
        case _ =>
          println(s"Request failed with status code ${response.status}")
      }
      println(s"Response headers: ${response.headers}")
    case Failure(ex) =>
      println(s"Request failed with error: ${ex.getMessage}")
  }
}