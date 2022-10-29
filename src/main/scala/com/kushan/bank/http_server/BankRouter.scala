package com.kushan.bank.http_server

// at the top
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.kushan.bank.domain.Command.GetBankAccount
import com.kushan.bank.domain.Response.{BankAccountBalanceUpdatedResponse, BankAccountCreatedResponse, GetBankAccountResponse}
import com.kushan.bank.domain.{Command, Response}
import com.kushan.bank.domain.http.Request.{BankAccountCreationRequest, BankAccountUpdateRequest}
import com.kushan.bank.domain.http.Response.FailureResponse

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * @author Ravindu
 *         10/29/2022
 */
class BankRouter(bank: ActorRef[Command])(implicit system: ActorSystem[_]) {

  implicit val timeout : Timeout = Timeout(5.seconds)

  def createBankAccount(request: BankAccountCreationRequest) : Future[Response] =
    bank.ask(replyTo => request.toCommand(replyTo))

  def getBankAccount(id: String): Future[Response] = bank.ask(replyTo => GetBankAccount(id, replyTo))

  def updateBankAccount(id : String, request: BankAccountUpdateRequest) : Future[Response] =
    bank.ask(replyTo => request.toCommand(id,replyTo))


  val routes: Route =
    pathPrefix("bank") {
      pathEndOrSingleSlash {
    /*   1 fetch the bank actor
         2 send it a CreateBankAccount command — note that it’s different from the HTTP request
         3 parse its reply
         4 send back an HTTP response */
        post {
          // parse the payload
          entity(as[BankAccountCreationRequest]) { request =>
            onSuccess(createBankAccount(request)) {
              // send back an HTTP response
              case BankAccountCreatedResponse(id) =>
                respondWithHeader(Location(s"/bank/$id")) {
                  complete(StatusCodes.Created)
                }
            }
          }
        }
      } ~ //<-- careful with this one
/*       1 send a command to the bank actor to retrieve the details
         2 parse the response
         3 send back an HTTP response*/
        path(Segment) { id =>
          get {
            onSuccess(getBankAccount(id)){
              case GetBankAccountResponse(Some(account)) =>
                complete(account)
              case GetBankAccountResponse(None) =>
                complete(StatusCodes.NotFound, FailureResponse(s"Bank account $id cannot be found."))
            }
          }~
          /*      1 Ask the bank actor to update the bank account.
                  2 Expect a reply.
                  3 Send back an HTTP response.*/
          put {
            entity(as[BankAccountUpdateRequest]) { request =>
              onSuccess(updateBankAccount(id, request)) {
                case BankAccountBalanceUpdatedResponse(Success(account)) =>
                  complete(account)
                case BankAccountBalanceUpdatedResponse(Failure(ex)) =>
                  complete(StatusCodes.BadRequest, FailureResponse(s"${ex.getMessage}"))
              }
            }
          }
    }
}
