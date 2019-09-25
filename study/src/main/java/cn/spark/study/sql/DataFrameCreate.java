package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

/**
 * @ClassName DataFrameCreate
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/3 16:44
 * @Version 1.0
 * 创建DataFrame
 */
public class DataFrameCreate {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("DataFrameCreate");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        //创建dataFrame,完全可以理解为一张表
        DataFrame df = sqlContext.read().json("hdfs://weekend109:9000/students.json");
        //打印所有数据
        df.show();



    }
}
