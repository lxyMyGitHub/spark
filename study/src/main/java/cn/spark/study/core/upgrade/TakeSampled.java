package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName TakeSampled
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:22
 * @Version 1.0
 */
public class TakeSampled {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("TakeSampled").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //准备模拟数据
        List<String> staffList = Arrays.asList("张三","李四","王二","赵武","志林","波波","皮皮","小马","小刘","小张");

        JavaRDD<String> staffRDD = sc.parallelize(staffList, 2);

        //TakeSampled算子
        //与sample算子的不同之处,俩点
        //1.action操作,sample是transformation操作
        //2.不能指定抽取比例,只能抽取几个
        List<String> luckyStaffList = staffRDD.takeSample(false, 3);
        for (String name : luckyStaffList) {
            System.out.println("lucky name is : "+ name);
        }


        sc.close();
    }
}
