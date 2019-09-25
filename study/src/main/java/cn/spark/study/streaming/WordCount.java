package cn.spark.study.streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * 实时worldcount程序
 * @ClassName WorldCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/23 15:54
 * @Version 1.0
 */
public class WordCount {
    public static void main(String[] args) throws Exception {
        //创建sparkConf对象
        //这里有一点不同,我们要设置一个Master属性,但是测试的时候是local模式
        //local后面必须跟一个方括号,里面写一个数字,数字代表了,我们用几个线程来执行我们的spark Streaming程序
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("WordCount");
        //创建JavaStreamingContext对象
        //该对象,就类似于Spark Core中的JavaSparkContext,就类似于SparkSQL中的SQLContext
        //该对象除了接收SparkConf对象之外
        //还必须接收一个Batch interval参数,就是说没收集多长时间的数据,划分一个batch,进行处理
        //这里设置一秒一次
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

        //首先创建输入DStream,代表了一个从数据源(比如kafka,socket)来的持续数据源
        //调用JavaStreamingContext的socketTextStream()方法,可以创建一个数据源为Socket网络端口的数据流
        //JavaReceiverDStream,代表了一个输入的DStream
        //socketTextStream()方法接收俩个基本参数,第一个是监听哪个主机上的地址,第二个是监听哪个端口
        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("local",9999);
        //到这里为止,可以理解为,JavaReceiverDStream中每隔一秒,会有一个RDD,其中封装了
        //这一秒发送的数据
        //RDD元素为String,即一行一行的文本
        //所以这里JavaReceiverInputStream的泛型类型<String>,其实就是代表了它地城的RDD的泛型类型

        //开始对接收到的数据,进行计算,使用SparkCore提供的算子,执行应用在Dstream中即可
        //在底层,实际上是会对DStream中的一个一个的RDD,执行我们应用在DStream上的算子
        //产生新的RDD,会作为新的DStreamRDD
        JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });

        //这个时候每秒的数据.一行一行的文本,就会被拆分为多个单词,words Dstream 中的RDD的元素类型
        //即为一个一个单词
        //接着,开始进行flatmap/reduceByKey操作
        JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });
        //说明: 用Spark Streaming开发程序,和Spark Core 很相像
        //唯一不同的是Spark Core 中的JavaRDD , JavaPairRDD,都变成了 JavaDStream,JavaPairDStream
        JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        //到此为止,我们就实现了wordCount程序了

        //每次计算完,都打印一下这一秒中单词计算情况,并休眠5秒钟,以便于测试和观察
        wordCounts.print();
        Thread.sleep(5000);
        //首先对javaStreamingContext进行后续处理
        //start() 启动执行
        jssc.start();
        //启动以后等待结束
        jssc.awaitTermination();
        jssc.close();
    }
}
