package cn.spark.study.streaming;

import com.google.common.base.Optional;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于transform的实时黑名单过滤
 * 广告大数据系统
 * @ClassName TransformBlacklist
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/25 11:13
 * @Version 1.0
 */
public class TransformBlacklist {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("TransformBlacklist");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        //用户对我们的网站上的广告进行点击
        //点击之后,是不是要进行实时计费,点一下算一次钱
        //但是,对于那些帮助某些无良商家刷广告的人,那么我们有一个黑名单
        //如果是黑名单中的用户点击广告,就给过滤掉

        //先做一份模拟的黑名单RDD
        List<Tuple2<String,Boolean>> blackListData = new ArrayList<Tuple2<String,Boolean>>();
        blackListData.add(new Tuple2<String, Boolean>("Tom",true));

        final JavaPairRDD<String,Boolean> blackListRDD = jssc.sc().parallelizePairs(blackListData);

        //这里的日志格式,就简化一下,就是date username的格式
        JavaReceiverInputDStream<String> adsClickLogDStream = jssc.socketTextStream("weekend109",9999);

        //所以要先对输入的数据,进行一下转换,变成(username,date_username)
        //以便于,后面对每个batch RDD,与定义好的黑名单RDD进行join操作
        JavaPairDStream<String,String> userAdsClickLogDStream = adsClickLogDStream.mapToPair(new PairFunction<String, String, String>() {
            @Override
            public Tuple2<String, String> call(String adsClickLog) throws Exception {
                return new Tuple2<String,String>(adsClickLog.split(" ")[1],adsClickLog);
            }
        });
        //然后就可以执行transform操作了,将每个batch的RDD,与黑名单RDD进行join,filter,map等操作
        //实时进行黑名单过滤
        JavaDStream<String> validAdsClickLogDStream = userAdsClickLogDStream.transform(new Function<JavaPairRDD<String, String>, JavaRDD<String>>() {
            @Override
            public JavaRDD<String> call(JavaPairRDD<String, String> userAdsClickLogRDD) throws Exception {
                //这里为什么用左外链接,因为这里并不是每个用户都存在于黑名单中的
                //所以如果直接用join,那么没有存在于黑名单中的数据,会无法join到
                //就给丢弃掉了,所以这里用左外连接,就是哪怕一个user没有在黑名单中
                //没有join到,也会被保存下来的

                JavaPairRDD<String, Tuple2<String, Optional<Boolean>>> joinedRDD = userAdsClickLogRDD.leftOuterJoin(blackListRDD);
                //链接之后执行filiter算子,进行过滤
                JavaPairRDD<String, Tuple2<String, Optional<Boolean>>> filteredRDD = joinedRDD.filter(new Function<Tuple2<String, Tuple2<String, Optional<Boolean>>>, Boolean>() {
                    @Override
                    public Boolean call(Tuple2<String, Tuple2<String, Optional<Boolean>>> tuple) throws Exception {
                        //这里的tuple就是每条点击日志
                        /**
                         * Tuple2<String, Tuple2<String, Optional<Boolean>>>
                         *     用户            用户    黑名单中的状态
                         */
                        if(tuple._2._2().isPresent() && tuple._2._2.get()){
                            return false;
                        }
                        return true;
                    }
                });
                //此时,filteredRDD中就只剩下没有被黑名单过滤的用户点击了
                //进行map操作
                JavaRDD<String> validAdsClickLogRDD = filteredRDD.map(new Function<Tuple2<String, Tuple2<String, Optional<Boolean>>>, String>() {
                    @Override
                    public String call(Tuple2<String, Tuple2<String, Optional<Boolean>>> tuple) throws Exception {
                        return tuple._2._1;
                    }
                });

                return validAdsClickLogRDD;
            }
        });
        //打印有效的广告点击日志
        //在真实的企业场景中,这里就可以写入kafka,avtiveMQ等这种中间件消息队列
        //然后再开发一个专门的后代查询,做业务功能
        validAdsClickLogDStream.print();
        jssc.start();
        jssc.awaitTermination();
        jssc.close();
    }
}
