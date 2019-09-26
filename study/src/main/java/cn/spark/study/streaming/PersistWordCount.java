package cn.spark.study.streaming;

import cn.spark.study.utils.ConnectionPool;
import com.google.common.base.Optional;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 基于持久化的程序WordCount
 * @ClassName PersistWordCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/24 17:12
 * @Version 1.0
 */
public class PersistWordCount {
    public static void main(String[] args) {
        System.setProperty("HADOOP_USER_NAME","hadoop");
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("PersistWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        jssc.checkpoint("hdfs://weekend109:9000/wordcount_checkpoint");
        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("weekend109", 9999);
        JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String line) throws Exception {
                return Arrays.asList(line.split(" "));
            }
        });

        JavaPairDStream<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });

        JavaPairDStream<String, Integer> wordCounts = pairs.updateStateByKey(
                new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {
            @Override
            public Optional<Integer> call(List<Integer> values, Optional<Integer> state) throws Exception {
                Integer newValue = 0;
                if(state.isPresent()){
                    newValue = state.get();
                }
                for (Integer value : values) {
                    newValue += value;
                }
                return Optional.of(newValue);
            }
        });
        wordCounts.foreachRDD(new Function<JavaPairRDD<String, Integer>, Void>() {
            @Override
            public Void call(JavaPairRDD<String, Integer> wordCountsRDD) throws Exception {
                wordCountsRDD.foreachPartition(new VoidFunction<Iterator<Tuple2<String, Integer>>>() {
                    @Override
                    public void call(Iterator<Tuple2<String, Integer>> wordCounts) throws Exception {
                        //给每个partition ,获取一个连接
                        Connection conn = ConnectionPool.getConnection();
                        //遍历数据,插入数据库
                        Tuple2<String,Integer> wordCount = null;
                        while(wordCounts.hasNext()){
                            wordCount = wordCounts.next();
                            String sql ="insert into wordcount (word,count) values ('"+wordCount._1+"',"+wordCount._2+")";
                            Statement stmt = conn.createStatement();
                            stmt.executeUpdate(sql);
                            System.out.println("=======================================insert successful ========================");
                        }
                        ConnectionPool.returnConnection(conn);
                    }
                });
                return null;
            }
        });
        jssc.start();
        jssc.awaitTermination();
        jssc.close();
    }
}
