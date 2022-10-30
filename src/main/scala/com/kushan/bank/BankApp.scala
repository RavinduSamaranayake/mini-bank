package com.kushan.bank

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.util.Timeout
import com.kushan.bank.actors.Bank
import com.kushan.bank.domain.Command
import com.kushan.bank.http_server.BankRouter

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * @author Ravindu
 *         10/30/2022
 */
/*create a standalone application that will spin up an ActorSystem, create the bank actor, and start a new HTTP server based on it.

For the ActorSystem, we need to be able to retrieve the Bank actor from inside of it, so we’ll need to send it a message and expect a response:*/
object BankApp {
  trait RootCommand
  case class RetrieveBankActor(replyTo: ActorRef[ActorRef[Command]]) extends RootCommand

  val rootBehavior: Behavior[RootCommand] = Behaviors.setup{ context =>
    val bankActor = context.spawn(Bank(),"bank")
    Behaviors.receiveMessage {
      case RetrieveBankActor(replyTo) =>
        replyTo ! bankActor
        Behaviors.same
    }
  }

  //Starting the HTTP server based on the bank actor will need some dedicated code as well:
  def startHttpServer(bank: ActorRef[Command])(implicit system: ActorSystem[_]): Unit = {
    implicit val ec: ExecutionContext = system.executionContext
    val router = new BankRouter(bank)
    val routes = router.routes

    // start the server
    val httpBindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

    // manage the server binding
    httpBindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}")
      case Failure(ex) =>
        system.log.error(s"Failed to bind HTTP server, because: $ex")
        system.terminate()
    }
  }

  //And in the main method, we now need to bring all pieces together:
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[RootCommand] = ActorSystem(rootBehavior, "BankSystem")
    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val ec: ExecutionContext = system.executionContext

    // using the ask pattern again
    val bankActorFuture: Future[ActorRef[Command]] = system.ask(replyTo => RetrieveBankActor(replyTo))
    bankActorFuture.foreach(startHttpServer)
  }
}
