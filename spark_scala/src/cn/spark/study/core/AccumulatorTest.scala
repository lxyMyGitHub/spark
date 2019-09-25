package cn.spark.study.core

import java.util

import org.apache.spark.{Accumulator, SparkConf, SparkContext}

object AccumulatorTest {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("accimulator")
    val sc = new SparkContext(conf)
    val sum = sc.accumulator(0)
    val numberList= Array("1","2","3","4","5")
    val numbers = sc.parallelize(numberList)
    numbers.foreach(number => println(number))
  }
}
