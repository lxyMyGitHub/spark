package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.*;

/**
 * @ClassName MapPartitions
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/7 10:23
 * @Version 1.0
 */
public class MapPartitions {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local").setAppName("MapPartitions");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //准备模拟数据
        List<String> studentNames = Arrays.asList("张三","李四","王二","赵武");
        JavaRDD<String> studentNamesRDD = sc.parallelize(studentNames, 2);
        final Map<String,Double> studentScores = new HashMap<String,Double>();
        studentScores.put("张三",300.5);
        studentScores.put("李四",405.63);
        studentScores.put("王二",509.63);
        studentScores.put("赵武",885.23);
        //mapPartitions
        //类似map,不同之处在于,map算子,一次就处理一个partition中的一条数据
        //mapPartitions一次处理一个partition中所有的数据

        //使用场景
        //如果你的RDD数据量不是特别大,那么建议使用mapPartitions算子替代map算子,可以加快处理速度
        //但是如果你的RDD的数据量特别大,比如说10亿,不建议使用mapPartitions,可能会OOM
        JavaRDD<Double> studentScoresRDD = studentNamesRDD.mapPartitions(new FlatMapFunction<Iterator<String>, Double>() {
            @Override
            public Iterable<Double> call(Iterator<String> names) throws Exception {
                //因为算子一次性处理一个partition的所有数据
                //call函数接收的参数,是iterator类型,代表了partition中所有数据的迭代器
                //返回的是一个Iterable类型,代表了返回多条记录通常使用List类型(因为List实现了Iterable接口)
                List<Double> studentScoreList = new ArrayList<Double>();
                while(names.hasNext()){
                    String studentName = names.next();
                    Double score = studentScores.get(studentName);
                    studentScoreList.add(score);
                }
                return studentScoreList;
            }
        });
        List<Double> studentScoresList = studentScoresRDD.collect();
        for (Double studentScore : studentScoresList) {
            System.out.println(studentScore);
        }
        sc.close();

    }
}
