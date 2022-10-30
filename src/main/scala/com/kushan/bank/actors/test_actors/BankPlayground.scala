package com.kushan.bank.actors.test_actors

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, Scheduler}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import com.kushan.bank.actors.Bank
import com.kushan.bank.domain.Command.{CreateBankAccount, GetBankAccount}
import com.kushan.bank.domain.Response
import com.kushan.bank.domain.Response.{BankAccountCreatedResponse, GetBankAccountResponse}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

/**
 * @author Ravindu
 *         10/30/2022
 */

/*For this live test, we’re first going to send a creation message and check that there are two events in
Cassandra (one from the bank and one from the account). Running this application should give us the
log successfully created bank account .... You can then shut down the application and run it again,
this time with just the second message. A successful log with the account details proves multiple things:
1 that the bank actor works
2 that the account actor works
3 that the bank account can successfully respawn the account
4 that the account can successfully restore its state
*/

object BankPlayground {
  def main(args: Array[String]): Unit = {
    val rootBehavior: Behavior[NotUsed] = Behaviors.setup { context =>
      val bank = context.spawn(Bank(),"bank")
      val logger = context.log

      val responseHandler = context.spawn(Behaviors.receiveMessage[Response]{
        case BankAccountCreatedResponse(id) =>
          logger.info(s"Successfully created bank account $id")
          Behaviors.same
        case GetBankAccountResponse(maybeBankAccount) =>
          logger.info(s"Account details: $maybeBankAccount")
          Behaviors.same
      },"replyHandler")

      implicit val timeout: Timeout = Timeout(2.seconds)
      implicit val scheduler: Scheduler = context.system.scheduler
      implicit val ec: ExecutionContext = context.executionContext


      // First create the account creation requests and then get the ids of them and comment this request and create get request with releva nt IDS and test those
      // test 1
      //bank ! CreateBankAccount("Ravindu", "USD", 10, responseHandler) //id - dd2b9e69-d1c1-4f26-a798-69da2ce961e6
      //bank ! CreateBankAccount("Kushan", "LKR", 900000, responseHandler)
      //bank ! CreateBankAccount("Sam", "EUR", 2000, responseHandler)
      // test 2
      bank ! GetBankAccount("305316bd-8351-42b7-b4d9-1bb1ba1b5d22", responseHandler)
      bank ! GetBankAccount("d8070717-50c0-40ed-a847-9aaeba86f8be", responseHandler)
      bank ! GetBankAccount("36d09212-3914-4447-8e2b-f48ee71955c3", responseHandler)
      Behaviors.empty
    }

    val system = ActorSystem(rootBehavior,"BankDemo")
  }
}
