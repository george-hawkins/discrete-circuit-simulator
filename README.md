Discrete circuit simulator
==========================

This repository contains an implementation of the discrete circuit simulator discussed in lectures 3.4 to 3.6 of the Coursera course [Functional Program Design in Scala](https://www.coursera.org/learn/progfun2).

In the discussion forums covering these lectures the question came up as to whether one could do without the initial invocation of the action passed into the `Wire.addAction` method found in [`Gates.scala`](src/main/scala/week3/Gates.scala).

This repository contains a set of tests in [`SimulatorSuite.scala`](https://github.com/george-hawkins/discrete-circuit-simulator/blob/master/src/test/scala/week3/SimulatorSuite.scala) that demonstrate the importance of this initial action invocation.

And below is my post commenting on this.

---

*Subject:* Wire.addAction question
*Author:* George Hawkins
*Date:* September 3rd, 2016 18:16 CEST

I think F. is incorrect in believing that we can remove the initial invocation of the action passed in to *addAction* (even if we later prime the agenda with an initial event).

It's not enough to provide an initial action to get the simulation going.

In fact it's fairly easy to demonstrate that even setting the signals of all our _input_ wires to true (and so causing corresponding actions to be added) before calling *run* on our simulation is not enough to trigger the sequence of events needed to establish the expected signals on all our _output_ wires.

The purpose of invoking each action, once on adding it, is to ensure that initial events are generated that establish an expected initial state on the outputs of all gates.

E.g. when you attach input and output wires to an inverter both wires have an initial signal with value false - but the output wire of an inverter should obviously have the inverse signal of its input wire.

If we did not trigger the *inverterAction* on adding it to the inverter's input wire then the output wire would only ever acquire an appropriate state once the input wire was first toggled from false to true at some later stage.

If we remove the invocation in *addAction* you might then ask if it's enough to just set at least one of our input wires to true before we run the simulation? Will this cause a cascade of events that achieve the expected output state?

In a circuit consisting of a single inverter the answer is yes.

But we can construct a slightly more complex circuit to demonstrate that this isn't enough.

If we take an AND gate followed by an inverter and set one of the input wires to true and run the simulation we'll see that the AND gate's output remains false (as expected) and that the input and output wires of our inverter will both still be false once *run* returns, i.e. the output of the inverter will not have acquired the correct state.

OK - but what if we set all our input wires to true before running the simulation, surely this will be enough?

In the AND gate, followed by an inverter, case it will be. But can we think of a gate where transitioning from two false inputs to two true inputs won't toggle the output signal?

How about an XOR gate?

Hmm... if we try that and set both inputs to true everything works properly (even without our action invocation logic).

So it looks like setting all input wires to true might be enough to cause a reasonable initial state to be established.

But actually it's not - the problem with the XOR case is that the two input wires of the XOR can never be set to true simultaneously - if we set the two signals to true one after the other we will get two events in our agenda with the same priority but one or other still has to be picked off and handled first (our agenda *insert* logic ensures a nice FIFO handling for items of equal priority).

So can we think of a logic gate that won't toggle it's output state despite this rippling of the inputs from both false to both true?

Yes - gates that correspond to the obscure but still simple [material nonimplication](https://en.wikipedia.org/wiki/Material_nonimplication) (Lpq for short) and [converse nonimplication](https://en.wikipedia.org/wiki/Converse_nonimplication) (Mpq for short).

* Lpq is simply p & !q
* Mpq is simply !p & q

So Lpq will maintain an output of false if we ripple from false to true in one direction while Mpq will maintain an output of false if we ripple from the other direction.

And indeed if we construct simple circuits consisting of just an Lpq gate followed by an inverter or an Mpq gate followed by an inverter we can see that the output of the gate remains false as both input signals are set to true, as long as we set the signals to true in the order appropriate for the given gate (q first for Lpq and p first for Mpq). And so the inverter will never acquire the correct output state if we've removed our initial invocations of the underlying actions.

I've constructed a set of unit tests that you can see here - [SimulatorSuite.scala](src/test/scala/week3/SimulatorSuite.scala)

Run them yourself and see that they all pass, then comment out the action invocation in *Gates.addAction* and rerun them.

Some of the tests will continue to pass but some will fail, showing that the commented out invocation was essential in these cases.

The tests are as shown here and (except for the first case) all involve a binary (two input) gate followed by an inverter:

* "output of (not false) is true" - *fail*
* "output of not(and true false) is true" - *fail*
* "output of not(xor true true) is true" - ok
* "output of not(lpq true true), where in1 becomes true first, is true" - ok
* "output of not(lpq true true), where in2 becomes true first, is true" - *fail*
* "output of not(mpq true true), where in1 becomes true first, is true" - *fail*
* "output of not(mpq true true), where in2 becomes true first, is true" - ok

The ones marked with fail are the ones that will no longer pass if you modify *addAction* as outlined.

The particularly interesting ones are those involving Lpq and Mpq where all inputs are set to true. The order that this happens affects whether the test will pass or fail after the modification - the order that fails for Lpq will be the one that still passes for Mpq and vice-versa.

OK - so we demonstrated that setting all input signals to true is not enough. If you're not completely bored already read on...

So is all this simply about ensuring that each gate has the initial output signal appropriate if its input signals are all false, i.e. the initial state of all wires?

No - it's really about ensuring that the outputs of our circuits establish a set of values that reflect all the inputs, irrespective of what they may be.

In simple circuits we see that the problem is immediately obvious with gates where the input signals being all false does not result in the output signal also being false.

This is true of our inverter - but would clearly also be true for standard gates like NAND, NOR, XOR and XNOR.

And in any but the most trivial circuits the problem will also manifest itself when establishing state across multiple connected gates.

In considering all this it's important to also consider the role of the check in *Wire.setSignal* that ensures the actions are only invoked if the value being set is different to the current value. It's this check that causes the continuously false output signal of the gates in our tests not to trigger any inverter related actions.

It's interesting to think about how what's been discussed here would be affected if this check were removed and why removal of this check would be a very bad idea.

It's also interesting to think about circuits that would never establish an initial fixed state, even if all inputs were false before *run* was called.

This could happen if one or more output wires were looped around such that they became input wires for earlier stages in the circuit.

Such a setup might well never halt, i.e. calling *run* would never lead to the agenda eventually being emptied out and *run* returning.

Would such a circuit be useless? Not necessarily - many real world circuits never halt. What's important is that you can extract state despite *run* never returning.

We already have an example of such state extraction with our probes.

Why would you want a circuit that never halted? There are many real world routine tasks that never halt. And there are many algorithms that go on as long as you have time to wait, e.g. calculating digits of pi.

---
