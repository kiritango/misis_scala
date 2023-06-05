package misis.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import misis.WithKafka
import misis.model.{AccountUpdate, AccountUpdated}
import misis.repository.AccountRepository

import scala.concurrent.ExecutionContext

class AccountStreams(repository: AccountRepository)(implicit
    val system: ActorSystem,
    executionContext: ExecutionContext
) extends WithKafka {

    def group = s"account-${repository.accountId}"

    kafkaSource[AccountUpdate]
        .filter(command => repository.account.id == command.accountId && repository.account.amount + command.value >= 0)
        .mapAsync(1) { command =>
            repository
                .update(command.value)
                .map(_ =>
                    AccountUpdated(
                        accountId = command.accountId,
                        value = command.value,
                        transactionId = command.transactionId,
                        deliverId = command.deliverId
                    )
                )
        }
        .to(kafkaSink)
        .run()

    kafkaSource[AccountUpdated]
        .filter(event => repository.account.id == event.accountId)
        .map { e =>
            e.transactionId match {
                case 0 | 1 | 2 =>
                    println(
                        s"Аккаунт ${e.accountId} обновлен на сумму ${e.value}. Баланс: ${repository.account.amount}"
                    )
                case 3 =>
                    println(
                        s"С  ${e.accountId} аккаунта совершён перевод на ${e.deliverId} в размере ${e.value} Баланс: ${repository.account.amount}"
                    )
            }
            e
        }
        .to(Sink.ignore)
        .run()
}
