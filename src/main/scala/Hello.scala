object Hello {
  def main(args: Array[String]): Unit = {
    //W1
    println("Hello PTR")
    println()

    //W2
    val x = 13
    println("is " + x + " prime?" + " " + RiceFields.isPrime(x))
    println()

    val height = 3
    val radius = 4

    println("area of cylinder with height $height and radius $radius")
    println(RiceFields.cylinderArea(height, radius))
    println()

    val integers: List[Int] = List(1, 2, 4, 8, 4)

    println("Original list: " + integers)
    println("Reversed list: " + RiceFields.reversal(integers))
    println()


  }
}