import java.security.Key
import scala.collection.MapView
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
    else if (max == 0)
      println(min + " " + max + " " + mid)
  }

  def rotateLeft[A](sequence: Seq[A], i: Int): Seq[A] = {
    val size = sequence.size

    sequence.drop(i % size) ++ sequence.take(i % size)
  }

  def consecutiveDigitsCollector(list: List[Int]): List[Int] = {
    list.head :: list.sliding(2).collect { case Seq(a, b) if a != b => b }.toList
  }

  def listRightAngleTriangles(): List[(Int, Int, Int)] = {
    val triangles = for {
      a <- 1 until 20
      b <- 1 until 20
      c = math.sqrt(a * a + b * b).toInt
      if a * a + b * b == c * c
    } yield (a, b, c)
    triangles.toList
  }

  /*def findQwertyRow(list: List[String]): List[String] = {
    var a: Map[Char, Int] = Map()

    val mapping = Map('q' -> 1, 'w' -> 1, 'e' -> 1, 'r' -> 1,
      't' -> 1, 'y' -> 1, 'u' -> 1, 'i' -> 1,
      'o' -> 1, 'p' -> 1, 'a' -> 2, 's' -> 2,
      'd' -> 2, 'f' -> 2, 'g' -> 2, 'h' -> 2,
      'j' -> 2, 'k' -> 2, 'l' -> 2, 'z' -> 3,
      'x' -> 3, 'c' -> 3, 'v' -> 3, 'b' -> 3,
      'n' -> 3, 'm' -> 3)

    //list.forall(y => )
  }*/

  def encryption(encrypt: String, key: Int): String = {
    encrypt.map(c => ((c + key - 97) % 26 + 97).toChar).mkString
  }

  def decryption(decrypt: String, key: Int): String = {
    decrypt.map(c => ((c - key - 97) % 26 + 97).toChar).mkString
  }


  def combinationsOfLetters(digits: String): List[String] = {
    val mapping = Map(
      '2' -> "abc",
      '3' -> "def",
      '4' -> "ghi",
      '5' -> "jkl",
      '6' -> "mno",
      '7' -> "pqrs",
      '8' -> "tuv",
      '9' -> "wxyz"
    )

    def generateCombinations(current: String, digits: String): List[String] = {
      if (digits.isEmpty) List(current)
      else {
        for {
          letter <- mapping(digits.head).toList
          combination <- generateCombinations(current + letter, digits.tail)
        } yield combination
      }
    }

    generateCombinations("", digits)
  }


  def groupAnagrams(strs: Array[String]): MapView[String, List[String]] = {
    strs.groupBy(_.sorted).mapValues(_.toList)
  }


  def arabicToRoman(arabic: String): String = {
    val arabicNum = arabic.toInt
    if (arabicNum < 1 || arabicNum > 3999) throw new IllegalArgumentException("Input must be between 1 and 3999")
    val numeralMap = Map(
      1000 -> "M",
      900 -> "CM",
      500 -> "D",
      400 -> "CD",
      100 -> "C",
      90 -> "XC",
      50 -> "L",
      40 -> "XL",
      10 -> "X",
      9 -> "IX",
      5 -> "V",
      4 -> "IV",
      1 -> "I"
    )
    var remaining = arabicNum
    var roman = ""
    numeralMap.keys.toList.sortWith(_ > _).foreach { key =>
      while (remaining >= key) {
        roman += numeralMap(key)
        remaining -= key
      }
    }
    roman
  }

  def factorize(num: Int): List[Int] = {
    val result = scala.collection.mutable.ArrayBuffer[Int]()
    var remaining = num
    var factor = 2
    while (factor <= remaining) {
      if (remaining % factor == 0) {
        result += factor
        remaining = remaining / factor
      } else {
        factor += 1
      }
    }
    result.toList
  }

  def lineWords(words: Array[String]): Array[String] = {
    val topRow = Set('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val midRow = Set('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val bottomRow = Set('z', 'x', 'c', 'v', 'b', 'n', 'm')
    val rows = Array(topRow, midRow, bottomRow)

    def isOneRowWord(word: String): Boolean = {
      val wordSet = word.toLowerCase().toSet
      rows.exists(row => wordSet.subsetOf(row))
    }

    val oneRowWords = words.filter(isOneRowWord)

    oneRowWords.foreach(println)

    oneRowWords
  }

  def Output(str: String): Unit = {
    println(str)
  }
}
