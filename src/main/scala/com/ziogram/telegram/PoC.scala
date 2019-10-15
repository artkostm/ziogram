package com.ziogram.telegram

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import com.ziogram.telegram.TdApi.GetSomething
import zio._
import zio.clock.Clock
import zio.duration.Duration

object PoC extends App {
  override def run(args: List[String]): ZIO[PoC.Environment, Nothing, Int] = Client
    .create()
    .use { client =>
      for {
        _     <- console.putStrLn("starting the client")
        resp  <- client.request(new GetSomething)
        _     <- console.putStrLn("response is " + resp)
      } yield ()
    }
    .fold(_ => 1, _ => 0)
}

object TdApi {

  trait Object {
    def getConstructor(): Int
  }

  class Function extends Object {
    override def getConstructor(): Int = 1
  }

  class GetSomething extends Function {
    override def getConstructor(): Int = 2
  }

  class SomeResponse(msg: String) extends Object {
    override def getConstructor(): Int = 3
  }

}

case class Client(clientId: Ref[Long],
                  susb: Ref[Map[Long, Promise[Throwable, TdApi.Object]]],
                  updatesQueue: Queue[TdApi.Object],
                  receiveQueue: Queue[TdApi.Object]) {
  private val queryIdGenerator = new AtomicLong()

  def request(func: TdApi.Function): ZIO[Any, Throwable, TdApi.Object] = for {
    p           <- Promise.make[Throwable, TdApi.Object]
    nextEventId = queryIdGenerator.incrementAndGet()
    _           <- susb.update(_ + (nextEventId -> p))
    id          <- clientId.get
    _           <- Client.send(id, nextEventId, func)
    response    <- p.await
  } yield response
}

object Client { //  def apply(): Managed[Throwable, Client] = //    for { //      rts          <- Managed.fromEffect(ZIO.runtime[Any]) //      updatesQueue <- Managed.make(Queue.dropping[TdApi.Object](100))(_.shutdown) //      client <- Managed.make( //                 ZIO.effect(new Client(r => rts.unsafeRunAsync_(updatesQueue.offer(r)))) //               )(c => UIO(c.close())) //    } yield client  
  def send(clientId: Long, eventId: Long, func: TdApi.Function): ZIO[Any, Throwable, Unit] =
    ZIO.effect(nativeClientSend(clientId, eventId, func))

  def nativeClientSend(clientId: Long, eventId: Long, func: TdApi.Function): Unit = println(
    s"""                |call nativeClientSend($clientId, $eventId, $func) from ${Thread
         .currentThread()
         .getName}                |""".stripMargin
  )

  def create(): ZManaged[Any with Clock, Throwable, Client] = (for {
    clientId    <- ZIO.effect(nativeClientCreate()).toManaged_
    clientIdRef <- Ref.make(clientId).toManaged_
    subs        <- Ref.make(Map.empty[Long, Promise[Throwable, TdApi.Object]]).toManaged_
    updates     <- Queue.unbounded[TdApi.Object].toManaged(_.shutdown)
    receive     <- Queue.unbounded[TdApi.Object].toManaged(_.shutdown)
    _           <- receiveLoop(clientId, subs, updates)
  } yield Client(clientIdRef, subs, updates, receive)).onExitFirst {
    case Exit.Success(client) => client.clientId.get.flatMap(destroy)
    case Exit.Failure(_)      => ZIO.unit
  }

  def nativeClientCreate(): Long = 1

  def destroy(clientId: Long): UIO[Unit] = UIO.effectTotal(nativeClientDestroy(clientId))

  def nativeClientDestroy(clientId: Long): Unit = println(
    s"called destroy [$clientId] from ${Thread.currentThread().getName}"
  )

  def receiveLoop(clientId: Long,
                  subs: Ref[Map[Long, Promise[Throwable, TdApi.Object]]],
                  updatesQueue: Queue[TdApi.Object]) = {
    def receive(): Array[(Long, TdApi.Object)] = {
      val ids    = Array.ofDim[Long](100)
      val events = Array.ofDim[TdApi.Object](100)
      val count  = nativeClientReceive(clientId, ids, events, 300D).toInt
      ids.take(count).zip(events.take(count))
    }

    for {
      received <- ZIO.effect(receive())
      _ <- ZIO.whenCase(received.filter(_._1 == 0).map(_._2)) {
            case updates => updatesQueue.offerAll(updates)
          }.fork
      _ <- ZIO.whenCase(received.filterNot(_._1 == 0)) {
            case responses =>
              for {
                subscribers <- subs.get
                _ <- ZIO.collectAll(responses.map {
                      case (id, response) =>
                        println(
                          s"""
id = $id, response = $response, subs = $subscribers
                            |""".stripMargin)
                        subscribers.get(id).map(_.succeed(response)).getOrElse(ZIO.unit)
                    })
              } yield ()
          }
    } yield ()
  }.repeat(Schedule.spaced(Duration.apply(1000, TimeUnit.MILLISECONDS))).fork.toManaged_

  def nativeClientReceive(clientId: Long,
                          eventIds: Array[Long],
                          events: Array[TdApi.Object],
                          timeout: Double): Long = {
    val ids = Array(0L, 1L)
    val e = Array(new TdApi.SomeResponse("response for event id 1"),
                  new TdApi.SomeResponse("response for event id 2"))
    ids.copyToArray(eventIds, 0)
    e.copyToArray(events, 0)
    Thread.sleep(100)
    println(
      s"""                |call nativeClientReceive($clientId, ${eventIds.toSeq
           .take(3)}, ${events.toSeq.take(3)}, $timeout) from ${Thread
           .currentThread()
           .getName}                |""".stripMargin
    )
    2
  }
}
