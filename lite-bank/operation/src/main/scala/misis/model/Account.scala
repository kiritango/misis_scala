package misis.model

case class Account(id: Int, amount: Int) {
    def update(value: Int) = this.copy(amount = amount + value)
}

trait Command
case class AccountUpdate(accountId: Int, value: Int, transactionId: Int = 0, deliverId: Int = 0)

case class AccountCreate(accountId: Int)

trait Event
case class AccountUpdated(
    accountId: Int,
    value: Int,
    transactionId: Int,
    deliverId: Int
)
case class AccountCreated(accountId: Int)
