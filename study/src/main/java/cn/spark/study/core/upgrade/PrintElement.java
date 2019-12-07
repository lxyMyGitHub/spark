package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @ClassName PlusClosureVariable
 * @Deseription 闭包
 * @Author lxy_m
 * @Date 2019/12/2 14:19
 * @Version 1.0
 */
public class PrintElement {
    public static void main(String[] args)
    {
        SparkConf conf = new SparkConf().setAppName("PrintElement").setMaster("local").set("spark.default.parallelism","2");
//        SparkConf conf = new SparkConf().setAppName("PrintElement"). setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<Integer> numbers = Arrays.asList(1,2,3,4,5);
        JavaRDD<Integer> numbersRDD = sc.parallelize(numbers);

        numbersRDD.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer num) throws Exception {

                System.out.println("===========================num : "+ num +"==========================");
            }
        });


        sc.close();
    }

}
