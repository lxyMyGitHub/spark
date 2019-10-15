package cn.scala.study.base

//import cn.scala.study.base.Class.Student

import scala.collection.mutable.ArrayBuffer

//object Class{
//  class Student(val name: String){}
//}

//class Class {
//
//
//  val students = new ArrayBuffer[Class.Student]
//  def register(name: String)={
//    new Class.Student(name)
//  }
//}
//class Class {
//
//  class Student(val name: String){}
//  val students = new ArrayBuffer[Class#Student]
//  def register(name: String)={
//    new Student(name)
//  }
//}

class Class(val name: String) {outer =>
  class Student(val name: String){
    def introduceMyself ="hello ,i'm" + name + ",I'm very happy to join class " + outer.name
  }
  def register(name: String)={
    new Student(name)
  }
}

