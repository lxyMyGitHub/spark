package cn.spark.study.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WindowHotWord {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("WindowHotWord")
    val ssc = new StreamingContext(conf,Seconds(2))

    val searchLogDStream = ssc.socketTextStream("weekend109",9999)
    val searchWordDStream = searchLogDStream.map(searchLog => searchLog.split(" ")(1))
    val searchWordPairDStream = searchWordDStream.map(searchWord => (searchWord,1))
    val searchWordPairCountDStream = searchWordPairDStream.reduceByKeyAndWindow((v1: Int,v2: Int) => v1 + v2,Seconds(60),Seconds(10))
    val finalDStream = searchWordPairCountDStream.transform(searchWordCountsRDD =>{
      val countSearchWordsRDD = searchWordCountsRDD.map(tuple => (tuple._2,tuple._1))
      val sortedCountSearchWordsRDD = countSearchWordsRDD.sortByKey(false)
      val sortedSearchWordCountsRDD= sortedCountSearchWordsRDD.map(tuple => (tuple._2,tuple._1))
      val hotSearchWordCounts = sortedSearchWordCountsRDD.take(3)
      for(wordCount <- hotSearchWordCounts){
        println(wordCount._1 +" : " + wordCount._2)
      }
      searchWordCountsRDD
    })
    finalDStream.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
