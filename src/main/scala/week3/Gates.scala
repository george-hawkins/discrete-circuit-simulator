package week3

// Provide a small description language for digital circuits.
trait Gates extends Simulation {
  // These are just declarations - someone else must provide definitions.
  def InverterDelay: Int
  def AndGateDelay: Int
  def OrGateDelay: Int

  class Wire {
    private var actions: List[Action] = Nil
    private var sigVal = false

    /** Get the current value of the signal transported by this wire. */
    def getSignal: Boolean = sigVal

    /** Modify the signal transported by this wire. */
    def setSignal(s: Boolean): Unit = {
      if (s != sigVal) {
        sigVal = s
        actions foreach (_()) // a => a()
      }
    }

    /** Attach the given action to the wire, all attached actions will be executed at each change of the signal transported by this wire. */
    def addAction(action: Action): Unit = {
      actions = action :: actions
      action()
      // Invoking the action at least once, as here, ensures that agenda events are generated that will cause the
      // initial propagation of input signals to output signals for all gates, e.g. ensuring that the output signals
      // of all inverters will become the opposite to their input signals.
    }
  }

  // Functions to place gates and inverters (connected to given wires) onto our virtual circuit board.
  // Each function has the side-effect of creating the object instances necessary to model the needed gate or inverter.

  def inverter(input: Wire, output: Wire): Unit = {
    def inverterAction(): Unit = {
      // Note: We are modeling the propagation delay across this component - its important to record the value
      // of the signal as it exists now, and not use the value, whatever it may be, after the delay.
      val inputSignal = input.getSignal
      afterDelay(InverterDelay) { output setSignal !inputSignal }
    }

    // TODO: why if you remove the () from def inverterAction() is this line then invalid?
    input addAction inverterAction
  }

  def andGate(in1: Wire, in2: Wire, output: Wire): Unit = {
    def andGateAction(): Unit = {
      val in1Sig = in1.getSignal
      val in2Sig = in2.getSignal
      afterDelay(AndGateDelay) { output setSignal (in1Sig & in2Sig) }
    }

    in1 addAction andGateAction
    in2 addAction andGateAction
  }

  def orGate(in1: Wire, in2: Wire, output: Wire): Unit = {
    def orGateAction(): Unit = {
      val in1Sig = in1.getSignal
      val in2Sig = in2.getSignal
      afterDelay(OrGateDelay) { output setSignal (in1Sig | in2Sig) }
    }

    in1 addAction orGateAction
    in2 addAction orGateAction
  }

  // Using De Morgan's law we can alternatively construct our OR gate from other elements (in the same
  // way the half and full adder do in Circuits). I.e. a | b = !(!a & !b)
  def orGateAlt(in1: Wire, in2: Wire, output: Wire): Unit = {
    val notIn1, notIn2, notOut = new Wire

    inverter(in1, notIn1)
    inverter(in2, notIn2)
    andGate(notIn1, notIn2, notOut)
    inverter(notOut, output)
  }

  def probe(name: String, wire: Wire): Unit = {
    def probeAction(): Unit = {
      println(s"$name - time=$currentTime new-value=${wire.getSignal}")
    }

    wire addAction probeAction
  }
}
