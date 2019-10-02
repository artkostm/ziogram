package com.ziogram.telegram.client

import java.util.concurrent.atomic.AtomicLong

import com.ziogram.telegram.client
import zio.{App, UIO, ZIO}
import com.ziogram.telegram.client.{Client, TdApi}

protected[telegram] trait TelegramClient {

  def send(query: TdApi.Function,
           resultHandler: TdApi.Object => Unit,
           exceptionHandler: Throwable => Unit): Unit
  def execute(query: TdApi.Function): TdApi.Object
  def close(): Unit
  def init(): Long
}

protected[telegram] object TelegramClient extends App {
  private lazy val client: TelegramClient = create()
  private val clientId                    = new AtomicLong()

  private def create(): TelegramClient = new TelegramClient {
    override def send(query: TdApi.Function,
                      resultHandler: TdApi.Object => Unit,
                      exceptionHandler: Throwable => Unit): Unit = ???
    override def execute(query: TdApi.Function): TdApi.Object    = ???

    override def close(): Unit =
      try {
        Client.destroyNativeClient(clientId.get())
      } catch {
        case e: Throwable => e.printStackTrace() // TODO: fix this
      }

    override def init(): Long = Client.createNativeClient()
  }

  def apply(): TelegramClient = synchronized {
    if (clientId.get() == 0L) clientId.getAndSet(Client.createNativeClient())
    client
  }

//  def main(args: Array[String]): Unit = {
//    System.loadLibrary("tdjni")
//    var clientId = 0L
//    try {
//      clientId = Client.createNativeClient()
//      println("client created with id " + clientId)
//    } catch {
//      case e: Throwable => e.printStackTrace()
//    } finally {
//      Client.destroyNativeClient(clientId)
//      println("client destroyed")
//    }
//  }
  override def run(args: List[String]): ZIO[TelegramClient.Environment, Nothing, Int] = {
    System.loadLibrary("tdjni")
    val clientId = Client.createNativeClient()
    (for {
      fiber <- ZIO.effect {
        val ids = Array.ofDim[Long](10)
        val events = Array.ofDim[TdApi.Object](10)
        val count = Client.nativeClientReceive(clientId, ids, events, 300)
        ids.take(count).zip(events.take(count))
      }.tap(logger).forever.fork
      _ <- fiber.join
    } yield ()).fold(_ => 1, _ => 0)
  }

  def logger(events: Array[(Long, TdApi.Object)]): UIO[Unit] = UIO.effectTotal(events.foreach(println))
}
