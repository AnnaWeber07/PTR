import scala.util.Random
import akka.actor.{ActorSystem, Props}
import akka.event.Logging

import java.time.InstantSource.system

object Hello {
  def main(args: Array[String]): Unit = {
    //W1
    println("Hello PTR")
    val str = "Hello PTR"
    var checker = RiceFields.Output(str)
    println("Check the Hello PTR function: " + RiceFields.Verify(checker))

    println()

    //W2
    val x = 13
    println("is " + x + " prime?" + " " + RiceFields.isPrime(x))
    println()

    val height = 3
    val radius = 4

    println("area of cylinder with height $height and radius $radius: ")
    println(RiceFields.cylinderArea(height, radius))
    println()

    val integers: List[Int] = List(1, 2, 4, 8, 4)

    println("Original list: " + integers)
    println("Reversed list: " + RiceFields.reversal(integers))
    println()

    val uniqueElements: List[Int] = List(1, 2, 4, 8, 4, 2)

    println("Elements in a list " + uniqueElements)
    println("Sum: " + RiceFields.uniqueSum(uniqueElements))
    println()

    val random = new Random()
    var randomElements: List[Int] = List(1, 2, 4, 8, 4)
    val randomQuantity = random.nextInt(randomElements.size)

    println("Random elements quantity: " + randomQuantity)
    for (x <- 1 to randomQuantity) {
      print(random.nextInt(randomElements.length) + " ")
    }

    println()
    println()

    println(RiceFields.fibonacciNumbers())

    println()

    val line = "mama is dancing with papa"
    println("Initial: " + line)
    println("Overwritten: " + RiceFields.translate(line))

    println()

    val a = 2
    val b = 4
    val c = 3
    print("Smallest order: ")
    RiceFields.smallest(a, b, c)

    println()

    val seq: Seq[Int] = Seq(1, 2, 4, 8, 4)
    val i = 3

    println(RiceFields.rotateLeft(seq, i))

    println()
    println(RiceFields.listRightAngleTriangles())

    println()

    val consecutiveElementsList: List[Int] = List(1, 2, 2, 2, 4, 8, 4)
    println("Consecutive elements list: " + consecutiveElementsList)
    println("Remove occurrences: " + RiceFields.consecutiveDigitsCollector(consecutiveElementsList))

    println()

    val encText = "lorem"
    val decText = "oruhp"
    println("To be encrypted: " + encText + ". Result: " + RiceFields.encryption(encText, 3))
    println("To be decrypted: " + decText + ". Result: " + RiceFields.decryption(decText, 3))

    println()

    println(RiceFields.combinationsOfLetters("23"))

    println()

    val strings = Array("eat", "tea", "tan", "ate", "nat", "bat")
    val result = RiceFields.groupAnagrams(strings)
    println(result)

    println()

    val arab = "13"
    println(RiceFields.arabicToRoman(arab))

    println()

    val numToFactorize = 13
    println(RiceFields.factorize(numToFactorize))

    println()

    val listOfStrings: Array[String] = Array("Hello", "Alaska", "Dad", "Peace")

    RiceFields.lineWords(listOfStrings)
    println()

    //here starts the 3rd week

  }
}
