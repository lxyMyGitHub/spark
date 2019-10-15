package cn.scala.study.base

import java.util

/**
  * Tuple拉链
  */
object TupleAndMap {
  def main(args: Array[String]): Unit = {
    val students = Array("Leo","Tom","Jack")
    val scores = Array(80,90,100)
    val studentScores = students.zip(scores)
    for((student,score) <- studentScores){
      println("student :  " + student + "  score : "+score)
    }
    //将array转换为map

    val studentScoresMap = studentScores.toMap
    println(studentScoresMap("Tom"))

    //javaMap 和ScalaMap隐式转换
    val javaScoreMap = new util.HashMap[String,Int]()
    javaScoreMap.put("leo",11)
    javaScoreMap.put("Tom",12)
    javaScoreMap.put("jack",13)
    println(javaScoreMap.get("jack"))
    import scala.collection.JavaConversions.mapAsScalaMap
    val scalaScoreMap: scala.collection.mutable.Map[String,Int] = javaScoreMap
    println(scalaScoreMap("Tom"))


  }



}
