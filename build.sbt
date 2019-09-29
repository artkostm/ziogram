lazy val ziogram = (project in file(".")).settings(
  name := "ziogram",
  version := "1.0",
  scalaVersion := "2.12.10",
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio"         % "1.0.0-RC13",
    "dev.zio" %% "zio-streams" % "1.0.0-RC13"
  ),
  TdLib.buildTdLib := TdLib.buildTdLibImpl
)
