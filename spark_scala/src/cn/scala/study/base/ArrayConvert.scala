package cn.scala.study.base

import scala.collection.mutable.ArrayBuffer

/**
  * Scala进阶编程:多维数组,Java数组与Scala数组的隐式转换
  */
object ArrayConvert {
  def main(args: Array[String]): Unit = {
    function3()
  }

  /**
    *多维数组
    * 数组的元素,还是数组,数组套数组,就是多维数组
    */
  def function1(): Unit ={
    val multiDimArr1 = Array.ofDim[Double](3,4)
    multiDimArr1(0)(0) = 1.0
    println(multiDimArr1(0)(0))
  }

  def function2()={
    val multiDimArr2 = new Array[Array[Int]](3)
    multiDimArr2(0) = new Array[Int](1)
    multiDimArr2(1) = new Array[Int](2)
    multiDimArr2(2) = new Array[Int](3)
    multiDimArr2(1)(1) = 1
//    println(multiDimArr2.foreach(a => a.foreach(b => println(b))))
    multiDimArr2
  }

  /**
    * Java数组与Scala数组的隐式转换
    */
  def function3()={
    import scala.collection.JavaConversions.bufferAsJavaList
    import scala.collection.mutable.ArrayBuffer

    val command = ArrayBuffer("javac","C:\\Windows\\System32\\cmd.exe\\HelloWorld.java")
    val processBuilder = new ProcessBuilder(command)
    val process = processBuilder.start()

    val res = process.waitFor()
    import scala.collection.JavaConversions.asScalaBuffer
    import scala.collection.mutable.Buffer
    val com:Buffer[String] = processBuilder.command()
  }

}
