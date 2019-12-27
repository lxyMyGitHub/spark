package cn.spark.study.core.upgrade.applog;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.List;

/**
 * @ClassName AppLogSpark
 * 移动端APP访问流量日志分析案例
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/25 19:51
 * @Version 1.0
 */
public class AppLogSpark {
    public static void main(String[] args) throws Exception{
        SparkConf conf = new SparkConf().setMaster("local").setAppName("AppLogSpark");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //读取日志文件,并创建一个RDD
        //使用textFile方法读取本地磁盘文化或者HDFS上的文件
        //创建一个初始RDD
        JavaRDD<String> accessLogRDD = sc.textFile("D:\\c_clean\\spark\\access.log");

        //将RDD映射为K-V形式,为后面的reduceByKey做准备
        JavaPairRDD<String, AccessLogInfo> accessLogPairRDD = mapAccessLogRDD2Pair(accessLogRDD);
        //根据deviceID进行聚合操作
        //获取每个deviceID的总上行流量总下行流量,最早访问时间戳
        JavaPairRDD<String, AccessLogInfo> aggrAccessLogPairRDD = aggregateByDeviceID(accessLogPairRDD);

        //按deviceID聚合的RDD的key映射为二次排序key,value映射为deviceID

        JavaPairRDD<AccessLogSortKey, String> sortKeyPairRDD = mapRDD2SortKey(aggrAccessLogPairRDD);
        //使用自定义二次排序器,按照上行流量,下行流量以及世界戳进行倒序排序
        JavaPairRDD<AccessLogSortKey, String> sortRDD = sortKeyPairRDD.sortByKey(false);
        //获取top10数据
        List<Tuple2<AccessLogSortKey, String>> top10List = sortRDD.take(10);
        for (Tuple2<AccessLogSortKey, String> data : top10List) {
            System.out.println(data._2 + " : " + data._1);
        }
        sc.close();
    }

    private static JavaPairRDD<String,AccessLogInfo> mapAccessLogRDD2Pair(JavaRDD<String> accessLogRDD){

        return accessLogRDD.mapToPair(new PairFunction<String, String, AccessLogInfo>() {
            @Override
            public Tuple2<String, AccessLogInfo> call(String accessLog) throws Exception {
                //根据\t对日志进行切分
                String[] accessLogSplited = accessLog.split("\t");
                //获取四个字段
                long timestamp = Long.valueOf(accessLogSplited[0]);
                String deviceID = accessLogSplited[1];
                long upTraffic = Long.valueOf(accessLogSplited[2]);
                long downTraffic = Long.valueOf(accessLogSplited[3]);
                //将时间戳上行流量,下行流量封装为自定义的可序列化对象
                AccessLogInfo accessLogInfo = new AccessLogInfo(timestamp,upTraffic,downTraffic);
                return new Tuple2<String,AccessLogInfo>(deviceID,accessLogInfo);
            }
        });
    }

    /**
     * 根据deviceID进行聚合操作
     * 计算出每个deviceID的总上行流量和总下行流量
     * @param accessLogPairRDD
     * @return
     */
    private static JavaPairRDD<String,AccessLogInfo> aggregateByDeviceID(
            JavaPairRDD<String,AccessLogInfo> accessLogPairRDD){
        return accessLogPairRDD.reduceByKey(new Function2<AccessLogInfo, AccessLogInfo, AccessLogInfo>() {
            @Override
            public AccessLogInfo call(AccessLogInfo accessLogInfo1, AccessLogInfo accessLogInfo2) throws Exception {
                long timestamp = accessLogInfo1.getTimestamp() < accessLogInfo2.getTimestamp() ?
                        accessLogInfo1.getTimestamp() : accessLogInfo2.getTimestamp();
                long upTraffic = accessLogInfo1.getUpTraffic() + accessLogInfo2.getUpTraffic();
                long downTraffic = accessLogInfo1.getDownTraffic() + accessLogInfo2.getDownTraffic();
                AccessLogInfo accessLogInfo = new AccessLogInfo(timestamp,upTraffic,downTraffic);
                return accessLogInfo;
            }
        });
    }

    /**
     * 将RDD的key映射为二次排序的key
     * @param aggrAccessLogPairRDD
     * @return
     */
    private static JavaPairRDD<AccessLogSortKey,String> mapRDD2SortKey(JavaPairRDD<String,AccessLogInfo> aggrAccessLogPairRDD){
        return aggrAccessLogPairRDD.mapToPair(new PairFunction<Tuple2<String, AccessLogInfo>, AccessLogSortKey, String>() {
            @Override
            public Tuple2<AccessLogSortKey, String> call(Tuple2<String, AccessLogInfo> tuple) throws Exception {
                String deviceID = tuple._1;
                AccessLogInfo accessLogInfo = tuple._2;
                //将日志信息封装为二次排序key
                AccessLogSortKey sortKey = new AccessLogSortKey(accessLogInfo.getTimestamp(),
                        accessLogInfo.getUpTraffic(),
                        accessLogInfo.getDownTraffic());

                return new Tuple2<AccessLogSortKey, String>(sortKey,deviceID);
            }
        });
    }

}
