package cn.spark.study.core;

import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Accumulator
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/22 17:23
 * @Version 1.0
 */
public class AccumulatorTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("accimulator");
        JavaSparkContext sc = new JavaSparkContext(conf);
        final Accumulator<Integer> sum = sc.accumulator(0);
        List<Integer> numberList = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> numbers = sc.parallelize(numberList);
        numbers.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer v) throws Exception {
                sum.add(v);
            }
        });
        System.out.println(sum.value());
        sc.close();

    }


}
