package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.List;


/**
 * @ClassName ParquteLoadData
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/10 10:34
 * @Version 1.0
 */
public class ParquetLoadData {
    public static void main(String[] args) {
        //创建SparkConf/JavaSparkContext/SQLContext
        SparkConf conf = new SparkConf().setAppName("ParquetLoadData");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        //读取Parquet文件数据,创建一个DataFrame
        DataFrame usersDF = sqlContext.read().parquet("hdfs://weekend109:9000/spark-study/users.parquet");
        //将dataFrame注册为临时表,使用SQL查询数据
        usersDF.registerTempTable("users");
        DataFrame userNameDF = sqlContext.sql("select name from users");
        List<String> userName = userNameDF.javaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) throws Exception {
                return "name:"+row.getString(0);
            }
        }).collect();

        for (String name : userName) {
            System.out.println("result" + name);
        }
    }


}
