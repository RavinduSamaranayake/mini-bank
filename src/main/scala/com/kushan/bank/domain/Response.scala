package com.kushan.bank.domain

import com.kushan.bank.domain.model.BankAccount

import scala.util.Try

/**
 * @author Ravindu
 *         10/28/2022
 */
sealed trait Response
object Response {
  case class BankAccountCreatedResponse(id: String) extends Response
  case class BankAccountBalanceUpdatedResponse(maybeBankAccount: Try[BankAccount]) extends Response
  case class GetBankAccountResponse(maybeBankAccount: Option[BankAccount]) extends Response
}
