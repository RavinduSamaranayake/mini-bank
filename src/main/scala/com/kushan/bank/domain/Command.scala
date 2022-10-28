package com.kushan.bank.domain

import akka.actor.typed.ActorRef

/**
 * @author Ravindu
 *         10/28/2022
 */
sealed trait Command
object Command {
  case class CreateBankAccount(user: String, currency: String, initialBalance: BigDecimal, replyTo: ActorRef[Response]) extends Command
  case class UpdateBalance(id: String, currency: String, amount: BigDecimal /*can be < 0*/ , replyTo : ActorRef[Response]) extends Command
  case class GetBankAccount(id: String, replyTo: ActorRef[Response]) extends Command
}
