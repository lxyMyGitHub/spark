package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

/**
 * @ClassName SecondarySort
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/23 9:52
 * @Version 1.0
 */
public class SecondarySort {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("SecondarySort");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("d://c_clean/spark/sort.txt");
        JavaPairRDD<SecondarySortKey,String> pairs = lines.mapToPair(new PairFunction<String, SecondarySortKey, String>() {
            @Override
            public Tuple2<SecondarySortKey, String> call(String line) throws Exception {
                String[] lineSplit = line.split(" ");
                SecondarySortKey key = new SecondarySortKey(Integer.valueOf(lineSplit[0]), Integer.valueOf(lineSplit[1]));
                return new Tuple2<SecondarySortKey, String>(key,line);
            }
        });
        JavaPairRDD<SecondarySortKey,String> sortKeys = pairs.sortByKey();

        JavaRDD<String> sortLine = sortKeys.map(new Function<Tuple2<SecondarySortKey, String>, String>() {
            @Override
            public String call(Tuple2<SecondarySortKey, String> t) throws Exception {
                return t._2;
            }
        });
        sortLine.foreach(new VoidFunction<String>() {
            @Override
            public void call(String s) throws Exception {
                System.out.println(s);
            }
        });

        sc.close();
    }
}
