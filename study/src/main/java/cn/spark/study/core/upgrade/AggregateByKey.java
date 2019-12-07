package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;

/**
 * @ClassName AggregateByKey
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/20 15:25
 * @Version 1.0
 * 本地测试的Wordcount程序
 */
public class AggregateByKey {
    public static <serialVersionUid> void main(String[] args) throws Exception{
        SparkConf conf = new SparkConf().setAppName("AggregateByKey").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> lines = sc.textFile("D:\\c_clean\\spark\\hello.txt");

        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });
        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word,1);
            }
        });

        //aggregateByKey分为三个参数
        //reduceByKey认为是aggregateByKey的简化版
        //aggregateByKey最重要的一点是,多提供了一个函数,Seq Function
        //就是说可以自己控制如何对每个patition中的数据进行先聚合,类似于mapreduce中的map-side combine
        //然后才是对所有partition中的数据进行全局聚合

        //param1 : 每个key的初始值
        //param2 : 一个函数,Seq Function 如何进行shuffle map-site的本地聚合
        //param3 : 一个函数,Combiner Function ,如何进行shuffle区安 reduce-site的全局聚合
        JavaPairRDD<String,Integer> wordCounts = pairs.aggregateByKey(0, new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        }, new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1+v2;
            }
        });

        wordCounts.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> wordCount) throws Exception {
                System.out.println(wordCount._1 + "  appeared  "  + wordCount._2 + "  times ");
            }
        });

        sc.close();
    }
}
