package com.ziogram

import com.ziogram.telegram.Telegram
import zio.console._
import zio.{App, Managed, ZIO}

object Main extends App {
  override def run(args: List[String]) =
    Managed.make(Telegram.Live())(_.client.close())
      .use { telegram =>
          program.provideSome[Environment] { env =>
            new Console with Telegram {
              override val console: Console.Service[Any] = env.console
              override val client: Telegram.Service[Any] = telegram.client
            }
          }
      }.fold(_ => 1, _ => 0)

  def program: ZIO[Console with Telegram, Nothing, Int] =
    (
      for {
        messages <- telegram.messages(10L, 0L)
        _        <- putStrLn("Messages: " + messages)
      } yield ()
    ).fold(_ => 1, _ => 0)
}
