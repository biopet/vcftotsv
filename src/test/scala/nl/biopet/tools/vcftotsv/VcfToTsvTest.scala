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

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

import scala.util.Random

class VcfToTsvTest extends ToolTest[Args] {
  def toolCommand: VcfToTsv.type = VcfToTsv

  import VcfToTsv._

  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      VcfToTsv.main(Array())
    }
  }

  val rand = new Random()

  val vepped: String = resourcePath("/VEP_oneline.vcf")
  val unvepped: String = resourcePath("/unvepped.vcf")

  @Test def testAllFields(): Unit = {
    val tmp = File.createTempFile("VcfToTsv", ".tsv")
    tmp.deleteOnExit()
    val tmpPath = tmp.getAbsolutePath
    val arguments = Array("-I", unvepped, "-o", tmpPath, "--all_info")
    main(arguments)
  }

  @Test def testSpecificField(): Unit = {
    val tmp = File.createTempFile("VcfToTsv", ".tsv")
    tmp.deleteOnExit()
    val tmpPath = tmp.getAbsolutePath
    val arguments = Array("-I", vepped, "-o", tmpPath, "-i", "CSQ")
    main(arguments)
  }

  @Test def testNewSeparators(): Unit = {
    val tmp = File.createTempFile("VcfToTsv", ".tsv")
    tmp.deleteOnExit()
    val tmpPath = tmp.getAbsolutePath
    val arguments =
      Array("-I",
            vepped,
            "-o",
            tmpPath,
            "--all_info",
            "--separator",
            ",",
            "--list_separator",
            "|")
    main(arguments)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def testIdenticalSeparators(): Unit = {
    val tmpPath = "/tmp/VcfToTsv_" + rand.nextString(10) + ".tsv"
    val arguments =
      Array("-I", vepped, "-o", tmpPath, "--all_info", "--separator", ",")
    main(arguments)
  }

  @Test def testFormatter(): Unit = {
    val formatter = createFormatter(2)
    formatter.format(5000.12345) should be("5000.12")
    val nformatter = createFormatter(3)
    nformatter.format(5000.12345) should be("5000.123")
  }

  @Test def testSortFields(): Unit = {
    val unsortedFields = Set("Child01-GT",
                             "Mother02-GT",
                             "Father03-GT",
                             "INFO-Something",
                             "INFO-ScoreSomething",
                             "INFO-AlleleScoreSomething",
                             "WeirdField")
    val samples = List("Child01", "Father03", "Mother02")

    val sorted = sortFields(unsortedFields, samples)
    sorted should be(
      List("WeirdField",
           "INFO-AlleleScoreSomething",
           "INFO-ScoreSomething",
           "INFO-Something",
           "Child01-GT",
           "Father03-GT",
           "Mother02-GT"))
  }
}
