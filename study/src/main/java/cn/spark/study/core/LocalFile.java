package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.tools.nsc.util.ClassPath;

/**
 * 统计文本文件字数
 * @ClassName LocalFile
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/21 10:01
 * @Version 1.0
 */
public class LocalFile {


    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("LocalFile");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("D://c_clean//spark//spark.txt");
        JavaRDD<Integer> lineLength = lines.map(new Function<String, Integer>() {
            @Override
            public Integer call(String v1) throws Exception {
                return v1.length();
            }
        });
        int count = lineLength.reduce(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        System.out.println("文件总字数是: "+ count);
        sc.close();



    }

}
