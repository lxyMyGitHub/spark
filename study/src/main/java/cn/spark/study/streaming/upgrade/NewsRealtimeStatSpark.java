package cn.spark.study.streaming.upgrade;

import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName NewsRealtimeStatSpark
 * @Deseription 新闻网站关键指标实时统计Spark应用程序
 * @Author lxy_m
 * @Date 2020/1/6 16:59
 * @Version 1.0
 */
public class NewsRealtimeStatSpark {
    public static void main(String[] args) throws Exception{
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("NewsRealtimeStatSpark");

        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        //首先,要创建一份kafka参数map
        Map<String,String> kafkaParams = new HashMap<String,String>();
        kafkaParams.put("metadata.broker.list","192.168.2.99:9092,192.168.2.100:9092,192.168.2.101:9092");

        //然后创建一个set,写入要读取的topic
        Set<String> topics = new HashSet<String>();
        topics.add("news-access");

        //创建输入DStream
        JavaPairInputDStream<String, String> lines = KafkaUtils.createDirectStream(jssc,
                String.class,
                String.class,
                StringDecoder.class,
                StringDecoder.class,
                kafkaParams, topics);
        //统计第一个指标:每10秒内的页面pv

        //先从所有数据中过滤出来访问日志
        JavaPairDStream<String, String> accessDStream = lines.filter(new Function<Tuple2<String, String>, Boolean>() {
            @Override
            public Boolean call(Tuple2<String, String> tuple) throws Exception {
                String log = tuple._2;
                String[] logSpilted = log.split(" ");
                String action = logSpilted[5];
                if("view".equals(action)){
                    return true;
                }else{
                    return false;
                }
            }
        });

        //统计第一个指标:每10秒内各个页面的pv
        caculatePagePv(accessDStream);
        //统计第二个指标:每10秒内各个页面的uv
        caculatePageUv(accessDStream);
        //统计第三个指标:每10秒内实时注册用户数
        calculateRegisterCount(lines);
        //统计第四个指标:每10秒内实时用户跳出数量
        calculateUserJumpCount(accessDStream);
        //统计第五个指标:每10秒内实时板块pv
        calculateSectionPv(accessDStream);
        jssc.start();
        jssc.awaitTermination();
        jssc.close();

    }

    /**
     * 计算每10秒钟的pageUv
     * @param accessDStream
     */
    private static void caculatePageUv(JavaPairDStream<String, String> accessDStream) {
        JavaDStream<String> pageidUseridDStream = accessDStream.map(new Function<Tuple2<String, String>, String>() {
            @Override
            public String call(Tuple2<String, String> tuple) throws Exception {
                String log = tuple._2;
                String[] logSpilted = log.split(" ");
                Long pageid = Long.valueOf(logSpilted[3]);
                Long userid = Long.valueOf("null".equals(logSpilted[2]) ? "-1" :logSpilted[2]);
                return pageid+"_"+userid;
            }
        });
        JavaDStream<String> distinctPageidUseridDStream = pageidUseridDStream.transform(new Function<JavaRDD<String>, JavaRDD<String>>() {
            @Override
            public JavaRDD<String> call(JavaRDD<String> rdd) throws Exception {
                //对rdd进行去重操作
                return rdd.distinct();
            }
        });

        JavaPairDStream<Long, Long> pageidDStream = distinctPageidUseridDStream.mapToPair(new PairFunction<String, Long, Long>() {
            @Override
            public Tuple2<Long, Long> call(String str) throws Exception {
                String[] splited = str.split("_");
                Long pageid = Long.parseLong(splited[0]);
                return new Tuple2<Long, Long>(pageid, 1L);
            }
        });
        JavaPairDStream<Long, Long> pageUvDStream = pageidDStream.reduceByKey(new Function2<Long, Long, Long>() {
            @Override
            public Long call(Long v1, Long v2) throws Exception {
                return v1 + v2;
            }
        });
        pageUvDStream.print();
    }

