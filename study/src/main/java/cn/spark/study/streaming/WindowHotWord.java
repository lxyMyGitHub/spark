package cn.spark.study.streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.List;

/**
 * 滑动窗口,热点搜索词实时统计(每N秒统计一次)
 * @ClassName WindowHotWord
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/25 16:50
 * @Version 1.0
 */
public class WindowHotWord {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("WindowHotWord");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        //日志格式 leo hello      tom hello
        JavaReceiverInputDStream<String> searchLogDStream = jssc.socketTextStream("weekend109",9999);
        //将搜索日志转换成只有一个搜索词即可
        JavaDStream<String> searchWordDStream = searchLogDStream.map(new Function<String, String>() {
            @Override
            public String call(String searchLog) throws Exception {
                return searchLog.split(" ")[1];
            }
        });
        //将搜索词映射为(searchWord,1)的格式
        //第二个参数 : 窗口间隔
        //第三个参数 : 时间间隔
        //每隔10秒统计前60秒的数据
        JavaPairDStream<String,Integer>  searchWordPairDStream = searchWordDStream.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String searchWord) throws Exception {
                return new Tuple2<String, Integer>(searchWord,1);
            }
        });
        //执行滑动窗口操作
        JavaPairDStream<String, Integer> searchWordPairCountDStream = searchWordPairDStream.reduceByKeyAndWindow(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        }, Durations.seconds(60), Durations.seconds(10));
        //执行transform操作,因为,一个窗口,就是一个60秒的数据,会变成一个RDD,然后,对着一个RDD
        //根据每隔搜索词出现的频率进行排序,然后获取排名前三的热点搜索词
        JavaPairDStream<String, Integer> finalDStream = searchWordPairCountDStream.transformToPair(new Function<JavaPairRDD<String, Integer>, JavaPairRDD<String, Integer>>() {
            @Override
            public JavaPairRDD<String, Integer> call(JavaPairRDD<String, Integer> searchWordCountsRDD) throws Exception {
                JavaPairRDD<Integer,String> countSearchWordsRDD = searchWordCountsRDD.mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
                    @Override
                    public Tuple2<Integer, String> call(Tuple2<String, Integer> tuple) throws Exception {
                        return new Tuple2<Integer, String>(tuple._2,tuple._1);
                    }
                });
                JavaPairRDD<Integer, String> sortedCountSearchWordsRDD = countSearchWordsRDD.sortByKey(false);
                //再次执行反正,编程searchWord,count这种格式
                JavaPairRDD<String, Integer> sortedSearchWordCountsRDD = sortedCountSearchWordsRDD.mapToPair(new PairFunction<Tuple2<Integer, String>, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(Tuple2<Integer, String> tuple) throws Exception {
                        return new Tuple2<String, Integer>(tuple._2, tuple._1);
                    }
                });
                //然后用take(),获取排名前三的热点的搜索词
                List<Tuple2<String, Integer>> hotSearchWordCounts = sortedSearchWordCountsRDD.take(3);
                for (Tuple2<String, Integer> wordCount : hotSearchWordCounts) {
                    System.out.println(wordCount._1+" : " + wordCount._2);
                }
                return searchWordCountsRDD;
            }
        });
        //这个无关紧要,只是为了出发Job执行,所以必须有output操作
        finalDStream.print();
        jssc.start();
        jssc.awaitTermination();
        jssc.close();
    }
}
