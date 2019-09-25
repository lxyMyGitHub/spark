package cn.spark.study.core;

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
 * @ClassName WordCountLocal
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/20 15:25
 * @Version 1.0
 * 本地测试的Wordcount程序
 */

public class WordCountCluster {
    public static void main(String[] args) throws Exception{
        //在spark集群中运行,需要修改俩个地方
        //第一:将sparkConf的setMaster()方法给删掉,默认会自动链接
        //第二,我们针对的不是本地文件了,修改为hadoop hdfs上的真正的存储大数据的文件


        SparkConf conf = new SparkConf().setAppName("WordCountCluster");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> lines = sc.textFile("hdfs://weekend109:9000/spark.txt");
        @SuppressWarnings(value = "unused")
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });
        @SuppressWarnings(value = "unused")
        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word,1);
            }
        });

        JavaPairRDD<String,Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
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
