package cn.scala.study.base
import scala.io.Source

/**
  * 文件读取
  */
object ScalaIOTest {
  def main(args: Array[String]): Unit = {
    val source = Source.fromFile("D:\\workspace\\idea_workspace\\spark_scala\\note\\笔记\\scaladoc的使用.txt","utf-8")
//    val lineIterator = source.getLines()

    /**
      * 这里说明一点: 一个BufferedSource对象的getLines方法，只能调用一次，一次调用完之后，遍历了迭代器里所有的内容，就已经把文件里的内容读取完了
      * 如果反复调用source.getLines，是获取不到内容的
      * 此时，必须重新创建一个BufferedSource对象
      */
//    for(line <- lineIterator) println(line)


//    val lines = source.getLines.toArray
//    for(line <- lines)println(line)
//
//    val linesMkString = source.mkString
//    for(line <- linesMkString)print(line)

    for( c <- source) print(c)
  }

}
