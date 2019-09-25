package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

import java.util.Arrays;
import java.util.List;

/**
 * 并行化集合创建RDD
 * @ClassName ParallelizeCollection
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/21 9:41
 * @Version 1.0
 */
public class ParallelizeCollection {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("ParallelizeCollection");
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
        int sum = numberRDD.reduce(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer num1, Integer num2) throws Exception {
                return num1 + num2;
            }
        });
        System.out.println("the sum 1 to 10 add is : "+sum);
        //关闭JavaSparkContext
        sc.close();
    }
}
