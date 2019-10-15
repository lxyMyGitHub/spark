package cn.scala.study.base

object ClassInClassTest {
  def main(args: Array[String]): Unit = {
    //普通内部类操作
    //c2的学生只能在c2,不能去c1
    //    val c1 = new Class
    //    val leo = c1.register("leo")
    //    c1.students += leo
    //    val c2 = new Class
    //    val jack = c2.register("jack")
    //    c2.students += jack




    //扩大内部类作用域:伴生对象
    //内部类对象可以共用
//    val c1 = new Class
//    val leo = c1.register("leo")
//    c1.students += leo
//    val c2 = new Class
//    val jack = c2.register("jack")
//    c1.students += jack

    //扩大内部类作用域:类型投影
//        val c1 = new Class
//        val leo = c1.register("leo")
//        c1.students += leo
//        val c2 = new Class
//        val jack = c2.register("jack")
//        c1.students += jack

    //内部类获取外部类的引用
    val c1 = new Class("c1")
    val leo = c1.register("leo")
    println(leo.introduceMyself)

  }
}