    /**
     * 计算页面pv
     * @param accessDStream
     */
    private static void caculatePagePv(JavaPairDStream<String, String> accessDStream) {
        JavaPairDStream<Long, Long> pageidDStream = accessDStream.mapToPair(new PairFunction<Tuple2<String, String>, Long, Long>() {
            @Override
            public Tuple2<Long, Long> call(Tuple2<String, String> tuple) throws Exception {
                String log = tuple._2;
                String[] logSpilted = log.split(" ");
                Long pageid = Long.valueOf(logSpilted[3]);
                return new Tuple2<Long, Long>(pageid, 1L);
            }
        });
        JavaPairDStream<Long, Long> pagePvDStream = pageidDStream.reduceByKey(new Function2<Long, Long, Long>() {
            @Override
            public Long call(Long v1, Long v2) throws Exception {
                return v1 + v2;
            }
        });
        //打印 计算结果
        pageidDStream.print();
        //在计算出没10秒页面pv之后,应该持久化到MySQL或redis中,对每个页面的pv进行累加
        //javaee系统就可以从MySQL或redis中,读取page pv实时变化的数据,以及曲线图
    }

    /**
     * 计算实时注册用户数
     * @param lines
     */
    private static void calculateRegisterCount(JavaPairInputDStream<String,String> lines){
        JavaPairDStream<String, String> accessDStream = lines.filter(new Function<Tuple2<String, String>, Boolean>() {
            @Override
            public Boolean call(Tuple2<String, String> tuple) throws Exception {
                String log = tuple._2;
                String[] logSpilted = log.split(" ");
                String action = logSpilted[5];
                if ("register".equals(action)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        JavaDStream<Long> registerCountDStream = accessDStream.count();
        registerCountDStream.print();
        //每次统计完一个最近10秒的数据之后,不是打印出来
        //而是存储(mysql,redis,hbase)
        //选用哪一种主要看你的公司提供的环境,以及你的看实时报表的用户以及并发数量
        //包括数据量
        //如果是一般的展示效果,就选用MySQL,如果需要超高并发展示
        //比如QPS >=1w看实时报表 就用redis
        //如果数据量比较大,就用HBASE

        //每次从存储中查询注册数量,最近一次插入的记录,比如上次是10秒前
        //然后将当前的记录和上一次记录累加,然后往存储中插入一条新纪录,就是最新的一条数据
        //然后javaee系统展示的时候,可以查看最近半小时内的注册用户数量变化的曲线图
        //查看一周内,每天的注册用户数量的变化曲线图(每天就取最后一条数据,就是每天的最终数据)

    }

    /**
     * 计算用户跳出数量
     * @param accessDStream
     */
    private static void calculateUserJumpCount(JavaPairDStream<String,String> accessDStream){
        JavaPairDStream<Long, Long> useridDStream = accessDStream.mapToPair(new PairFunction<Tuple2<String, String>, Long, Long>() {
            @Override
            public Tuple2<Long, Long> call(Tuple2<String, String> tuple) throws Exception {
                String log = tuple._2;
                String[] logSpilted = log.split(" ");
                Long userid = Long.valueOf("null".equals(logSpilted[2]) ? "-1" :logSpilted[2]);
                return new Tuple2<Long, Long>(userid, 1L);
            }
        });
        JavaPairDStream<Long, Long> userCountDStream = useridDStream.reduceByKey(new Function2<Long, Long, Long>() {
            @Override
            public Long call(Long v1, Long v2) throws Exception {
                return v1 + v2;
            }
        });
        JavaPairDStream<Long, Long> userJumpDStream = userCountDStream.filter(new Function<Tuple2<Long, Long>, Boolean>() {
            @Override
            public Boolean call(Tuple2<Long, Long> tuple) throws Exception {
                if (tuple._2 == 1) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        JavaDStream<Long> userJumpCountDStream = userJumpDStream.count();
        userJumpCountDStream.print();
    }

    /**
     * 板块实时pv
     */
    private static void calculateSectionPv(JavaPairDStream<String,String> accessDStream){
        JavaPairDStream<String, Long> sectionDStream = accessDStream.mapToPair(new PairFunction<Tuple2<String, String>, String, Long>() {
            @Override
            public Tuple2<String, Long> call(Tuple2<String, String> tuple) throws Exception {
                String log = tuple._2;
                String[] logSpilted = log.split(" ");
                String section = logSpilted[4];
                return new Tuple2<String, Long>(section, 1L);
            }
        });
        JavaPairDStream<String, Long> sectionPvDStream = sectionDStream.reduceByKey(new Function2<Long, Long, Long>() {
            @Override
            public Long call(Long v1, Long v2) throws Exception {
                return v1 + v2;
            }
        });
        sectionPvDStream.print();
    }
}
