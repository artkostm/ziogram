package com.ziogram.telegram

import com.ziogram.telegram.client.Client
import org.drinkless.tdlib.TdApi
import zio.{RIO, Task, UIO, URIO, ZIO}

trait Telegram {
  val client: Telegram.Service[Any]
}

object Telegram {

  trait Service[R] {
    def messages(chatId: Long, offset: Long): RIO[R, Iterator[TdApi.Message]]
    def init(): RIO[R, Long]
    def close(): URIO[R, Unit]
  }

  trait Live extends Telegram {
    protected[Telegram] val inner: Client

    override val client: Service[Any] = new Service[Any] {

      override def init(): Task[Long] =
        ZIO.effect(inner.init())

      override def messages(chatId: Long, offset: Long): Task[Iterator[TdApi.Message]] =
        ZIO.effectAsync { cb =>
          inner.send(new TdApi.GetChatHistory(),
                     res => cb(ZIO.succeed(res.asInstanceOf[Iterator[TdApi.Message]])),
                     error => cb(ZIO.fail(error)))
        }

      override def close(): UIO[Unit] =
        ZIO.effectTotal(inner.close())
    }
  }

  object Live {

    def apply(): Task[Telegram] =
      ZIO.effect(new Live {
        override protected[Telegram] val inner: Client = Client()
      })
  }
}
