package cn.spark.study.streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName KafkaReceiverWordCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/24 13:47
 * @Version 1.0
 */
public class KafkaReceiverWordCount {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("HDFSWordCount");

        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        //使用KafkaUtils.createStream()方法,创建针对kafka的输入数据流
        Map<String,Integer> topicThreadMap = new HashMap<String,Integer>();
        topicThreadMap.put("wordCount",1);
        JavaPairReceiverInputDStream<String, String> lines = KafkaUtils.createStream(jssc,
                "weekend109:2181,weekend110:2181,weekend111:2181",
                "DefaultConsumerGroup",
                topicThreadMap);

        //开发wordCount逻辑
        JavaDStream<String> words = lines.flatMap(new FlatMapFunction<Tuple2<String, String>, String>() {
            @Override
            public Iterable<String> call(Tuple2<String, String> tuple) throws Exception {
                return Arrays.asList(tuple._2.split(" "));
            }
        });

        JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });

        JavaPairDStream<String, Integer> wordCount = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        wordCount.print();

        jssc.start();
        jssc.awaitTermination();
        jssc.close();
    }
}
