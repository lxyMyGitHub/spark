package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;

/**
 * Hive数据源
 * @ClassName HiveDataSource
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/11 19:10
 * @Version 1.0
 */
public class HiveDataSource {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf().setAppName("HiveDataSource");
        JavaSparkContext sc = new JavaSparkContext(conf);
        HiveContext hiveContext = new HiveContext(sc.sc());
        //将学生基本信息数据导入student_infos表
        //判断是否存在student_infos表,如果存在则删除
        hiveContext.sql("DROP TABLE IF EXISTS  student_infos");
        //创建该表
        hiveContext.sql("CREATE TABLE IF NOT EXISTS  student_infos(name STRING,age INT)");
        //将学生基本信息数据导入到student_infos表
        hiveContext.sql("LOAD DATA LOCAL INPATH '/home/hadoop/app/study/resources/student_infos.txt' INTO TABLE student_infos");

        hiveContext.sql("DROP TABLE IF EXISTS  student_scores");
        //创建该表
        hiveContext.sql("CREATE TABLE IF NOT EXISTS  student_scores(name STRING,score INT)");
        //将学生基本信息数据导入到student_infos表
        hiveContext.sql("LOAD DATA LOCAL INPATH '/home/hadoop/app/study/resources/student_scores.txt' INTO TABLE student_scores");



        //执行SQL查询,关联俩张表
        DataFrame goodsStudentsDF = hiveContext.sql("select si.name,si.age,ss.score " +
                " from student_infos si " +
                " JOIN student_scores ss on si.name=ss.name " +
                " where ss.score>80 ");
        //将DataFrame中的数据保存到good_student_infos中
        hiveContext.sql("DROP TABLE IF EXISTS  good_student_infos");
        goodsStudentsDF.saveAsTable("good_student_infos");
        //然后针对good_student_infos直接创建DataFrame
        Row[] goofStudentRows = hiveContext.table("good_student_infos").collect();
        for (Row row : goofStudentRows) {
            System.out.println(row);
        }

        sc.close();


    }


}
