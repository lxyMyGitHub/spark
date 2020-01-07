package cn.spark.study.streaming;

import kafka.serializer.StringDecoder;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.*;

/**
 * 基于Flume Push 方式的实时word count 程序
 * @ClassName FlumePushWordCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/24 15:56
 * @Version 1.0
 */
public class FlumePushWordCount {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("FlumePushWordCount");

        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        //创建输入DStream
        JavaReceiverInputDStream<SparkFlumeEvent> lines =
                FlumeUtils.createStream(jssc,"192.168.2.99",8888);

        JavaDStream<String> words = lines.flatMap(new FlatMapFunction<SparkFlumeEvent, String>() {
            @Override
            public Iterable<String> call(SparkFlumeEvent event) throws Exception {
                AvroFlumeEvent avroFlumeEvent = event.event();
                String line = new String(avroFlumeEvent.getBody().array());
                return Arrays.asList(line.split(" "));
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
