package cn.scala.study.base

object XMLTest {
  def main(args: Array[String]): Unit = {
    val books1 = <books><book>my first scala book</book></books>
    val books2 = <book>my first scala book</book><book>my first spark book</book>
    println(books1)
    println(books2)
//    可以通过scala.xml.NodeBuffer类型，来手动创建一个节点序列

    val booksBuffer = new scala.xml.NodeBuffer
    booksBuffer += <book>book1</book>
    booksBuffer += <book>book2</book>
    val books: scala.xml.NodeSeq = booksBuffer


//    xml元素的属性

//    scala.xml.Elem.attributes属性，可以返回这儿xml元素的属性，是Seq[scala.xml.Node]类型的，继续调用text属性，可以拿到属性的值

    val book = <book id="1" price="10.0">book1</book>
    val bookId = book.attributes("id").text

//    还可以遍历属性

    for(attr <- book.attributes) println(attr)

//    还可以调用book.attributes.asAttrMap，获取一个属性Map
  }

}
