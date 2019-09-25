package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName JSONDataSource
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/10 16:06
 * @Version 1.0
 */
public class JSONDataSource {
    public static void main(String[] args) {
        //创建SparkConf/JavaSparkContext/SQLContext
        SparkConf conf = new SparkConf().setMaster("local").setAppName("JSONDataSource");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        //针对JSON文件创建DataFrame
        DataFrame studentsScoresDF = sqlContext.read().json("hdfs://weekend109:9000/spark-study/students.json");
        //针对学生的成绩信息的DataFrame,注册临时表,查询分数大于80分的学生的姓名
        studentsScoresDF.registerTempTable("student_scores");
        DataFrame goodsStudentNamesDF = sqlContext.sql("select name,score from student_scores where score>=80");
        List<String> goodsStudentNames = goodsStudentNamesDF.javaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) throws Exception {
                return row.getString(0);
            }
        }).collect();
        //然后针对JavaRDD<String>,创建DataFrame
        List<String> studentInfoJSONs = new ArrayList<String>();
        studentInfoJSONs.add("{\"name\":\"Leo\",\"age\":18}");
        studentInfoJSONs.add("{\"name\":\"Jack\",\"age\":19}");
        studentInfoJSONs.add("{\"name\":\"Marry\",\"age\":17}");
        JavaRDD<String> studentsInfoJSONsRDD = sc.parallelize(studentInfoJSONs);
        DataFrame studentInfoDF = sqlContext.read().json(studentsInfoJSONsRDD);
        //针对学生基本信息DataFrame,注册零时表,然后查询分数大于80分的学生的基本信息
        studentInfoDF.registerTempTable("student_infos");
        String sql ="select name,age from student_infos where name in (";
        for (int i = 0;i<goodsStudentNames.size();i++) {
            sql += "'"+goodsStudentNames.get(i)+"'";
            if(i<goodsStudentNames.size()-1){
                sql +=",";
            }
        }
        sql += ")";
        DataFrame goodStudentInfosDF = sqlContext.sql(sql);
        //将俩份数据的DataFream转换为JavaPairRDD,执行Join transformation
        JavaPairRDD<String,Tuple2<Integer, Integer>> goodsStudentsRDD = goodsStudentNamesDF.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(Row row) throws Exception {
                return new Tuple2<String,Integer>(row.getString(0),Integer.parseInt(String.valueOf(row.getLong(1))));
            }
        }).join(goodStudentInfosDF.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(Row row) throws Exception {
                return new Tuple2<String, Integer>(row.getString(0),Integer.parseInt(String.valueOf(row.getLong(1))));
            }
        }));
        //然后将封装在RDD中的好学生的全部信息,转换为一个JavaRDD<Row>的格式
        JavaRDD<Row> goodsStudentRowsRDD = goodsStudentsRDD.map(new Function<Tuple2<String, Tuple2<Integer, Integer>>, Row>() {
            @Override
            public Row call(Tuple2<String, Tuple2<Integer, Integer>> tuple) throws Exception {
                return RowFactory.create(tuple._1,tuple._2._1,tuple._2._2);
            }
        });
        //创建一份元数据,将JavaRDD<Row>转换为DataFrame
        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField("name",DataTypes.StringType,true));
        structFields.add(DataTypes.createStructField("score",DataTypes.IntegerType,true));
        structFields.add(DataTypes.createStructField("age",DataTypes.IntegerType,true));
        StructType structType = DataTypes.createStructType(structFields);
        DataFrame goodStudentDF = sqlContext.createDataFrame(goodsStudentRowsRDD,structType);
        //将好学生的全部信息保存到一个JSON文件中去
        goodStudentDF.write().format("json").save("hdfs://weekend109:9000/spark-study/goodStudents");

    }
}
