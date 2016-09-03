package week3

// Simulation
//     ^
//     |
//   Gates
//     ^
//     |
// Circuits    Parameters
//     ^           ^
//     +-----------+
//     |
//    Sim (i.e. my specific simulation seen here)
//
object Test extends App {

  // See SimulatorSuite for real tests - this is just a play area for the Simulation classes (and includes some
  // of the steps Ordersky walked through using a worksheet).

  object Sim extends Circuits with Parameters

  import Sim._

  val in1, in2, sum, carry = new Wire

  halfAdder(in1, in2, sum, carry)

  // Adding these probes will immediately cause some output as their actions
  // (that do printing) get called once as part of the addAction step.
  probe("sum", sum)
  probe("carry", carry)

  in1 setSignal true
  // We should expect sum to become true (and carry to stay false).
  run()

  in2 setSignal true
  // We should expect sum to become false and carry to become true.
  run()

  in1 setSignal false
  // We should expect sum to become true and carry to become false.
  run()
}
