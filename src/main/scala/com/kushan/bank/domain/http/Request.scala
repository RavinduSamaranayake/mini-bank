package com.kushan.bank.domain.http

import akka.actor.typed.ActorRef
import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal}
import com.kushan.bank.domain.Command.{CreateBankAccount, UpdateBalance}
import com.kushan.bank.domain.{Command, Response}
import com.kushan.bank.http_server.Validation.{ValidationResult, Validator, validateMinimum, validateMinimumAbs, validateRequired}

/**
 * @author Ravindu
 *         10/29/2022
 */
object Request {

  //with validations
  object BankAccountCreationRequest {
    implicit val validator: Validator[BankAccountCreationRequest] = (request: BankAccountCreationRequest) => {
      val userValidation = validateRequired(request.user, "user")
      val currencyValidation = validateRequired(request.currency, "currency")
      val balanceValidation = validateMinimum(request.balance, 0, "balance")
        .combine(validateMinimumAbs(request.balance, 0.01, "balance"))

      (userValidation, currencyValidation, balanceValidation).mapN(BankAccountCreationRequest.apply)
    }
  }

  object BankAccountUpdateRequest {
    implicit val validator: Validator[BankAccountUpdateRequest] = (request: BankAccountUpdateRequest) => {
      val currencyValidation = validateRequired(request.currency, "currency")
      val amountValidation = validateMinimumAbs(request.amount, 0.01, "amount")

      (currencyValidation, amountValidation).mapN(BankAccountUpdateRequest.apply)
    }
  }

  case class BankAccountCreationRequest(user: String, currency: String, balance: Double){
    def toCommand(replyTo: ActorRef[Response]): Command = CreateBankAccount(user,currency,balance,replyTo)
  }
  case class BankAccountUpdateRequest(currency: String, amount: Double){
    def toCommand(id:String, replyTo: ActorRef[Response]): Command = UpdateBalance(id,currency, amount, replyTo)
  }
}
