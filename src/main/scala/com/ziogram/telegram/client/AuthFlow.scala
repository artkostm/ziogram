package com.ziogram.telegram.client

import com.ziogram.telegram.client.Auth.{getCode, getPhone}
import com.ziogram.telegram.client.TdApi._
import zio.Exit.{Failure, Success}
import zio.{App, IO, Managed, Queue, Runtime, Schedule, UIO, ZIO}
import zio.console._

import scala.io.StdIn

trait AuthFlow {}

object Auth {

//  def events(client: Client): IO[Throwable, Queue[Object]] =
//    Queue.unbounded[Object].tap { queue =>
//      for {
//        obj <- queue.take
//        _ <- ZIO.whenCase(obj.getConstructor) {
//              case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR =>
//                val parameters = new TdlibParameters()
//                parameters.databaseDirectory = "db"
//                parameters.useMessageDatabase = false
//                parameters.useSecretChats = true
//                parameters.apiId = 94575
//                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
//                parameters.systemLanguageCode = "en"
//                parameters.deviceModel = "Desktop"
//                parameters.systemVersion = "Unknown";
//                parameters.applicationVersion = "1.0"
//                parameters.enableStorageOptimizer = true
//
//                IO.effectAsync { cb =>
//                  client.send(new SetTdlibParameters(parameters),
//                              obj => cb(ZIO.succeed(obj)),
//                              e => cb(ZIO.fail(e)))
//                }
//              case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR =>
//                IO.effectAsync { cb =>
//                  client.send(new CheckDatabaseEncryptionKey(),
//                              obj => cb(ZIO.succeed(obj)),
//                              e => cb(ZIO.fail(e)))
//                }
//              case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR =>
//                getPhone().provide(null)
//              case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR =>
//                getCode().provide(null)
//            }
//      } yield ()
//    }

//  private def authEventsWith(queue: Queue[Object], client: Client) =
//    for {
//    rts <- ZIO.runtime
//    response <- ZIO.effectAsync { cb => client.send()}
//    }

  def getPhone(): ZIO[Client, Throwable, Object] = //: Task[Throwable, Object] =
  for {
    client      <- ZIO.environment[Client]
    _           <- UIO.effectTotal(println("Please enter phone number: "))
    phoneNumber <- IO.effect(StdIn.readLine())
    response <- IO.effectAsync[Throwable, Object] { cb =>
                 client.send(new SetAuthenticationPhoneNumber(phoneNumber, null),
                             obj => cb(ZIO.succeed(obj)),
                             e => cb(ZIO.fail(e)))
               }
  } yield response

  def getCode(): ZIO[Client, Throwable, Object] = //: Task[Throwable, Object] =
  for {
    client <- ZIO.environment[Client]
    _      <- UIO.effectTotal(println("Please enter authentication code: "))
    code   <- IO.effect(StdIn.readLine())
    response <- IO.effectAsync[Throwable, Object] { cb =>
                 client.send(new CheckAuthenticationCode(code),
                             obj => cb(ZIO.succeed(obj)),
                             e => cb(ZIO.fail(e)))
               }
  } yield response

  def apply(rts: Runtime[Any], queue: Queue[Object]): UIO[Client] = UIO.effectTotal {
    Client.create({ obj =>
      rts.unsafeRunAsync(queue.offer(obj)) {
        case Success(_)     => ()
        case Failure(cause) => ()
      }
    }, null, null)
  }
}

object testa extends App {
  System.loadLibrary("tdjni")

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    (for {
      queue  <- Managed.make(Queue.unbounded[Object])(_.shutdown)
      client <- Managed.make(Auth(testa, queue))(c => UIO.effectTotal(c.close()))
    } yield (client, queue)).use {
      case (client, queue) =>
        (for {
          update <- queue.take //.repeat(Schedule.doUntil(o => o.isInstanceOf[AuthorizationStateClosed] || o.isInstanceOf[AuthorizationStateReady]))
          _      <- UIO.effectTotal(println("Got: " + update))
          _ <- ZIO.whenCase(update.getConstructor) {
                case UpdateAuthorizationState.CONSTRUCTOR =>
                  update
                    .asInstanceOf[UpdateAuthorizationState]
                    .authorizationState
                    .getConstructor match {
                    case AuthorizationStateWaitTdlibParameters.CONSTRUCTOR =>
                      val parameters = new TdlibParameters()
                      parameters.databaseDirectory = "db"
                      parameters.useMessageDatabase = false
                      parameters.useSecretChats = true
                      parameters.apiId = 94575
                      parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2"
                      parameters.systemLanguageCode = "en"
                      parameters.deviceModel = "Desktop"
                      parameters.systemVersion = "Unknown";
                      parameters.applicationVersion = "1.0"
                      parameters.enableStorageOptimizer = true

                      IO.effectAsync[Throwable, Object] { cb =>
                        client.send(new SetTdlibParameters(parameters),
                                    obj => cb(ZIO.succeed(obj)),
                                    e => cb(ZIO.fail(e)))
                      }
                    case AuthorizationStateWaitEncryptionKey.CONSTRUCTOR =>
                      IO.effectAsync[Throwable, Object] { cb =>
                        client.send(new CheckDatabaseEncryptionKey(),
                                    obj => cb(ZIO.succeed(obj)),
                                    e => cb(ZIO.fail(e)))
                      }
                    case AuthorizationStateWaitPhoneNumber.CONSTRUCTOR =>
                      getPhone().provide(client)
                    case AuthorizationStateWaitCode.CONSTRUCTOR =>
                      getCode().provide(client)
                  }
                case _ => UIO.unit
              }
        } yield update).repeat(
          Schedule.doUntil(
            o => o.isInstanceOf[AuthorizationStateClosed] || o.isInstanceOf[AuthorizationStateReady]
          )
        )
    }.fold(_ => 1, _ => 0)
}
