package cn.scala.study.base

/**
  * 偏函数
  */
object PartialFunctionTest {
  def main(args: Array[String]): Unit = {

    print(getStudentGrade("Leo"))
    print(getStudentGrade.isDefinedAt("Tom"))
    print(getStudentGrade.isDefinedAt("klio"))

  }

  //一半函数
  def getStudent(name: String){}

  //偏函数
  val getStudentGrade: PartialFunction[String,Int] = {
    case "Leo" => 90;
    case "Tom" => 45;
    case "Jack" => 66
  }

}
