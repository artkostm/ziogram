package test.prisest

import org.drinkless.tdlib.TdApi.TdlibParameters
import org.drinkless.tdlib.example.Example

object Test extends App {

  Example.main(Array("Example"))
  println("Libs loaded!")

  new TdlibParameters()

}

//final case class ClientParams(
//  useTestDc: Boolean,
//  databaseDirectory: String = "tdlib",
//  filesDirectory: String = null,
//  var useFileDatabase: Boolean = false,
//  useChatInfoDatabase: Boolean = false,
//  useMessageDatabase: Boolean = false,
//  useSecretChats: Boolean = false,
//  apiId: Int = 0,
//  apiHash: String,
//  systemLanguageCode: String = "en-US",
//  deviceModel: String = "Desktop",
//  systemVersion: String = "1.0",
//  applicationVersion: String = "1.0",
//  enableStorageOptimizer: Boolean = false,
//  ignoreFileNames: Boolean = true
//) extends TdlibParameters
