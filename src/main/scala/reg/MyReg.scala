// See README.md for license details.

package reg

import chisel3._

class MyReg extends Module {
  val io = IO(new Bundle {
    val in_val = Input(UInt(8.W))
    val in_wren = Input(Bool())
    val out = Output(UInt(8.W))
  })

  val reg = RegInit(0.U(8.W))

  when (io.in_wren) {
    reg := io.in_val
  }

  io.out := reg
}
