package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

/**
 * @ClassName DataFrameOperation
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/3 17:59
 * @Version 1.0
 */
public class DataFrameOperation {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("DataFrameOperation");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        //创建dataFrame,完全可以理解为一张表
        DataFrame df = sqlContext.read().json("hdfs://weekend109:9000/students.json");
        //打印所有数据
        df.show();
        //打印元数据(Schema)
        df.printSchema();
        //查询某列所有的数据
        df.select("name").show();
        //查询某几列的数据,并对列进行计算
        df.select(df.col("name"),df.col("age").plus(1)).show();
        //对于某一列的值进行过滤
        df.filter(df.col("age").gt(18)).show();
        //根据某列进行分组,然后进行聚合
        df.groupBy("age").count().show();


    }
}
