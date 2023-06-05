package misis.model



case class Account(id: Int, amount: Int) {
    def update(value: Int) = this.copy(amount = amount + value)
}

trait Command
case class AccountUpdate(accountId: Int, value: Int,transactionId: Int = 0, deliverId: Int = 0)

trait Event
case class AccountUpdated(
                             accountId: Int,
                             value: Int,
                             transactionId: Int,
                             deliverId: Int,
                         )
