package cn.scala.study.base

/**
  * 在执行父类构造函数之前,我先初始化自子类的field
  */
class PEStudent extends  {
  override val classNumber: Int = 3
} with Student
