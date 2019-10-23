package cn.scala.study.base

class Person (val name: String,val age: Int)
/**
  * unapply方法,姑妈思意,那就是apply反过来
  * apply可以理解为,接受一堆参数,然后返回一个对象
  * unapply方法,可以理解为接收一个字符串,解析成一个对象的各个字段
  * 提取器就是一个包含了unapply对象,跟apply正好相反
  */
object Person {
  def apply(name: String,age: Int) = new Person(name,age)
  def unapply(str: String)={
    val splitIndex = str.indexOf(" ")
    if(splitIndex == -1)None
    else Some((str.substring(0,splitIndex),str.substring(splitIndex+1)))
  }
}


