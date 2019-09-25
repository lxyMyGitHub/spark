package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName BroadcastVariable
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/22 17:01
 * @Version 1.0
 */
public class BroadcastVariable {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("BroadcastVariable");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //创建共享变量
        final int factor = 3;
        final Broadcast<Integer> factorBroadcast = sc.broadcast(factor);

        List<Integer> list = Arrays.asList(1,2,3,4,5);
        JavaRDD<Integer> numbers = sc.parallelize(list);
        JavaRDD<Integer> nums = numbers.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v) throws Exception {
                //使用共享变量
                int factor = factorBroadcast.value();
                return v * factor;
            }
        });
        nums.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer t) throws Exception {
                System.out.println(t);
            }
        });
        sc.close();


    }
}
