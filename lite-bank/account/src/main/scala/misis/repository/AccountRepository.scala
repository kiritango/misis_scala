package misis.repository

import misis.model.Account
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer

class AccountRepository() {

    private val accounts: ListBuffer[Account] = ListBuffer.empty[Account]

    def createAccount(accountId: Int): Future[Account] = {
        val account = Account(accountId, 0)
        accounts += account
        Future.successful(account)
    }
    def checkAccount(accountId: Int): Boolean = {
        accounts.exists(_.id == accountId)
    }
    def getAccount(accountId: Int): Int = {
        accounts.find(_.id == accountId).map(_.amount).getOrElse(0)
    }
    def update(accountId: Int, value: Int): Future[Account] = {
        val account = accounts.find(_.id == accountId)
        account match {
            case Some(account) =>
                val newAccount = account.update(value)
                accounts -= account
                accounts += newAccount
                Future.successful(newAccount)
            case None =>
                throw new IllegalArgumentException(s"Аккаунт не найден")
        }
    }
}
