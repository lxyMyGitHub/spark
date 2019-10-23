package cn.scala.study.base
//package com{
//  package study{
//    class Test {
//    }
//  }
//}
object Test {
    def main(args: Array[String]): Unit = {
//        str2objTest
        casePersonTest
    }

    private  def casePersonTest()={
        val p = PersonCase("Tom",22)
        p match {
            case PersonCase(name,age) => println(name+ ": " + age)
        }
    }
    //类提取器
    private def str2objTest = {
        val Person(name, age) = "leo 25"
        println(name)
        println(age)
    }
}

/**
  *注解
  */
//Scala中开发注解

//要自己动手开发一个注解，就必须扩展Annotation trait，比如

//class Test extends annotation.Annotation
//
//@Test
//class myTest
/**
  * 注解的参数
  *
  * 注解中，是可以有参数的，比如
  *
  * class Test(var timeout: Int) extends annotation.Annotation
  *
  * @Test(timeout = 100) class myTest
  * 如果注解的参数是value的话，那么也可以不用指定注解的参数名，比如
  * class Test(var value: String) extends annotation.Annotation
  */