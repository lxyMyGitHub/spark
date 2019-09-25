package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName RDD2DataFreamProgrammatically
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/4 14:08
 * @Version 1.0
 * 以编程方式动态指定元数据,将RDD转换为DataFrame
 */
public class RDD2DataFreamProgrammatically {
    public static void main(String[] args) {
        //创建SparkConf/JavaSparkContext/SQLContext
        SparkConf conf = new SparkConf().setMaster("local").setAppName("RDD2DataFreamProgrammatically");
        JavaSparkContext sc = new JavaSparkContext(conf);
        SQLContext sqlContext = new SQLContext(sc);
        //第一步,创建一个普通的RDD,但是,必须将其转换为RDD<Row>的这种格式
        JavaRDD<String> lines = sc.textFile("d://c_clean/spark/students.txt");

        JavaRDD<Row> studentRDD = lines.map(new Function<String, Row>() {
            @Override
            public Row call(String line) throws Exception {
                String[] lineSplited = line.split(",");
                //往row中塞数据的时候,要对应数据格式,进行类型转换操作***
                return RowFactory.create(Integer.parseInt(lineSplited[0]),lineSplited[1],Integer.parseInt(lineSplited[2]));
            }
        });
        //第二步,动态构造元数据
        //比如说id,name等,field的名称和类型,可能都是在程序运行过程中,动态从MySQL db里
        //或者配置文件中,加载出来,是不固定饿
        //所以特别适合这种编程方式,来构造元数据
        List<StructField> structFields = new ArrayList<StructField>();
        structFields.add(DataTypes.createStructField("id",DataTypes.IntegerType,true));
        structFields.add(DataTypes.createStructField("name",DataTypes.StringType,true));
        structFields.add(DataTypes.createStructField("age",DataTypes.IntegerType,true));
        StructType structType = DataTypes.createStructType(structFields);

        //第三步,使用动态构造的元数据,将RDD转换为DataFrame
        DataFrame studentDF = sqlContext.createDataFrame(studentRDD,structType);
        studentDF.show();
        studentDF.registerTempTable("students");
        DataFrame teenagerDF = sqlContext.sql("select * from students where age < 18");
        List<Row> teenagerRows = teenagerDF.javaRDD().collect();
        for (Row row : teenagerRows) {
            System.out.println(row);
//            System.out.println(row.get(0)+":"+row.get(1)+":"+row.get(2));
        }


    }
}
