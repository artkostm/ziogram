import sbt._
import os._

object TdLib {

  lazy val buildTdLib = taskKey[Unit]("Build tdlib native library and java wrapper")

  case object MacOs extends Builder {
    override def build(): Path = {

      remove.all(FS.Build)
      makeDir(FS.Build)

      proc(
        "cmake",
        "-DCMAKE_BUILD_TYPE=Release",
        "-DOPENSSL_ROOT_DIR=/usr/local/opt/openssl/",
        "-DCMAKE_INSTALL_PREFIX:PATH=../example/java/td",
        "-DTD_ENABLE_JNI=ON",
        ".."
      ).call(cwd = FS.Build, stdout = Inherit, stderr = Inherit)

      proc("cmake", "--build", ".", "--target", "install")
        .call(cwd = FS.Build, stdout = Inherit, stderr = Inherit)

      remove.all(FS.ExampleBuild)
      makeDir(FS.ExampleBuild)

      proc(
        "cmake",
        "-DCMAKE_BUILD_TYPE=Release",
        "-DCMAKE_INSTALL_PREFIX:PATH=../../../tdlib",
        "-DTd_DIR:PATH=" + (FS.Example / "td" / "lib" / "cmake" / "Td").toString(),
        ".."
      ).call(cwd = FS.ExampleBuild, stdout = Inherit, stderr = Inherit)


      proc("cmake", "--build", ".", "--target", "install")
        .call(cwd = FS.ExampleBuild, stdout = Inherit, stderr = Inherit)

      FS.TdLibBin
    }
  }

  case object Windows extends Builder {
    override def build(): Path = {
      println("Sorry, will be implemented soon!")
      FS.ExampleBuild
    }
  }

  object FS {
    val Root         = pwd / "tdlib"
    val Build        = Root / "build"
    val Example      = Root / "example" / "java"
    val ExampleBuild = Example / "build"
    val TdLibBin     = Root / "tdlib" / "bin"
  }

  trait Builder {
    def build(): Path
  }
}
