package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.In;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName JDBCDataSource
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/12 11:04
 * @Version 1.0
 */
public class JDBCDataSource {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("JDBCDataSource").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        Map<String,String> options = new HashMap<String,String>();
        options.put("url","jdbc:mysql://weekend109:3306/test");
        options.put("dbtable","student_infos");
        options.put("user","root");
        options.put("password","root");
        DataFrame studentInfosDF = sqlContext.read().format("jdbc").options(options).load();
        studentInfosDF.show();
        options.put("dbtable","student_scores");
        DataFrame studentScoresDF = sqlContext.read().format("jdbc").options(options).load();
        //将俩个dataFream转换为JavaPairRDD,然后执行join操作
        JavaPairRDD<String, Tuple2<Integer, Integer>> studentsRDD = studentInfosDF.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(Row row) throws Exception {
                return new Tuple2<String, Integer>(row.getString(0), Integer.parseInt(String.valueOf(row.get(1))));
            }
        }).join(studentScoresDF.javaRDD().mapToPair(new PairFunction<Row, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(Row row) throws Exception {
                return new Tuple2<String, Integer>(row.getString(0), Integer.parseInt(String.valueOf(row.get(1))));
            }
        }));
        //将JavaPairRDD转换为JavaRDD<Row>
        JavaRDD<Row> studentRowsRDD = studentsRDD.map(new Function<Tuple2<String, Tuple2<Integer, Integer>>, Row>() {
            @Override
            public Row call(Tuple2<String, Tuple2<Integer, Integer>> tuple) throws Exception {
                return RowFactory.create(tuple._1,tuple._2._1,tuple._2._2);
            }
        });
        //过滤出分数大于80分的数据
        JavaRDD<Row> filteredStudentRowsRDD = studentRowsRDD.filter(new Function<Row, Boolean>() {
            @Override
            public Boolean call(Row row) throws Exception {
                if(row.getInt(2)>80){
                    return true;
                }
                return false;
            }
        });
        //转换为DataFrame
        List<StructField> studentFields = new ArrayList<StructField>();
        studentFields.add(DataTypes.createStructField("name", DataTypes.StringType,true));
        studentFields.add(DataTypes.createStructField("age", DataTypes.IntegerType,true));
        studentFields.add(DataTypes.createStructField("score", DataTypes.IntegerType,true));
        StructType structType = DataTypes.createStructType(studentFields);
        DataFrame studentsDF = sqlContext.createDataFrame(filteredStudentRowsRDD,structType);
        Row[] rows = studentsDF.collect();
        for (Row row : rows) {
            System.out.println(row);
        }
        //将dataFream中的数据保存到MySQL表中
        studentsDF.javaRDD().foreach(new VoidFunction<Row>() {
            @Override
            public void call(Row row) throws Exception {
                String sql = "insert into good_student_infos values(" +
                        "'"+ String.valueOf(row.getString(0)) + "'," +
                        Integer.parseInt(String.valueOf(row.get(1)))+"," +
                        Integer.parseInt(String.valueOf(row.get(2))) + ")";
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = null;
                Statement stmt = null;
                try {
                    conn = DriverManager.getConnection("jdbc:mysql://weekend109:3306/test","root","root");
                    stmt = conn.createStatement();
                    stmt.executeUpdate(sql);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(stmt != null){
                        stmt.close();
                    }
                }
            }
        });
    }
}
