package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Union
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:34
 * @Version 1.0
 */
public class Union {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Union").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //union算子
        //将俩个RDD合并为一个RDD

        //准备模拟数据
        List<String> department1StaffList = Arrays.asList("张三","李四","王二","赵武","小张");
        JavaRDD<String> department1StaffRDD = sc.parallelize(department1StaffList);
        List<String> department2StaffList = Arrays.asList("熙悦","窒息","周武","武器","小张");
        JavaRDD<String> department2StaffRDD = sc.parallelize(department2StaffList);
        JavaRDD<String> departmentStaffRDD = department1StaffRDD.union(department2StaffRDD);
        for (String staff : departmentStaffRDD.collect()) {
            System.out.println(staff);
        }
        sc.close();
    }
}
