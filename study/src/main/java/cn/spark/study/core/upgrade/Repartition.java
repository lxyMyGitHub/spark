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
 * @ClassName Coalesce
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:22
 * @Version 1.0
 */
public class Coalesce {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Coalesce").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);


        //coalesce算子
        //功能是将RDD的partition缩减,减少
        //将一定量的数据,压缩到更少的partition中去
        //建议的使用场景,配合filter算子使用
        //使用filter算子过滤掉很多数据以后,比如30%的数据,出现了很多partition中的数据不均匀的情况
        //此时建议使用coalesce算子,压缩partition数量
        //从而让各个partition中的数据都更加紧凑
        //公司原来有6个部门,公司裁员,有的人员离职,部门人员不均匀
        //进行部门整合操作,让每个部门分配均匀的人员
        //准备模拟数据
        List<String> staffList = Arrays.asList("张三","李四","王二","赵武","志林","波波","皮皮","小马","小刘","小张");

        JavaRDD<String> staffRDD = sc.parallelize(staffList, 6);
        JavaRDD<String> staffRDD2 = staffRDD.mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
            @Override
            public Iterator<String> call(Integer index, Iterator<String> names) throws Exception {
                List<String> list = new ArrayList<String>();
                while (names.hasNext()) {
                    String staff = names.next();
                    list.add(staff);
                    System.out.println("第一次:"+(index+1) + " : " + staff);
                }
                return list.iterator();
            }
        }, true);
//        staffRDD2.count();
        JavaRDD<String> staffRDD3 = staffRDD2.coalesce(3);
        System.out.println("---------------------------------------------------------");
        JavaRDD<String> staffRDD4 = staffRDD3.mapPartitionsWithIndex(new Function2<Integer, Iterator<String>, Iterator<String>>() {
            @Override
            public Iterator<String> call(Integer index, Iterator<String> names) throws Exception {
                List<String> list = new ArrayList<String>();
                while (names.hasNext()) {
                    String staff = names.next();
                    list.add(staff);
                    System.out.println("第二次:"+(index+1) + " : " + staff);
                }
                return list.iterator();
            }
        }, true);
        staffRDD4.count();
        sc.close();
    }
}
