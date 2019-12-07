package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName Repartition
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:22
 * @Version 1.0
 */
public class Repartition {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Repartition").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //Repartition算子
        //用于将任意RDD的partition增多,或者减少
        //与coalesce不同之处在于,coalesce仅仅是能将RDD的partition减少
        //但是repairtition可以将RDD的partition变多
        //有些时候,可能自动设置的partition数量过于少了,导致我们后面的算子的运行特别慢
        //此时就可以在Spark SQL加载hive数据到RDD中以后
        //立即使用repartition算子,将RDD数量变多

        //案例
        //公司增加新部门,但是人员有限,将人员分配到更多的部门中去
        //准备模拟数据
        List<String> staffList = Arrays.asList("张三","李四","王二","赵武","志林","波波","皮皮","小马","小刘","小张");

        JavaRDD<String> staffRDD = sc.parallelize(staffList, 3);
        JavaRDD<String> staffRDD2 = staffRDD.mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
            @Override
            public Iterator<String> call(Integer index, Iterator<String> names) throws Exception {
                List<String> list = new ArrayList<String>();
                while (names.hasNext()) {
                    String staff = names.next();
                    list.add("部门"+index+";姓名:"+staff);
//                    System.out.println( "第一次:" +(index+1) + " : " + staff);
                }
                return list.iterator();
            }
        }, true);
        for (String staff : staffRDD2.collect()) {
            System.out.println(staff);
        }
        JavaRDD<String> staffRDD3 = staffRDD2.repartition(6);
        JavaRDD<String> staffRDD4 = staffRDD3.mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
            @Override
            public Iterator<String> call(Integer index, Iterator<String> names) throws Exception {
                List<String> list = new ArrayList<String>();
                while (names.hasNext()) {
                    String staff = names.next();
                    list.add("部门"+index+";姓名:"+staff);
//                    System.out.println("第二次:"+(index+1) + " : " + staff);
                }
                return list.iterator();
            }
        }, true);
        for (String staff : staffRDD4.collect()) {
            System.out.println(staff);
        }
        sc.close();
    }
}
