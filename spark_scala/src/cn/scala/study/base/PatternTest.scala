package cn.scala.study.base

/**
  * 正则表达式
  * 一种语法,用一个表达式来匹配一系列的字符串
  * [a-z]+  : 表示一个或多个小写字母范围的26个小写英文字母,比如hello
  */
object PatternTest {
  def main(args: Array[String]): Unit = {
    val pattern1 = "[a-z]+".r

    val str = "hello 123 world 456"

    //获取一个字符串中,匹配正则表达式的部分,使用findAllIn,会获取到一个Iterator,迭代器
    //然后就可以遍历各个匹配正则的部分,去进行处理
    for (matchString <- pattern1.findAllIn(str)) println(matchString)

    //findFirstIn ,可以获取第一个匹配正则表达式的部分
    println(pattern1.findAllIn(str))
    println(str)
    //replaceAllIn,将匹配正则的部分替换掉
    println(pattern1.replaceAllIn("hello world","replace"))

    //replaceFirstIn,可以将第一个匹配正则的地方,替换掉
    println(pattern1.replaceFirstIn("hello world","replace"))
  }
}
