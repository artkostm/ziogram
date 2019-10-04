import sbt._
import os._

import scala.collection.immutable
import scala.util.matching.Regex

object TdLib {

  lazy val buildTdLib = taskKey[Unit]("Build tdlib native library and java wrapper")

  def buildTdLibImpl(logger: util.Logger): Unit =
    platform.Platform() match {
      case Some(p) =>
        TdLib.builders.get(p).foreach(_.build(logger))
      case None => throw new RuntimeException("Cannot recognize os name or os arch!")
    }

  private val builders: Map[platform.Platform, Builder] =
    Map(
      platform.Platform.Mac     -> MacOs,
      platform.Platform.Windows -> Windows
    )

  private case object MacOs extends Builder {
    override def build(logger: util.Logger): Path = {

      if (!exists(FS.LibBinFile)) {
        remove.all(FS.TdBuild)
        makeDir(FS.TdBuild)

        proc(
          "cmake",
          "-DCMAKE_BUILD_TYPE=Release",
          "-DOPENSSL_ROOT_DIR=/usr/local/opt/openssl/",
          "-DCMAKE_INSTALL_PREFIX:PATH=../example/java/td",
          "-DTD_ENABLE_JNI=ON",
          ".."
        ).call(cwd = FS.TdBuild, stdout = Inherit, stderr = Inherit)

        proc("cmake", "--build", ".", "--target", "install")
          .call(cwd = FS.TdBuild, stdout = Inherit, stderr = Inherit)

        remove.all(FS.NativeBuild)
        makeDir(FS.NativeBuild)

        proc(
          "cmake",
          "-DCMAKE_BUILD_TYPE=Release",
          "-DCMAKE_INSTALL_PREFIX:PATH=../../../native",
          "-DTd_DIR:PATH=" + (FS.Example / "td" / "lib" / "cmake" / "Td").toString(),
          "-DBINDINGS_SOURCE_DIR=../../main/java",
          "-DTD_SOURCE_DIR=" + FS.Example.toString(),
          ".."
        ).call(cwd = FS.NativeBuild, stdout = Inherit, stderr = Inherit)

        proc("cmake", "--build", ".", "--target", "install")
          .call(cwd = FS.NativeBuild, stdout = Inherit, stderr = Inherit)
      }

      FS.LibBinFile
    }
  }

  private case object Windows extends Builder {
    override def build(logger: util.Logger): Path = {
      logger.warn("Sorry, will be implemented soon!")
      FS.LibBinFile
    }
  }

  private object FS {
    val Root: Path       = pwd / "src" / "native"
    val NativeBuild      = Root / "build"
    val TdLib: Path      = Root / "td"
    val LibBinFile: Path = pwd / "native" / "bin" / System.mapLibraryName("tdjni")
    val TdBuild: Path    = TdLib / "build"
    val Example: Path    = TdLib / "example" / "java"
  }

  sealed trait Builder {
    def build(logger: util.Logger): Path
  }
}

object platform {
  import enumeratum._

  abstract sealed class Platform(val osArch: Map[Regex, Regex]) extends EnumEntry

  object Platform extends Enum[Platform] {
    case object Mac     extends Platform(Map("^.*(mac|darwin).*$".r -> ".*".r))
    case object Windows extends Platform(Map("^.*(windows).*$".r    -> ".*".r))

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
