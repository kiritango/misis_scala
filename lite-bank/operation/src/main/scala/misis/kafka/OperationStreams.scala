package misis.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import misis.{TopicName, WithKafka}
import misis.model.{AccountUpdate, AccountUpdated}
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext

class OperationStreams()(implicit val system: ActorSystem, executionContext: ExecutionContext) extends WithKafka {
    override def group: String = "operation"
    implicit val commandTopicName: TopicName[AccountUpdated] = simpleTopicName[AccountUpdated]

    kafkaSource[AccountUpdated]
        .filter(event => event.transactionId == 1 || event.transactionId == 2)
        .map { command =>
            command.transactionId match {
                case (1) =>
                    produceCommand(AccountUpdate(command.deliverId, -command.value, 2, command.accountId))
                case (2) =>
                    produceCommand(
                        AccountUpdated(
                            accountId = command.deliverId,
                            value = command.value,
                            transactionId = 3,
                            deliverId = command.accountId
                        )
                    )
                    println(
                        s"С ${command.deliverId} аккаунту переведено ${command.value} на ${command.accountId} аккаунт"
                    )
            }

        }
        .to(Sink.ignore)
        .run()
}
