package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.List;

/**
 * @ClassName Top3
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/23 10:56
 * @Version 1.0
 */
public class Top3 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("top3").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("d://c_clean/spark/top.txt");
        JavaPairRDD<Integer,String> numbers = lines.mapToPair(new PairFunction<String, Integer, String>() {
            @Override
            public Tuple2<Integer, String> call(String s) throws Exception {
                return new Tuple2<Integer, String>(Integer.valueOf(s),s);
            }
        });
        JavaPairRDD<Integer, String> sortPairs = numbers.sortByKey(false);
        JavaRDD<Integer> sortedNumbers = sortPairs.map(new Function<Tuple2<Integer, String>, Integer>() {
            @Override
            public Integer call(Tuple2<Integer, String> t) throws Exception {
                return t._1;
            }
        });
        List<Integer> sortedNumberList = sortedNumbers.take(3);
        for (Integer i : sortedNumberList) {
            System.out.println(i);
        }
        sc.close();
    }


}
