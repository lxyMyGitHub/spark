package cn.scala.study.base

/**
  * scala跳出循环的三种方法
  * 1.基于boolean类型的控制变量
  * 2.使用嵌套函数以及return
  * 3.使用Breaks类的break方法
  */
object BreakRepeat {
  def main(args: Array[String]): Unit = {
    //1.基于boolean类型的控制变量
//    function1()
//    function11()
    //2.使用嵌套函数以及return
    function2()
    //3.使用Breaks类的break方法
    function3()
  }

  /**
    * 基于boolean类型的控制变量
    */
  def function1()={
    println("基于boolean类型的控制变量")
    var flag = true
    var res = 0;
    var n = 0;
    while (flag){
      res += n
      n += 1
      if(n == 5){
        flag = false
      }
    }
    println("res is : "+res + " n is :" + n)
  }


  def function11()={
    println("基于boolean类型的控制变量")
    var flag = true
    var res = 0;
    var n = 0;
    //高级for循环,加上了 if 守卫
    for(i <- 0 until 10 if flag){
      res += i
      if(i == 4) flag = false
    }
    println("res is : "+res + " n is :" + n)
  }

  /**
    * 使用嵌套函数以及return
    */
  def function2()={
    println("使用嵌套函数以及return")
    var res = 0

    def add_inner(): Unit ={
      for (i <- 0 until 10){
        if(i == 5){
          return
        }
        res += i
      }
    }
    add_inner()
    println("res is : " +res)
  }

  /**
    * 使用Breaks类的break方法
  */
  def function3()={
    println("使用Breaks类的break方法")
    import scala.util.control.Breaks._
    var res = 0
    breakable{
      for(i <- 0 until 10){
        if(i == 5){
          break;
        }
        res += i
      }
    }
    println("res is : " + res)
  }
}
