package com.kushan.bank.domain.http

import akka.actor.typed.ActorRef
import com.kushan.bank.domain.Command.{CreateBankAccount, UpdateBalance}
import com.kushan.bank.domain.{Command, Response}

/**
 * @author Ravindu
 *         10/29/2022
 */
object Request {
  case class BankAccountCreationRequest(user: String, currency: String, balance: Double){
    def toCommand(replyTo: ActorRef[Response]): Command = CreateBankAccount(user,currency,balance,replyTo)
  }
  case class BankAccountUpdateRequest(currency: String, amount: Double){
    def toCommand(id:String, replyTo: ActorRef[Response]): Command = UpdateBalance(id,currency, amount, replyTo)
  }
}
