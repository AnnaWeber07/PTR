import scala.collection.immutable._

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
}
