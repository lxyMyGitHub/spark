package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.List;

/**
 * @ClassName RDD2DataFreamReflection
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/4 9:23
 * @Version 1.0
 */
public class RDD2DataFreamReflection {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("RDD2DataFreamReflection").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> lines = sc.textFile("d://c_clean/spark/students.txt");
        SQLContext sqlContext = new SQLContext(sc);
        JavaRDD<Student> students = lines.map(new Function<String, Student>() {
            @Override
            public Student call(String line) throws Exception {
                String[] lineSplited = line.split(",");
                Student stu = new Student();
                stu.setId(Integer.parseInt(lineSplited[0]));
                stu.setName(lineSplited[1]);
                stu.setAge(Integer.parseInt(lineSplited[2]));
                return stu;
            }
        });
        //使用反射方式,将RDD转换为DataFrame
        //将Student.class传入进去,其实就是用反射的方式创建DataFrame
        //因为Student.class本身就是反射的一个应用
        //然后底层还得通过对student Class进行反射.来获取其中的field
        DataFrame studentDF = sqlContext.createDataFrame(students, Student.class);
        //拿到了一个DataFrame之后,就可以将其注册为一个零时表,针对其中的数据执行SQL语句
        studentDF.show();
        //查询年龄小于等于18的数据
        studentDF.registerTempTable("students");
        DataFrame teenagerDF = sqlContext.sql("select * from students where age <= 18");
        //将查询出来的DataFrame,再次转换为RDD
        JavaRDD<Row> teenagerRDD = teenagerDF.javaRDD();
        //将RDD中的数据,进行映射,映射为Student
        JavaRDD<Student> teenagerStudentRDD = teenagerRDD.map(new Function<Row, Student>() {
            @Override
            public Student call(Row row) throws Exception {
                int id = row.getInt(1);
                String name = row.getString(2);
                int age = row.getInt(0);
                Student stu = new Student();
                stu.setId(id);
                stu.setName(name);
                stu.setAge(age);
                return stu;
            }
        });
        List<Student> studentList = teenagerStudentRDD.collect();
        for (Student student : studentList) {
            System.out.println(student);
        }

    }



}
