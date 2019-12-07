package cn.spark.study.core.upgrade;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName Intersection
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 13:41
 * @Version 1.0
 */
public class Distince {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("Distince").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //Distince
        //对RDD中的数据进行去重
        //举例:uv统计案例
        //uv : user view ,每天每个客户能对网站点击多少次
        //此时 需要对用户进行点击去重,然后统计出每天有多少个用户访问了网站
        //而不是所有用户访问了多少次
        //准备模拟数据
        List<String> accessLogs = Arrays.asList(
                "user1 2019-12-07 13:49:12",
                "user2 2019-12-07 13:49:12",
                "user3 2019-12-07 13:49:12",
                "user1 2019-12-07 13:49:12",
                "user1 2019-12-07 13:49:12",
                "user4 2019-12-07 13:49:12",
                "user2 2019-12-07 13:49:12"
                );
        JavaRDD<String> accessLogsRDD = sc.parallelize(accessLogs);
        JavaRDD<String> accessLogRDD = accessLogsRDD.map(new Function<String, String>() {
            @Override
            public String call(String log) throws Exception {
                return log.split(" ")[0];
            }
        });
        JavaRDD<String> usersRDD = accessLogRDD.distinct();
        int uvSize = usersRDD.collect().size();
        System.out.println("us size is : " + uvSize);
//        for (String staff : usersRDD.collect()) {
//            System.out.println(staff);
//        }


        sc.close();
    }
}
