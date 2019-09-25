package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;

/**
 * @ClassName saveMode
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/10 10:14
 * @Version 1.0
 */
public class saveModeTest {

    public static void main(String[] args) {
        //创建SparkConf/JavaSparkContext/SQLContext
        SparkConf conf = new SparkConf().setMaster("local").setAppName("GenericLoadSave");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        DataFrame peopleDF = sqlContext.read().format("json").load("d://c_clean/spark/people.json");
        peopleDF.show();
        peopleDF.save("d://c_clean/spark/",SaveMode.Append);
    }
}
