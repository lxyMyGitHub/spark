package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * @ClassName Persist
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/22 15:42
 * @Version 1.0
 */
public class Persist {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("persist").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //catch()或者persist()的使用是有规则的
        //必须在transformation或者textFile等创建一个RDD之后,直接连续调用catch()或persist()
        //如果声明RDD然后catch,会报错,大量文件丢失
        JavaRDD<String> lines = sc.textFile("d://c_clean/spark/spark.txt").cache();
        long beginTime = System.currentTimeMillis();
        long sum = lines.count();
        long endTime = System.currentTimeMillis();
        System.out.println(sum);
        System.out.println(endTime-beginTime +" ms");
        beginTime = System.currentTimeMillis();
        sum = lines.count();
        System.out.println(sum);
        endTime = System.currentTimeMillis();
        System.out.println(endTime-beginTime +" ms");


        sc.close();

    }


}
