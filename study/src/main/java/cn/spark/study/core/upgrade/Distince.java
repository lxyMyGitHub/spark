package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Intersection
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:41
 * @Version 1.0
 */
public class Intersection {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Intersection").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //intersection
        //用来获取量个rdd中相同的数据
        //举例:找出俩个部门中的交集
        //准备模拟数据
        List<String> department1StaffList = Arrays.asList("张三","李四","王二","赵武","小张");
        JavaRDD<String> department1StaffRDD = sc.parallelize(department1StaffList);
        List<String> department2StaffList = Arrays.asList("熙悦","窒息","周武","武器","小张");
        JavaRDD<String> department2StaffRDD = sc.parallelize(department2StaffList);
        JavaRDD<String> staffIntersectionRDD = department1StaffRDD.intersection(department2StaffRDD);
        for (String staff : staffIntersectionRDD.collect()) {
            System.out.println(staff);
        }


        sc.close();
    }
}
