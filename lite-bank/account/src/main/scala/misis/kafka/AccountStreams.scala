package misis.kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import misis.WithKafka
import misis.model.{AccountUpdate, AccountUpdated, AccountCreate, AccountCreated}
import misis.repository.AccountRepository

import scala.concurrent.ExecutionContext

class AccountStreams(repository: AccountRepository)(implicit
    val system: ActorSystem,
    executionContext: ExecutionContext
) extends WithKafka {

    def group = s"account"

    kafkaSource[AccountUpdate]
        .filter(command =>
            repository.checkAccount(command.accountId) && repository.getAccount(command.accountId) + command.value >= 0
        )
        .mapAsync(1) { command =>
            repository
                .update(command.accountId, command.value)
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
        .map { e =>
            e.transactionId match {
                case 0 | 1 | 2 =>
                    println(
                        s"Аккаунт ${e.accountId} обновлен на сумму ${e.value}. Баланс: ${repository.getAccount(e.accountId)}"
                    )
                case 3 =>
                    println(
                        s"С  ${e.accountId} аккаунта совершён перевод на ${e.deliverId} в размере ${e.value} Баланс: ${repository
                                .getAccount(e.accountId)}"
                    )
            }
            e
        }
        .to(Sink.ignore)
        .run()
    kafkaSource[AccountCreate]
        .mapAsync(1) { command =>
            repository
                .createAccount(command.accountId)
                .map(_ =>
                    AccountCreated(
                        accountId = command.accountId
                    )
                )
        }
        .to(kafkaSink)
        .run()

    kafkaSource[AccountCreated]
        .map { e =>
            println(
                s"Аккаунт ${e.accountId} cоздан"
            )
            e
        }
        .to(Sink.ignore)
        .run()

}
