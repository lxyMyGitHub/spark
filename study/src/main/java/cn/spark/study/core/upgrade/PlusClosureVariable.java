package cn.spark.study.core;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @ClassName PlusClosureVariable
 * @Deseription 闭包
 * @Author lxy_m
 * @Date 2019/12/2 14:19
 * @Version 1.0
 */
public class PlusClosureVariable { 
    public static void main(String[] args)
    {
//        SparkConf conf = new SparkConf().setAppName("PlusClosureVariable");
        SparkConf conf = new SparkConf().setAppName("PlusClosureVariable"). setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<Integer> numbers = Arrays.asList(1,2,3,4,5);
        JavaRDD<Integer> numbersRDD = sc.parallelize(numbers);
        final List<Integer> closureNumbers = new ArrayList<Integer>();
        closureNumbers.add(0);
        numbersRDD.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer num) throws Exception {
                int closureNumberValue = closureNumbers.get(0);
                closureNumberValue += num;
                closureNumbers.set(0,closureNumberValue);
                System.out.println("===========================闭包值 : "+ closureNumberValue +"==========================");
            }
        });
        System.out.println("===========================闭包值 : "+closureNumbers.get(0)+"==========================");


        sc.close();
    }

}
