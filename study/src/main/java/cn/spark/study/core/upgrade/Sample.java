package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Sample
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:22
 * @Version 1.0
 */
public class Sample {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Sample").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //准备模拟数据
        List<String> staffList = Arrays.asList("张三","李四","王二","赵武","志林","波波","皮皮","小马","小刘","小张");

        JavaRDD<String> staffRDD = sc.parallelize(staffList, 2);

        //sample算子
        //可以使用指定的比例,比如说0.1或者0.9,从RDD中随机抽取10%或者90%的数据
        //从RDD中随机抽取数据的功能
        JavaRDD<String> luckyStaffRDD = staffRDD.sample(false, 0.1);
        for (String staff : luckyStaffRDD.collect()) {
            System.out.println(staff);
        }


        sc.close();
    }
}
