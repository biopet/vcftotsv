organization := "com.github.biopet"
organizationName := "Sequencing Analysis Support Core - Leiden University Medical Center"

//TODO: Start year should reflect the tools original start year on github.com/biopet/biopet in the tools section
startYear := Some(2014)

name := "vcftotsv"
biopetUrlName := "vcftotsv"

biopetIsTool := true

mainClass in assembly := Some("nl.biopet.tools.vcftotsv.VcfToTsv")


developers := List(
  Developer(id="ffinfo", name="Peter van 't Hof", email="pjrvanthof@gmail.com", url=url("https://github.com/ffinfo"))
)

scalaVersion := "2.11.11"

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.2"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.1" % Test
libraryDependencies += "com.github.biopet" %% "ngs-utils" % "0.1"