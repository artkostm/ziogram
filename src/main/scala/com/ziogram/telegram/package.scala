package com.ziogram

import com.ziogram.telegram.client.TdApi
import zio.{RIO, URIO, ZIO}

package object telegram extends Telegram.Service[Telegram] {
  override def messages(chatId: Long, offset: Long): RIO[Telegram, Iterator[TdApi.Message]] =
    ZIO.accessM(_.client.messages(chatId, offset))

  override def init(): RIO[Telegram, Long] =
    ZIO.accessM(_.client.init())

  override def close(): URIO[Telegram, Unit] =
    ZIO.accessM(_.client.close())
}
