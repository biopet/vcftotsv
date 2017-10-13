package nl.biopet.tools.vcftotsv

import java.io.File

case class Args(inputFile: File = null,
                outputFile: File = null,
                fields: List[String] = Nil,
                infoFields: List[String] = Nil,
                sampleFields: List[String] = Nil,
                disableDefaults: Boolean = false,
                allInfo: Boolean = false,
                allFormat: Boolean = false,
                separator: String = "\t",
                listSeparator: String = ",",
                maxDecimals: Int = 2)
