/*
 * Copyright (c) 2014 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.vcftotsv

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('I', "inputFile") required () maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(inputFile = x)
  } text "Input vcf file"
  opt[File]('o', "outputFile") maxOccurs 1 valueName "<file>" action { (x, c) =>
    c.copy(outputFile = x)
  } text "output file, default to stdout"
  opt[String]('f', "field") unbounded () action { (x, c) =>
    c.copy(fields = x :: c.fields)
  } text "Genotype field to use" valueName "Genotype field name"
  opt[String]('i', "info_field") unbounded () action { (x, c) =>
    c.copy(infoFields = x :: c.infoFields)
  } text "Info field to use" valueName "Info field name"
  opt[Unit]("all_info") action { (_, c) =>
    c.copy(allInfo = true)
  } text "Use all info fields in the vcf header"
  opt[Unit]("all_format") action { (_, c) =>
    c.copy(allFormat = true)
  } text "Use all genotype fields in the vcf header"
  opt[String]('s', "sample_field") unbounded () action { (x, c) =>
    c.copy(sampleFields = x :: c.sampleFields)
  } text "Genotype fields to use in the tsv file"
  opt[Unit]('d', "disable_defaults") action { (_, c) =>
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
