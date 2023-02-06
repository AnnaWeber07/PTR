import scala.collection.immutable._
import scala.util.Random

object RiceFields {

  def isPrime(primes: Int): Boolean = {
    //require(primes (_ >= 0), "negative number")
    if
    (primes <= 1)
      false
    else if (primes == 2)
      true
    else
      !(2 until primes).exists(n => primes % n == 0)
  }

  def cylinderArea(height: Int, rad: Int): Double = {
    2 * Math.PI * rad * (rad + height)
  }

  def reversal(list: List[Int]): List[Int] = {
    list.reverse
  }

  def uniqueSum(list: List[Int]): Int = {
    list.distinct.sum
  }

  def fibonacciNumbers(a: Int = 0, b: Int = 1, count: Int = 2): List[Int] = {

    val n = 5

    val c = a + b
    if (count >= n) {
      List(c)
    }

    else if (a == 0 && b == 1) {
      List(a, b, c) ++ fibonacciNumbers(b, c, count + 1)
    }

    else {
      c +: fibonacciNumbers(b, c, count + 1)
    }
  }

  // def extractRandomNumber(random: Int, list: List[Int]): List[Int] = {

  // }

  def translate(string: String): String = {
    var A: Map[String, String] = Map()

    val relatives = Map("mama" -> "mother", "papa" -> "father")

    relatives.foldLeft(string) { case (string, (key, value)) => string.replaceAll(key, value) }

  }


  def smallest(a: Int, b: Int, c: Int): Unit = {
    var max = c

    if (a > max || b > max) {
      if (a > b)
        max = a
      else
        max = b
    }

    var min = c
    if (a < min || b < min) {
      if (a < b)
        min = a
      else
        min = b
    }

    var mid = a + b + c - min - max

    if (min != 0 && mid != 0 && max != 0)
      println(min + " " + mid + " " + max)
    else if (min == 0)
      println(mid + " " + min + " " + max)
    else if (min == 0 && mid == 0)
      println(max + " " + min + " " + mid)
    else if (min == 0 && max == 0)
      println(mid + " " + min + " " + max)
    else if (max == 0)
      println(min + " " + max + " " + mid)
    else if (mid == 0)
      println(min + " " + mid + " " + max)
  }

  def rotateLeft[A](sequence: Seq[A], i: Int): Seq[A] = {
    val size = sequence.size

    sequence.drop(i % size) ++ sequence.take(i % size)
  }
}
