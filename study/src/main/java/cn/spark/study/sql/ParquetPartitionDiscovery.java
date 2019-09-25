package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

/**
 * @ClassName ParquetPartitionDiscovery
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/10 11:13
 * @Version 1.0
 */
public class ParquetPartitionDiscovery {
    public static void main(String[] args) {
        //创建SparkConf/JavaSparkContext/SQLContext
        SparkConf conf = new SparkConf().setAppName("ParquetPartitionDiscovery");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        //读取Parquet文件数据,创建一个DataFrame
        DataFrame usersDF = sqlContext.read().parquet("hdfs://weekend109:9000/spark-study/users/gender=male/country=US/users.parquet");
        usersDF.printSchema();
        usersDF.show();
    }
}
