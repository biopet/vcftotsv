package nl.biopet.tools.vcftotsv

import java.io.File

import nl.biopet.utils.tool.AbstractOptParser

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('I', "inputFile") required () maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(inputFile = x)
  } text "Input vcf file"
  opt[File]('o', "outputFile") maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(outputFile = x)
  } text "output file, default to stdout"
  opt[String]('f', "field") unbounded () action { (x, c) =>
    c.copy(fields = x :: c.fields)
  } text "Genotype field to use" valueName "Genotype field name"
  opt[String]('i', "info_field") unbounded () action { (x, c) =>
    c.copy(infoFields = x :: c.infoFields)
  } text "Info field to use" valueName "Info field name"
  opt[Unit]("all_info") unbounded () action { (_, c) =>
    c.copy(allInfo = true)
  } text "Use all info fields in the vcf header"
  opt[Unit]("all_format") unbounded () action { (_, c) =>
    c.copy(allFormat = true)
  } text "Use all genotype fields in the vcf header"
  opt[String]('s', "sample_field") unbounded () action { (x, c) =>
    c.copy(sampleFields = x :: c.sampleFields)
  } text "Genotype fields to use in the tsv file"
  opt[Unit]('d', "disable_defaults") unbounded () action { (_, c) =>
    c.copy(disableDefaults = true)
  } text "Don't output the default columns from the vcf file"
  opt[String]("separator") maxOccurs 1 action { (x, c) =>
    c.copy(separator = x)
  } text "Optional separator. Default is tab-delimited"
  opt[String]("list_separator") maxOccurs 1 action { (x, c) =>
    c.copy(listSeparator = x)
  } text "Optional list separator. By default, lists are separated by a comma"
  opt[Int]("max_decimals") maxOccurs 1 action { (x, c) =>
    c.copy(maxDecimals = x)
  } text "Number of decimal places for numbers. Default is 2"
}
