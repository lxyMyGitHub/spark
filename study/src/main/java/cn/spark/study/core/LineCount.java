package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Int;
import scala.Tuple2;

/**
 *
 * @ClassName LineCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/21 11:00
 * @Version 1.0
 */
public class LineCount {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf().setAppName("LineCount").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("D://c_clean//spark/spark.txt");
        JavaPairRDD<String, Integer> pairs = lines.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String t) throws Exception {
                return new Tuple2<String, Integer>(t,1);
            }
        });
        JavaPairRDD<String, Integer> lineCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });

        lineCount.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> values) throws Exception {
                System.out.println(values._1 + " : " + values._2);
            }
        });


        sc.close();


    }
}
