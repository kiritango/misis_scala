package misis

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import misis.kafka.OperationStreams
import misis.repository.Repository
import misis.route.Route

object OperationApp extends App {
    implicit val system: ActorSystem = ActorSystem("OperationApp")
    implicit val ec = system.dispatcher
    val port = ConfigFactory.load().getInt("port")

    private val streams = new OperationStreams()
    private val repository = new Repository(streams)

    private val route = new Route(streams, repository)
    Http().newServerAt("0.0.0.0", port).bind(route.routes)
}
