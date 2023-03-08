import SpotifyAPI.executionContext
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}
import akka.util.ByteString
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

import scala.concurrent.duration._
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

object SpotifyAPI {
  implicit val system: ActorSystem = ActorSystem("spotify-api")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val clientId = "YOUR_CLIENT_ID"
  val clientSecret = "YOUR_CLIENT_SECRET"
  val redirectUri = "YOUR_REDIRECT_URI"

  val scope = "playlist-modify-public"

  val authUrl = s"https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&scope=$scope"
  val tokenUrl = "https://accounts.spotify.com/api/token"
  val apiUrl = "https://api.spotify.com/v1"

  var accessToken: Option[String] = None

  def authenticate(): Future[String] = {
    val authRequest = HttpRequest(
      method = HttpMethods.GET,
      uri = authUrl
    )

    for {
      authResponse <- Http().singleRequest(authRequest)
      authCode = authResponse.uri.query().getOrElse("code", "")
      tokenRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = tokenUrl,
        headers = List(
          Authorization(
            BasicHttpCredentials(
              Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(StandardCharsets.UTF_8))
            )
          )
        ),
        entity = FormData(
          "grant_type" -> "authorization_code",
          "code" -> authCode,
          "redirect_uri" -> redirectUri
        ).toEntity
      )
      tokenResponse: HttpResponse <- Http().singleRequest(tokenRequest)
      tokenData <- tokenResponse.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield {
      accessToken = Some(tokenData.utf8String.parseJson.asJsObject.getFields("access_token").head.toString().replaceAll("\"", ""))
      accessToken.get
    }
  }

  def createPlaylist(userId: String, name: String, image: Option[String]): Future[String] = {
    val createPlaylistRequest = HttpRequest(
      method = HttpMethods.POST,
      uri= s"$apiUrl/users/$userId/playlists",
      headers = List(
        Authorization(OAuth2BearerToken(accessToken.get)),
        headers.Content-Type(ContentTypes.application/json)
      ),
      entity = HttpEntity(
        ContentTypes.application/json,
        s"""{
           |  "name": "$name"${
          image.map(img =>
            s""",
               |  "images": [
               |    {
               |      "data_uri": "$img"
               |    }
               |  ]""").getOrElse("")
        }
           |}""".stripMargin
      )
    )

    Http().singleRequest(createPlaylistRequest).flatMap { response =>
      response.status match {
        case StatusCodes.Created =>
          val result = Try(response.entity.withoutSizeLimit().dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String.parseJson.asJsObject.getFields("id").head.toString.replaceAll("\"", "")))
          result match {
            case Success(value) => Future.successful(value).flatMap(identity)
            case Failure(exception) => Future.failed(exception)
          }
        case _ =>
          response.entity.toStrict(5.seconds).map(_.data.utf8String).flatMap { data =>
            Future.failed(new RuntimeException(data))
          }

      }
    }
  }

  def addTracksToPlaylist(playlistId: String, tracks: Seq[String]): Future[Unit] = {
    val trackUris = tracks.map(track => s"spotify:track:$track").mkString(",")
    val addTracksRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = s"$apiUrl/playlists/$playlistId/tracks?uris=$trackUris",
      headers = List(Authorization(OAuth2BearerToken(accessToken.get)))
    )

    Http().singleRequest(addTracksRequest).flatMap { response =>
      response.status match {
        case StatusCodes.OK => Future.successful(())
        case _ =>
          response.entity.withoutSizeLimit().dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)
            .flatMap(data => Future.failed(new RuntimeException(data)))
      }
    }
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    SpotifyAPI.authenticate().onComplete {
      case Success(token) =>
        println(s"Authentication successful, token: $token")

        val userId = "YOUR_USER_ID"
        val playlistName = "My Awesome Playlist"
        val tracks = Seq("TRACK_ID_1", "TRACK_ID_2", "TRACK_ID_3")
        val image = Some("YOUR_PLAYLIST_IMAGE_DATA_URI")

        SpotifyAPI.createPlaylist(userId, playlistName, image).onComplete {
          case Success(playlistId) =>
            println(s"Playlist created, id: $playlistId")

            SpotifyAPI.addTracksToPlaylist(playlistId, tracks).onComplete {
              case Success(_) =>
                println("Tracks added to playlist")
              case Failure(e) =>
                println(s"Error adding tracks to playlist: ${e.getMessage}")
            }
          case Failure(e) =>
            println(s"Error creating playlist: ${e.getMessage}")
        }
      case Failure(e) =>
        println(s"Error authenticating: ${e.getMessage}")
    }
  }
}