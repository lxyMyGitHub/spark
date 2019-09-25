package cn.spark.study.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object TransformBlacklist {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("UpdateStateByKeyWordCount")
    val ssc = new StreamingContext(conf,Seconds(5))
    val blacklist = Array(("tom",true))
    val blacklistRDD = ssc.sparkContext.parallelize(blacklist)
    val adsClickLogDStream = ssc.socketTextStream("weekend109",9999)
    val userAdsClickLogDStream = adsClickLogDStream.map(adsClickLog => (adsClickLog.split(" ")(1),adsClickLog))
    val validAdsClickLogDStream = userAdsClickLogDStream.transform(userAdsClickLogRDD => {
      val joinedRDD = userAdsClickLogRDD.leftOuterJoin(blacklistRDD)
      val filterRDD = joinedRDD.filter(tuple => {
        if(tuple._2._2.getOrElse(false)){
          false
        }else {
          true
        }
      })
      filterRDD.map(tuple => tuple._2._1)
    })
    validAdsClickLogDStream.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
