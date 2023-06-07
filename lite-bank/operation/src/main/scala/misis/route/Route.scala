package misis.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import misis.TopicName
import misis.kafka.OperationStreams
import misis.model.{AccountUpdate, TransferStart, AccountCreate}
import misis.repository.Repository
import java.util.UUID

import scala.concurrent.ExecutionContext

class Route(streams: OperationStreams, repository: Repository)(implicit ec: ExecutionContext)
    extends FailFastCirceSupport {

    implicit val commandTopicName: TopicName[AccountUpdate] = streams.simpleTopicName[AccountUpdate]
    implicit val createTopicName: TopicName[AccountCreate] = streams.simpleTopicName[AccountCreate]

    def routes =
        (path("hello") & get) {
            complete("ok")
        } ~
            (path("update" / IntNumber / IntNumber) { (accountId, value) =>
                val command = AccountUpdate(accountId, value)
                streams.produceCommand(command)
                complete(command)
            }) ~
            (path("transfer") & post & entity(as[TransferStart])) { transfer =>
                repository.transfer(transfer)
                complete(transfer)
            } ~
            (path("createacc" / IntNumber) { (accountId) =>
                val command = AccountCreate(accountId)
                streams.produceCommand(command)
                complete(command)
            })
}
