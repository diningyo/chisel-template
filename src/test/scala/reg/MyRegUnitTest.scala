// See README.md for license details.

package reg

import java.io.File

import scala.math.{random, floor}
import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class MyRegUnitTester(c: MyReg) extends PeekPokeTester(c) {


  var exp_val = 0

  /**
    * update the register value
    *
    * @param wren write enable
    * @param wrdata positive integer
    * @return the Register value
    */
  def updateMyReg(wren: Boolean, wrdata: Int): Int = {

    if (wren) {
      exp_val = wrdata
    }

    exp_val
  }

  private val r = c

  for(i <- 1 to 40 by 3) {
    val wren =  floor(random * 2).toInt == 1
    val wrdata = floor(random * 10).toInt

    poke(r.io.in_wren, wren)
    poke(r.io.in_val, wrdata)
    step(1)

    val expected_val = updateMyReg(wren, wrdata)

    expect(r.io.out, expected_val)

    println(f"(wren, wrdata, io.out) = ($wren%5s, 0x$wrdata%02x, 0x${peek(r.io.out).toInt}%02x)")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GCDTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GCDTester'
  * }}}
  */
class MyRegTester extends ChiselFlatSpec {
  // Disable this until we fix isCommandAvailable to swallow stderr along with stdout
  private val backendNames = if(false && firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "MyReg" should s"calculate proper greatest common denominator (with $backendName)" in {
      Driver(() => new MyReg, backendName) {
        c => new MyRegUnitTester(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new MyReg) {
      c => new MyRegUnitTester(c)
    } should be (true)
  }

  "using --backend-name verilator" should "be an alternative way to run using verilator" in {
    if(backendNames.contains("verilator")) {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new MyReg) {
        c => new MyRegUnitTester(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new MyReg) {
      c => new MyRegUnitTester(c)
    } should be(true)
  }

  /**
    * By default verilator backend produces vcd file, and firrtl and treadle backends do not.
    * Following examples show you how to turn on vcd for firrtl and treadle and how to turn it off for verilator
    */

  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/make_a_vcd", "--top-name", "make_a_vcd"),
      () => new MyReg
    ) {

      c => new MyRegUnitTester(c)
    } should be(true)

    new File("test_run_dir/make_a_vcd/make_a_vcd.vcd").exists should be (true)
  }

  "running with --generate-vcd-output off" should "not create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "off", "--target-dir", "test_run_dir/make_no_vcd", "--top-name", "make_no_vcd",
      "--backend-name", "verilator"),
      () => new MyReg
    ) {

      c => new MyRegUnitTester(c)
    } should be(true)

    new File("test_run_dir/make_no_vcd/make_a_vcd.vcd").exists should be (false)

  }

}
