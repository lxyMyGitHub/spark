package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Cartesian
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:41
 * @Version 1.0
 */
public class Cartesian {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Cartesian").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //Cartesian 笛卡尔积
        //比如说有俩个RDD,分别有10条数据,用了cartesian算子以后
        //俩个RDD的每一条数据都会和另一个RDD的每一条数据进行一次join
        //最终组成了一个笛卡尔乘积

        //小案例
        //有5件衣服 5条裤子 属于俩个RDD
        //需要每件衣服和每条裤子进行一次join 尝试进服装搭配
        List<String> clothes = Arrays.asList("夹克","T恤","皮衣","卫衣","羽绒服");
        JavaRDD<String> clothesRDD = sc.parallelize(clothes);
        List<String> trousers = Arrays.asList("打底裤","牛仔裤","工装裤","运动裤");
        JavaRDD<String> trousersRDD = sc.parallelize(trousers);
        JavaPairRDD<String, String> pairsRDD = clothesRDD.cartesian(trousersRDD);
        for (Tuple2<String,String> pair : pairsRDD.collect()) {
            System.out.println(pair._1 + " + " + pair._2);
        }


        sc.close();
    }
}
