name := "ziogram"

version := "1.0"

scalaVersion := "2.12.10"

libraryDependencies += "org.drinkness" % "tdlib" % "1.0" from "file:///Users/arttsiom.chuiko/Desktop/h2/tdlib-1.0_2.12.jar"

libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC13"
libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.0-RC13"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.2.7"

TdLib.buildTdLib := {
  println(sys.props("os.name"))
  println(sys.props("os.arch"))
  try {
    val binaries = TdLib.MacOs.build()
    println("Please find your binaries under " + binaries)
  } catch {
    case t: Throwable => t.printStackTrace()
  }
}

//(run in Compile) <<= (run in Compile).dependsOn(TdLib.buildTdLib)

