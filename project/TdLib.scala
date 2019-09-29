import sbt._
import os._

import scala.collection.immutable
import scala.util.matching.Regex

object TdLib {

  lazy val buildTdLib = taskKey[Unit]("Build tdlib native library and java wrapper")

  def buildTdLibImpl: Unit = {
    platform.Platform() match {
      case Some(p) =>
        TdLib.builders.get(p).foreach(_.build())
      case None => throw new RuntimeException("Cannot recognize os name or os arch!")
    }
  }

  private val builders: Map[platform.Platform, Builder] =
    Map(
      platform.Platform.Mac     -> MacOs,
      platform.Platform.Windows -> Windows
    )

  private case object MacOs extends Builder {
    override def build(): Path = {

      if (!exists(FS.Lib) || !exists(FS.Lib / jarFileName)) {
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

        list(FS.TdLibBin).foreach(copy.matching {
          case _ / file => FS.Lib / file
        })
        proc("jar", "cvf", jarFileName, ".").call(cwd = FS.Lib, stdout = Inherit, stderr = Inherit)
      }

      FS.TdLibBin
    }
  }

  private case object Windows extends Builder {
    override def build(): Path = {
      println("Sorry, will be implemented soon!")
      FS.ExampleBuild
    }
  }

  private object FS {
    val Root: Path         = pwd
    val TdLib: Path        = Root / "tdlib"
    val Lib: Path          = Root / "lib"
    val Build: Path        = TdLib / "build"
    val Example: Path      = TdLib / "example" / "java"
    val ExampleBuild: Path = Example / "build"
    val TdLibBin: Path     = TdLib / "tdlib" / "bin"
  }

  sealed trait Builder {
    def build(): Path
  }

  private val jarFileName = "tdlib_1.0.jar"
}

object platform {
  import enumeratum._

  abstract sealed class Platform(val osArch: Map[Regex, Regex]) extends EnumEntry

  object Platform extends Enum[Platform] {
    case object Mac     extends Platform(Map("^.*(mac|darwin).*$".r -> ".*".r))
    case object Windows extends Platform(Map("^.*(windows).*$".r -> ".*".r))

    override def values: immutable.IndexedSeq[Platform] = findValues

    def apply(): Option[Platform] =
      for {
        name <- sys.props.get("os.name")
        arch <- sys.props.get("os.arch")
        platform <- values.find(_.osArch.exists {
                     case (os, ar) =>
                       name.toLowerCase.matches(os.regex) && arch.toLowerCase.matches(ar.regex)
                   })
      } yield platform
  }
}
