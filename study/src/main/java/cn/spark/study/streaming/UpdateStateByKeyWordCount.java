package cn.spark.study.streaming;

import com.google.common.base.Optional;
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
import java.util.List;

/**
 * UpdateStateByKey算子实现缓存机制的实时wordCount程序WordCount
 * @ClassName UpdateStateByKeyWordCount
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/24 17:12
 * @Version 1.0
 */
public class UpdateStateByKeyWordCount {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("UpdateStateByKeyWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));

        //第一点,如果要使用updateStateByKey算子,就必须设置一个checkpoint目录,开启checkpoint机制
        //这样的话,才能把每个key对应的state除了在内存中有,那么是不是也要checkpoint一份
        //因为你要长期保存一份key的state的话,那么spark streaming是要求必须用checkpoint的,以便于在
        //内存数据丢失的时候,可以从checkpoint中恢复数据

        //开启checkpoint机制,调用checkpoint方法,设置一个hdfs目录即可
        jssc.checkpoint("hdfs://weekend109:9000/wordcount_checkpoint");
        //然后实现基础的wordCount逻辑
        JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
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
        //如果需要从程序运行开始到现在统计计数,就必须通过Redis或者MySQL这些db来实现累加
        //但是,我们的updateStateByKey,就可以实现直接通过spark维护一份每个单词的全局统计次数

        //这里的Optional,相当于Scala中的样例类,就是Option,可以这么理解
        //他代表了一个值得存在状态,可能存在,也可能不存在
        JavaPairDStream<String, Integer> wordCounts = pairs.updateStateByKey(
                new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {
            //这里俩个参数
            //实际上,对于每个单词,每次batch计算的时候
            //都会调用这个函数
            //第一个参数values,相当于是这个batch中,这个key的新的值,可能有多个吧
                    //比如说一个hello,可能有俩个(hello,1),(hello,1) ,那么传入的是(1,1)
                    //第二个参数,就是指的是这个key之前的状态,state,其中泛型的类型是你自己制定的
            @Override
            public Optional<Integer> call(List<Integer> values, Optional<Integer> state) throws Exception {
                //首先定义一个全局的单词计数
                Integer newValue = 0;
                //其次判断state是否存在,如果不存在说明是第一个key第一次出现
                //如果存在,说明这个key之前已经统计过了
                if(state.isPresent()){
                    newValue = state.get();
                }
                //接着,将本次新出现的值,都累加到newValue上面去,就是一个key目前的全局统计
                //次数
                for (Integer value : values) {
                    newValue += value;
                }

                return Optional.of(newValue);
            }
        });
        //到这里为止,相当于是,每个batch过来时,计算到Pairs DStream,就会执行全局的updateStateByKey算子
        //project/src返回的JavaPairDStream就是统计单词的结果
        wordCounts.print();
        jssc.start();
        jssc.awaitTermination();
        jssc.close();
    }
}
