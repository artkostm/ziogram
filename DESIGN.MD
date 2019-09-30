1. Model is just like akka actor's receiver

<pre>
                                                             maybe just Messages or instead of ZIO return Managed[Error, Queue[of something]]
                                                                    |
def messages(chatId: Long, offset: Long): ZIO[Telegram, Error(?), Queue[Messages] (?)] = 
	ZIO.accessM(_.client.send[Messages](new GetChatHistory(chatId, offset))
	                           |                   |
							Response type        Request
</pre>

```scala
for {
	_ <- putStrLn("Messages:")
	_ <- telegram.messages(1, 10).use { messagesQueue =>
		for {
			msgs <- messagesQueue.take
			_    <- putStrLn(msgs)
		} yield ()
	}
} yield ()
```
