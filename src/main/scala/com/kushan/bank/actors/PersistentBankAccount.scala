package com.kushan.bank.actors

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.kushan.bank.domain.{Command, Event}
import com.kushan.bank.domain.Command.{CreateBankAccount, GetBankAccount, UpdateBalance}
import com.kushan.bank.domain.Event.{BalanceUpdated, BankAccountCreated}
import com.kushan.bank.domain.Response.{BankAccountBalanceUpdatedResponse, BankAccountCreatedResponse, GetBankAccountResponse}
import com.kushan.bank.domain.model.BankAccount

import scala.util.{Failure, Success}

/**
 * @author Ravindu
 *         10/27/2022
 */
object PersistentBankAccount {
/*
  Event Sourcing for :
         1.fault tolerance (replay events when system is crash or some thing)
         2.auditing
*/

/*
  under domain,
   commands = messages
   events = to persist to Cassandra
   state
   responses
*/

  /* //Command handlers with normal function
  def commandHandler(state:BankAccount,command:Command)  : Effect[Event, BankAccount] = {
    command match {
      case CreateBankAccount(user, currency, initialBalance, bank) =>
        val id = state.id
        Effect
          .persist(BankAccountCreated(BankAccount(id, user, currency, initialBalance)))
          .thenReply(bank)(_ => BankAccountCreatedResponse(id))

      case UpdateBankAccount(_, _, amount, bank) =>
        val newBalance = state.balance.+(amount)
        if (newBalance.compareTo(BigDecimal(0)) < 0) {
          Effect.reply(bank)(BankAccountBalanceUpdatedResponse(Failure(new RuntimeException("Cannot withdraw more than available"))))
        } else {
          Effect
            .persist(BalanceUpdated(amount))
            .thenReply(bank)(newState => BankAccountBalanceUpdatedResponse(Success(newState)))
        }

      case GetBankAccount(_, bank) =>
        Effect.reply(bank)(GetBankAccountResponse(Some(state)))
    }
  } */


  val commandHandler : (BankAccount,Command) => Effect[Event, BankAccount] = (state,command) => command match {
    case CreateBankAccount(user, currency, initialBalance, bank) =>
      val id = state.id
      Effect
        .persist(BankAccountCreated(BankAccount(id, user, currency, initialBalance)))
        .thenReply(bank)(_ => BankAccountCreatedResponse(id))

    case UpdateBalance(_, _, amount, bank) =>
      val newBalance = state.balance.+(amount)
      if (newBalance.compareTo(BigDecimal(0)) < 0) {
        Effect.reply(bank)(BankAccountBalanceUpdatedResponse(Failure(new RuntimeException("Cannot withdraw more than available"))))
      } else {
        Effect
          .persist(BalanceUpdated(amount))
          .thenReply(bank)(newState => BankAccountBalanceUpdatedResponse(Success(newState)))
      }

    case GetBankAccount(_, bank) =>
      Effect.reply(bank)(GetBankAccountResponse(Some(state)))
  }

  val eventHandler: (BankAccount,Event) => BankAccount = (state,event) => {
    event match {
      case BankAccountCreated(bankAccount) => bankAccount
      case BalanceUpdated(amount) => state.copy(balance = state.balance+amount)
    }
  }

  def apply(id: String) : Behavior[Command] = {
    EventSourcedBehavior[Command,Event,BankAccount](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = BankAccount(id,"","", balance = BigDecimal(0)),//unused
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )
  }

}
