package week3

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SimulatorSuite extends FunSuite {
  test("output of (not false) is true") {
    val sim = new Circuits with Parameters

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in, out = new Wire

    inverter(in, out)

    runSim()

    assert(in.getSignal == false)
    assert(out.getSignal == true)
  }

  test("output of not(and true false) is true") {
    val sim = new Circuits with Parameters

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in1, in2, notIn, out = new Wire

    andGate(in1, in2, notIn)
    inverter(notIn, out)

    in1.setSignal(true)
    runSim()

    assert(in1.getSignal == true)
    assert(in2.getSignal == false)
    assert(out.getSignal == true)
  }

  test("output of not(xor true true) is true") {
    val sim = new Circuits with Parameters {
      final val XorGateDelay = 3
      def xorGate(in1: Wire, in2: Wire, output: Wire): Unit = {
        def xorGateAction(): Unit = {
          val in1Sig = in1.getSignal
          val in2Sig = in2.getSignal
          afterDelay(XorGateDelay) { output setSignal (in1Sig ^ in2Sig) }
        }

        in1 addAction xorGateAction
        in2 addAction xorGateAction
      }
    }

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in1, in2, notIn, out = new Wire

    xorGate(in1, in2, notIn)
    inverter(notIn, out)

    in1.setSignal(true)
    in2.setSignal(true)
    runSim()

    assert(in1.getSignal == true)
    assert(in2.getSignal == true)
    assert(out.getSignal == true)
  }

  test("output of not(lpq true true), where in1 becomes true first, is true") {
    val sim = new CircuitsPlusLpq

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in1, in2, notIn, out = new Wire

    lpqGate(in1, in2, notIn)
    inverter(notIn, out)

    in1.setSignal(true)
    in2.setSignal(true)
    runSim()

    assert(in1.getSignal == true)
    assert(in2.getSignal == true)
    assert(out.getSignal == true)
  }

  test("output of not(lpq true true), where in2 becomes true first, is true") {
    val sim = new CircuitsPlusLpq

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in1, in2, notIn, out = new Wire

    lpqGate(in1, in2, notIn)
    inverter(notIn, out)

    in2.setSignal(true) // Important - in2 is set to true before in1.
    in1.setSignal(true)
    runSim()

    assert(in1.getSignal == true)
    assert(in2.getSignal == true)
    assert(out.getSignal == true)
  }

  test("output of not(mpq true true), where in1 becomes true first, is true") {
    val sim = new CircuitsPlusMpq

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in1, in2, notIn, out = new Wire

    mpqGate(in1, in2, notIn)
    inverter(notIn, out)

    in1.setSignal(true)
    in2.setSignal(true)
    runSim()

    assert(in1.getSignal == true)
    assert(in2.getSignal == true)
    assert(out.getSignal == true)
  }

  test("output of not(mpq true true), where in2 becomes true first, is true") {
    val sim = new CircuitsPlusMpq

    import sim._
    import sim.{run => runSim} // Clashes with FunSuiteLike.run

    val in1, in2, notIn, out = new Wire

    mpqGate(in1, in2, notIn)
    inverter(notIn, out)

    in2.setSignal(true) // Important - in2 is set to true before in1.
    in1.setSignal(true)
    runSim()

    assert(in1.getSignal == true)
    assert(in2.getSignal == true)
    assert(out.getSignal == true)
  }

  // Circuits with a material nonimplication (written Lpq) gate - https://en.wikipedia.org/wiki/Material_nonimplication
  private class CircuitsPlusLpq extends Circuits with Parameters {
    final val LpqGateDelay = 3
    def lpqGate(in1: Wire, in2: Wire, output: Wire): Unit = {
      def lpqGateAction(): Unit = {
        val in1Sig = in1.getSignal
        val in2Sig = in2.getSignal
        afterDelay(LpqGateDelay) { output setSignal (in1Sig & !in2Sig) }
      }

      in1 addAction lpqGateAction
      in2 addAction lpqGateAction
    }
  }

  // Circuits with a converse nonimplication (written Mpq) gate - https://en.wikipedia.org/wiki/Converse_nonimplication
  private class CircuitsPlusMpq extends Circuits with Parameters {
    final val MpqGateDelay = 3
    def mpqGate(in1: Wire, in2: Wire, output: Wire): Unit = {
      def mpqGateAction(): Unit = {
        val in1Sig = in1.getSignal
        val in2Sig = in2.getSignal
        afterDelay(MpqGateDelay) { output setSignal (!in1Sig & in2Sig) }
      }

      in1 addAction mpqGateAction
      in2 addAction mpqGateAction
    }
  }
}
