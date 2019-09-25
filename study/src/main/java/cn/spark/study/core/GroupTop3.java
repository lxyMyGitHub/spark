package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName GroupTop3
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/23 13:48
 * @Version 1.0
 */
public class GroupTop3 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("groupTop3").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("d://c_clean/spark/score.txt");
        JavaPairRDD<String, Integer> scorePairs = lines.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] lineSplit = s.split(" ");
                return new Tuple2<String, Integer>(lineSplit[0], Integer.valueOf(lineSplit[1]));
            }
        });
        JavaPairRDD<String, Iterable<Integer>> groupPairs = scorePairs.groupByKey();
        JavaPairRDD<String, Iterable<Integer>> classPairsTop3 = groupPairs.mapToPair(new PairFunction<Tuple2<String, Iterable<Integer>>, String, Iterable<Integer>>() {
            @Override
            public Tuple2<String, Iterable<Integer>> call(Tuple2<String, Iterable<Integer>> classScores) throws Exception {
                Integer[] top3 = new Integer[3];
                String className = classScores._1;
                Iterator<Integer> iterator = classScores._2.iterator();
                while (iterator.hasNext()) {
                    Integer score = iterator.next();
                    for (int i = 0; i < 3; i++) {
                        if (top3[i] == null) {
                            top3[i] = score;
                            break;
                        } else if (score > top3[i]) {
                            int tmp = top3[i];
                            top3[i] = score;
                            if (i < top3.length - 1) {
                                top3[i + 1] = tmp;
                            }
                            break;
                        }
                    }
                }

                return new Tuple2<String, Iterable<Integer>>(className, Arrays.asList(top3));
            }
        });
        classPairsTop3.foreach(new VoidFunction<Tuple2<String, Iterable<Integer>>>() {
            @Override
            public void call(Tuple2<String, Iterable<Integer>> t) throws Exception {
                System.out.println("class Name : "+t._1);
                Iterator<Integer> iterator = t._2.iterator();
                while(iterator.hasNext()){
                    System.out.println("score: "+ iterator.next());
                }
                System.out.println("------------------------------------------------");
            }
        });

    }
}
