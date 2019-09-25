package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;

import java.util.Arrays;

/**
 * @ClassName WordCountLocal
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/20 15:25
 * @Version 1.0
 * 本地测试的Wordcount程序
 */
public class WordCountLocal {
    public static <serialVersionUid> void main(String[] args) throws Exception{
            //编写spark应用程序
            //本地执行,可以直接运行

            //第一步,创建sparkconf对象,设置spark应用的配置信息
            //使用setMaster()可以设置spark应用程序要链接的spark集群的master节点的URL
            //但是如果设置了local,代表本地运行
        SparkConf conf = new SparkConf().setAppName("WordCountLocal").setMaster("local");
            //第二步:创建JavaSparkContext对象
            //在spark中.SparkContext是spark所有功能的一个入口,你无论是用Java,Scala甚至是Python编写
            //都必须要有一个SparkContext,它的主要作用,包括初始化spark应用程序所需的一些核心组件,包括
            //调度器(DAGSchedule,TaskSchedule),还会去到SparkMaster节点上进行注册,等等
            //一句话,SparkContext,是Spark应用中,可以说是最最重要的一个对象
            //但是呢,在spark中,编写不同类型的spark应用程序,使用的SparkContext是不同的,如果使用Scala
            //使用的就是原生的SparkContext对象,如果使用Java,就是JavaSparkContext对象
            //如果使用Spark SQL程序,那么就是SQLContext/HiveContext
            //如果是开发Spark Streaming程序,那么就是它独有的SparkContext
            //以此类推
        JavaSparkContext sc = new JavaSparkContext(conf);

            //第三步:要针对输入源,创建一个初始的RDD
            //输入源中的数据会打散,分配到RDD的每个partition中,从而形成一个初始的分布式数据集
            //此处用本地测试,针对本地文件
            //SparkContext中,用于根据文件类型的输入源创建RDD的方法,叫做textFile()方法
            //在Java中,创建普通的RDD,都叫做JavaRDD
            //在这里,RDD中,有元素这种概念,如果是hdfs或者是本地文件,创建的RDD每一个元素就相当于文件里的一行
        JavaRDD<String> lines = sc.textFile("D:\\c_clean\\spark\\spark.txt");

            //第四步:对初始的RDD进行transformation操作,也就是一些计算操作
            //先将每一行拆分成单个的单词
            //  通常操作会创建function,并配合RDD的map,flatmap等算子执行
            //通常如果function比较简单实用匿名内部类
            //如果function比较复杂,就单独创建

            //FlatMapFunction<String, String>俩个泛型代表输入和输入类型
            //flatMap作用就是将RDD的一个元素,给拆分成一个或多个元素
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });
            //接着需要将每一个单词映射为(单词,1)的这种形式,之后进行累加计算
            //单词作为key进行计算
            //mapToPair其实就是将每个元素都映射为一个(v1,v2)这样的tuple类型
            //mapTopPair这个算子需要与PariFunction配合使用,第一个泛型参数代表输入类型,第二个和第三个泛型参数代表输出的tuple2的第一个值和第二个值的类型
        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word,1);
            }
        });

            //需要以单词作为key,统计每个单词出现的次数,这里要使用reduceByKey这个算子
            //对每个key对应的value都进行reduce操作
            //pairs中(hello,1),(hello,1),(world,1)
            //类似reduce操作,顺序比较相邻的俩个值
            //最后返回的也是tuple,返回的是每个单词和其出现的次数
        JavaPairRDD<String,Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });

        //之前使用的flatMap,mapToPair.deducrByKey都叫做transformation操作
        //接下来需要action操作,比如foreach,来触发程序执行
        wordCounts.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> wordCount) throws Exception {
                System.out.println(wordCount._1 + "  appeared  "  + wordCount._2 + "  times ");
            }
        });

        sc.close();
    }
}
