package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

/**
 * @ClassName GenericLoadSave
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/9 20:04
 * @Version 1.0
 * 通用的load和save操作
 */
public class GenericLoadSave {
    public static void main(String[] args) {
        //创建SparkConf/JavaSparkContext/SQLContext
        SparkConf conf = new SparkConf().setMaster("local").setAppName("GenericLoadSave");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        DataFrame userDF = sqlContext.read().load("d://c_clean/spark/users.parquet");
        userDF.printSchema();
        userDF.select("name","favorite_color").write().save("d://c_clean/spark/nameAndColors.parquet");
        userDF.show();
    }
}
