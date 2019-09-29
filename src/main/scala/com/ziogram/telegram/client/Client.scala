package org.chatanalysis.telegram.client

import java.util.concurrent.atomic.AtomicLong

import org.drinkless.tdlib.TdApi

protected[telegram] trait Client {

  def send(query: TdApi.Function,
           resultHandler: TdApi.Object => Unit,
           exceptionHandler: Throwable => Unit): Unit
  def execute(query: TdApi.Function): TdApi.Object
  def close(): Unit
  def init(): Long
}

protected[telegram] object Client {
  private lazy val client: Client = create()
  private val clientId = new AtomicLong()

  @native protected[telegram] def createNativeClient(): Long

  @native protected[telegram] def nativeClientSend(nativeClientId: Long,
                                                   eventId: Long,
                                                   function: TdApi.Function): Unit

  @native protected[telegram] def nativeClientReceive(nativeClientId: Int,
                                                      eventIds: Array[Long],
                                                      events: Array[TdApi.Object],
                                                      timeout: Double): Int
  @native protected[telegram] def nativeClientExecute(function: TdApi.Function): TdApi.Object
  @native protected[telegram] def destroyNativeClient(nativeClientId: Long): Unit

  private def create(): Client = new Client {
    override def send(query: TdApi.Function,
                      resultHandler: TdApi.Object => Unit,
                      exceptionHandler: Throwable => Unit): Unit = ???
    override def execute(query: TdApi.Function): TdApi.Object    = ???

    override def close(): Unit =
      try {
        destroyNativeClient(clientId.get())
      } catch {
        case e: Throwable => e.printStackTrace() // TODO: fix this
      }

    override def init(): Long = createNativeClient()
  }

  def apply(): Client = synchronized {
    if (clientId.get() == 0L) clientId.getAndSet(createNativeClient())
    client
  }

  def main(args: Array[String]): Unit = {
    var clientId = 0L
    try {
      clientId = createNativeClient()
      println("client created with id " + clientId)
    } catch {
      case e: Throwable => e.printStackTrace()
    } finally {
      destroyNativeClient(clientId)
      println("client destroyed")
    }
  }
}
