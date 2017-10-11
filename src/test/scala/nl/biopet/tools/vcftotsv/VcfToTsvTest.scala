package nl.biopet.tools.vcftotsv

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

object VcfToTsvTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      VcfToTsv.main(Array())
    }
  }
}
