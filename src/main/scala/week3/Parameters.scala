package week3

// Provide parameters appropriate for our particular technology - e.g. these values
// might be for CMOS and elsewhere we might have values appropriate for TTL.
trait Parameters {
  def InverterDelay = 2
  def OrGateDelay = 3
  def AndGateDelay = 5

  // Interesting how these provide the values for the declarations in Gates without there being any inheritance relationship involved.
}
