/*
 * Copyright (c) 2014 Sequencing Analysis Support Core - Leiden University Medical Center
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

import java.io.PrintStream
import java.text.DecimalFormat

import htsjdk.variant.vcf.VCFFileReader
import nl.biopet.utils.tool.ToolCommand

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object VcfToTsv extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)

  val defaultFields = List("CHROM", "POS", "ID", "REF", "ALT", "QUAL")

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    // Throw exception if separator and listSeparator are identical
    if (cmdArgs.separator == cmdArgs.listSeparator)
      throw new IllegalArgumentException(
        "Separator and list_separator should not be identical"
      )

    val formatter = createFormatter(cmdArgs.maxDecimals)

    val reader = new VCFFileReader(cmdArgs.inputFile, false)
    val header = reader.getFileHeader
    val samples = header.getSampleNamesInOrder

    val allInfoFields = header.getInfoHeaderLines.map(_.getID).toList
    val allFormatFields = header.getFormatHeaderLines.map(_.getID).toList

    val fields: Set[String] = (if (cmdArgs.disableDefaults) Nil
                               else defaultFields)
      .toSet[String] ++
      cmdArgs.fields.toSet[String] ++
      (if (cmdArgs.allInfo) allInfoFields else cmdArgs.infoFields)
        .map("INFO-" + _) ++ {
      val buffer: ListBuffer[String] = ListBuffer()
      for (f <- if (cmdArgs.allFormat) allFormatFields
           else cmdArgs.sampleFields;
           sample <- samples) {
        buffer += sample + "-" + f
      }
      buffer.toSet[String]
    }

    val sortedFields = sortFields(fields, samples.toList)

    val writer =
      if (cmdArgs.outputFile != null) new PrintStream(cmdArgs.outputFile)
      else Console.out

    writer.println(sortedFields.mkString("#", cmdArgs.separator, ""))
    for (vcfRecord <- reader) {
      val values: mutable.Map[String, Any] = mutable.Map()
      values += "CHROM" -> vcfRecord.getContig
      values += "POS" -> vcfRecord.getStart
      values += "ID" -> vcfRecord.getID
      values += "REF" -> vcfRecord.getReference.getBaseString
      values += "ALT" -> {
        val t = for (a <- vcfRecord.getAlternateAlleles) yield a.getBaseString
        t.mkString(cmdArgs.listSeparator)
      }
      values += "QUAL" -> (if (vcfRecord.getPhredScaledQual == -10) "."
                           else formatter.format(vcfRecord.getPhredScaledQual))
      values += "INFO" -> vcfRecord.getFilters
      for ((field, content) <- vcfRecord.getAttributes) {
        values += "INFO-" + field -> {
          content match {
            case a: List[_] => a.mkString(cmdArgs.listSeparator)
            case a: Array[_] => a.mkString(cmdArgs.listSeparator)
            case a: java.util.ArrayList[_] => a.mkString(cmdArgs.listSeparator)
            case _ => content
          }
        }
      }

      for (sample <- samples) {
        val genotype = vcfRecord.getGenotype(sample)
        values += sample + "-GT" -> {
          val l = for (g <- genotype.getAlleles)
            yield vcfRecord.getAlleleIndex(g)
          l.map(x => if (x < 0) "." else x).mkString("/")
        }
        if (genotype.hasAD)
          values += sample + "-AD" -> List(genotype.getAD: _*)
            .mkString(cmdArgs.listSeparator)
        if (genotype.hasDP) values += sample + "-DP" -> genotype.getDP
        if (genotype.hasGQ) values += sample + "-GQ" -> genotype.getGQ
        if (genotype.hasPL)
          values += sample + "-PL" -> List(genotype.getPL: _*)
            .mkString(cmdArgs.listSeparator)
        for ((field, content) <- genotype.getExtendedAttributes) {
          values += sample + "-" + field -> content
        }
      }
      val line = for (f <- sortedFields) yield {
        if (values.contains(f)) {
          values(f)
        } else ""
      }
      writer.println(line.mkString(cmdArgs.separator))
    }
  }

  /**
    *  This function creates a correct DecimalFormat for a specific length of decimals
    * @param len number of decimal places
    * @return DecimalFormat formatter
    */
  def createFormatter(len: Int): DecimalFormat = {
    val patternString = "###." + (for (_ <- 1 to len) yield "#").mkString("")
    new DecimalFormat(patternString)
  }

  /**
    * This fields sorts fields, such that non-info and non-sample specific fields (e.g. general ones) are on front
    * followed by info fields
    * followed by sample-specific fields
    * @param fields fields
    * @param samples samples
    * @return sorted samples
    */
  def sortFields(fields: Set[String], samples: List[String]): List[String] = {
    def fieldType(x: String) = x match {
      case _ if x.startsWith("INFO-") => 'i'
      case _ if samples.exists(y => x.startsWith(y + "-")) => 'f'
      case _ => 'g'
    }

    fields.toList.sortWith((a, b) => {
      (fieldType(a), fieldType(b)) match {
        case ('g', 'g') =>
          val ai = defaultFields.indexOf(a)
          val bi = defaultFields.indexOf(b)
          if (bi < 0) true else ai <= bi
        case ('f', 'f') =>
          val sampleA = a.split("-").head
          val sampleB = b.split("-").head
          sampleA.compareTo(sampleB) match {
            case 0 => !(a.compareTo(b) > 0)
            case i if i > 0 => false
            case _ => true
          }
        case ('g', _) => true
        case (_, 'g') => false
        case (a2, b2) if a2 == b2 => !(a2.compareTo(b2) > 0)
        case ('i', _) => true
        case _ => false
      }
    })
  }

  def descriptionText: String =
    """
      |This tool converts a vcf file to a Tab Separated Values (TSV) file. For every key in the INFO column of the VCF file,
      |a separate column will be created with the corresponding values. The user can select the keys that will be parsed into
      |the output TSV file. This can be useful in the case a program only accepts a TSV file for downstream analysis.
      |
    """.stripMargin

  def manualText: String =
    """
      |The output of this tool is a TSV file produced from the input vcf file. Depending on which options are enabled their
      | could be some fields discarded. Fieldseparator and list separator values can be selected.
    """.stripMargin

  def exampleText: String =
    """
      |To run a simple conversion that will include all info fields in the resulting tsv run:
    """.stripMargin +
      example("--inputFile",
              "myVCF.vcf",
              "--outputFile",
              "my_tabDelimited_VCF.tsv",
              "--all_info")
}
