package cn.spark.study.streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * 基于hdfs实时的wordCount
 * @ClassName HDFSWordCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/24 9:09
 * @Version 1.0
 */
public class HDFSWordCount {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("HDFSWordCount");

        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));

        //首先使用JavaStreamingContext的firstStream()方法,针对HDFS目录创建导入数据流
        JavaDStream<String> lines = jssc.textFileStream("hdfs://weekend109:9000/wordcount_dir");
        JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });

        JavaPairDStream<String, Integer> pair = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });

        JavaPairDStream<String, Integer> wordCount = pair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        wordCount.print();


        jssc.start();
        //启动以后等待结束
        jssc.awaitTermination();
        jssc.close();
    }
}
