package misis.repository


import io.circe.generic.auto._
import misis.TopicName
import misis.kafka.OperationStreams
import misis.model.{AccountUpdate,TransferStart}
import akka.actor.ActorSystem
import scala.concurrent.{ExecutionContext}


class Repository(streams: OperationStreams)(implicit val system: ActorSystem, executionContext: ExecutionContext) {
    implicit val commandTopicName: TopicName[AccountUpdate] = streams.simpleTopicName[AccountUpdate]

    def transfer(transfer: TransferStart) = {
        if (transfer.value > 0) {
            streams.produceCommand(AccountUpdate(transfer.sourceId, -transfer.value, 1, transfer.destinationId))
        }
    }
}