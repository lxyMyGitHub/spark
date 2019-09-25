package cn.spark.study.core;

import org.apache.avro.TestAnnotation;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * action实战
 * @ClassName ActionOperation
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/22 14:19
 * @Version 1.0
 */
public class ActionOperation {
    private static SparkConf conf = null;
    private static JavaSparkContext sc = null;

    public static void main(String[] args) {
//        reduce();
//        collect();
//        count();
//        take();
//        saveAsTextFile();
        countByKey();
    }

    private static void init(){
        conf = new SparkConf().setMaster("local").setAppName("ActionOperation");
        sc = new JavaSparkContext(conf);
    }
    public static void reduce(){
        init();
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8);
        JavaRDD<Integer> listRDD = sc.parallelize(list);
        int sum = listRDD.reduce(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        System.out.println(sum);

    }

    private static void collect(){
        init();
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8);
        JavaRDD<Integer> listRDD = sc.parallelize(list);
        JavaRDD<Integer> doubleNumber = listRDD.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v) throws Exception {
                return v * 2;
            }
        });
        //一般不建议使用,
        //用foreach action,在远程集群上遍历rdd中的数据
        //而使用collect操作,将分布在远程集群上的数据拉取到本地
        //所以这种方式一般不建议使用,因为要从远程走大量的网络传输,将数据获取到本地
        //此外除了性能差,还可能在RDD数据特别 大的情况下,发生OOM异常,内存溢出
        //因此建议使用foreach action操作
        List<Integer> collect = doubleNumber.collect();
        System.out.println(collect);

    }

    private  static void count(){
        init();
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8);
        JavaRDD<Integer> listRDD = sc.parallelize(list);
        long count = listRDD.count();
        System.out.println(count);
    }

    /**
     * take也是从集群机器取数据到本地
     */
    private static void take(){
        init();
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8);
        JavaRDD<Integer> listRDD = sc.parallelize(list);
        List<Integer> take = listRDD.take(3);
        for (Integer i : take) {
            System.out.println(i);
        }
    }

    private static void saveAsTextFile(){
        init();
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,7,8);
        JavaRDD<Integer> listRDD = sc.parallelize(list);
        JavaRDD<Integer> doubleNumber = listRDD.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v) throws Exception {
                return v * 2;
            }
        });
        doubleNumber.saveAsTextFile("hdfs://weekend109:9000/data");
        sc.close();
    }
    
    private static void countByKey(){
        init();
        List<Tuple2<String,Integer>> scoreList = Arrays.asList(new Tuple2<String, Integer>("class1",80),
                new Tuple2<String, Integer>("class2",55),
                new Tuple2<String, Integer>("class1",24),
                new Tuple2<String, Integer>("class1",98),
                new Tuple2<String, Integer>("class2",78));
        //并行化集合
        JavaPairRDD<String,Integer> scores = sc.parallelizePairs(scoreList);
        Map<String, Object> students = scores.countByKey();
        for (Map.Entry<String,Object> entry : students.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue().toString());
        }
        sc.close();
    }
}
