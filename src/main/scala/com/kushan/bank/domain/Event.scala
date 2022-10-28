package com.kushan.bank.domain

import com.kushan.bank.domain.model.BankAccount

/**
 * @author Ravindu
 *         10/28/2022
 */
trait Event
object Event {
  case class BankAccountCreated(bankAccount: BankAccount) extends Event
  case class BalanceUpdated(amount: BigDecimal) extends Event
}
