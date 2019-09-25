package cn.spark.study.core;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName TransformationOperation
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/21 13:52
 * @Version 1.0
 */
public class TransformationOperation {

    public static void main(String[] args) {
//        map();
//        filter();
//        flatMap();
//        groupByKey();
//        reduceByKey();
//        sortByKey();
//        join();
        coGroupe();
    }


    /**
     * map算子案例:将集合中每一个元素都乘以2
     */
    private static void map(){
        // 创建SparkConf
        SparkConf conf = new SparkConf().setAppName("TransformationOperation").setMaster("local");
        // 创建JavaSparkContext
        JavaSparkContext sc = new JavaSparkContext(conf);
        //构造集合
        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9);
        //并行化集合,创建初始RDD
        JavaRDD<Integer> paras = sc.parallelize(numbers);
        //使用map算子,将集合中的每个元素都乘以2
        //map算子是对任何类型RDD都可以调用的
            //在Java中,map算子接收的参数是Function对象
            //创建Function对象,一定会让你设置第二个泛型参数,这个泛型类型,就是发那会的新元素的类型
            //同事call()方法的返回类型,也必须与第二个泛型类型同步
            //在call()方法内部就可以对原始RDD中的每一个元素进行处理和计算,并返回一个新的RDD
        JavaRDD<Integer> results = paras.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer value) throws Exception {
                return value * 2;
            }
        });
        //foreach打印:action操作
        results.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer v1) throws Exception {
                System.out.println(v1);
            }
        });

        //关闭JavaSparkContext
        sc.close();
    }

    /**
     * filter算子案例
     * 过滤集合中的偶数
     */
    private static void filter(){
        SparkConf conf = new SparkConf().setMaster("local").setAppName("TransformationOperation");
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
        JavaRDD<Integer> numberRDD = sc.parallelize(numbers);
        //filter算子,传入的也是Function对象,其他的使用其实和map一样
        //但是唯一不同的是,call()方法返回的类型是Boolean
        //每一个初始RDD中的元素都会传入call()方法,此时你可以执行各种自定义的计算逻辑
        //来判断这个元素是否是你想要的
        //如果你想在新的RDD中保留这个元素,那么返回就返回true,否则不想保留这个元素,返回false
        JavaRDD<Integer> evenNumberRDD = numberRDD.filter(new Function<Integer, Boolean>() {
            @Override
            public Boolean call(Integer v1) throws Exception {
                return v1 % 2 == 0;
            }
        });
        evenNumberRDD.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer v1) throws Exception {
                System.out.println("evenNumber is : "+v1);
            }
        });
        sc.close();

    }

    /**
     * flatMap案例
     * 将一行单词拆分为多个单词
     */
    private static void flatMap(){
        SparkConf conf = new SparkConf().setAppName("TransformationOperation").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<String> lines = Arrays.asList("hello you", "hello me", "hello every", "hello hahaha");
        JavaRDD<String> linesRDD = sc.parallelize(lines);
        //flatMap算子,在Java中,接收的参数是FlatMapFunction
        //我们需要自定义FlatMapFunction的第二个泛型类型,即代表了返回的 新元素类型
        //call 方法,返回的类型,不是Object,而是Iterable<Object>,这里的Object和第二个泛型类型相同
        //flatMap其实就是,接收原始RDD中的每个元素,并进行各种逻辑的计算和处理,返回可以返回多个元素
        //多个元素,即封装在Iterable集合中,可以使用ArrayList等集合
        JavaRDD<String> words = linesRDD.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String s) throws Exception {
                return Arrays.asList(s.split(" "));
            }
        });
        words.foreach(new VoidFunction<String>() {
            @Override
            public void call(String s) throws Exception {
                System.out.println(s);
            }
        });

        sc.close();


    }

    /**
     * groupByKey按照班级对成绩进行分组
     *
     */
    private static void groupByKey(){
        SparkConf conf = new SparkConf().setAppName("TransformationOperation").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //模拟集合
        List<Tuple2<String,Integer>> scoreList = Arrays.asList(new Tuple2<String, Integer>("class1",80),
                new Tuple2<String, Integer>("class2",55),
                new Tuple2<String, Integer>("class1",24),
                new Tuple2<String, Integer>("class1",98),
                new Tuple2<String, Integer>("class2",78));
        //并行化集合
        JavaPairRDD<String,Integer> scores = sc.parallelizePairs(scoreList);

        /**
         * 执行groupByKey
         * 对每个班级的成绩进行分组
         * groupByKey算子,返回的是JavaPairRDD
         * 但是JavaPairRDD的第一个泛型类型不变,第二个泛型类型编程Iterable这种集合类型
         * 也就是说,按照了key进行分组,那么每个key可能会有多个value,此时多个value聚合成了Iterable
         * 那么接下来,我们是不是就可以通过groupScores这种JavaPairRDD,很方便的处理某个分组内的数据
         *
         */
        JavaPairRDD<String, Iterable<Integer>> scoreRDD = scores.groupByKey();

        scoreRDD.foreach(new VoidFunction<Tuple2<String, Iterable<Integer>>>() {
            @Override
            public void call(Tuple2<String, Iterable<Integer>> tuple) throws Exception {
                System.out.println("class is :"+tuple._1);
                Iterator<Integer> ite = tuple._2.iterator();
                while (ite.hasNext()){
                    System.out.println(ite.next());
                }
                System.out.println("==============================");
            }
        });
        sc.close();
    }

    /**
     * reduceByKey计算每个班级总分
     *
     */
    private static void reduceByKey(){
        SparkConf conf = new SparkConf().setAppName("TransformationOperation").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //模拟集合
        List<Tuple2<String,Integer>> scoreList = Arrays.asList(new Tuple2<String, Integer>("class1",80),
                new Tuple2<String, Integer>("class2",55),
                new Tuple2<String, Integer>("class1",24),
                new Tuple2<String, Integer>("class1",98),
                new Tuple2<String, Integer>("class2",78));
        //并行化集合
        JavaPairRDD<String,Integer> scores = sc.parallelizePairs(scoreList);
        /**
         *reduceByKey接收的参数是Function2类型,有3个泛型参数,代表的是三个值
         * 第一个泛型类型和第二个泛型类型代表了原始的RDD中的元素的value类型
         * 因此对每个key进行reduce,都会依次将第一个,第二个value传入,将值再与第三个value传入
         * 因此此处会自动定义俩个泛型类型,代表call方法的俩个传入参数类型
         * 第三个泛型类型,代表了每次reduce结果的返回值类型,默认也是与原RDD的value类型相同
         * reduceByKey算子返回的RDD还是JavaPairRDD<Key,value>
         */
        JavaPairRDD<String, Integer> totalScores = scores.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        totalScores.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> tuple) throws Exception {
                System.out.println("className is : "+tuple._1);
                System.out.println("class total score is : "+tuple._2);
                System.out.println("==============================");
            }
        });

        sc.close();
    }

    /**
     * 按照学生分数进行排序
     * sortByKey算子
     */
    private static void sortByKey(){
        SparkConf conf = new SparkConf().setMaster("local").setAppName("TransformationOperation");
        JavaSparkContext sc = new JavaSparkContext(conf);
        List<Tuple2<Integer,String>> scoreList = Arrays.asList(
                new Tuple2<Integer, String>(65,"marry"),
                new Tuple2<Integer, String>(62,"jcak"),
                new Tuple2<Integer, String>(55,"tony"),
                new Tuple2<Integer, String>(75,"honey"),
                new Tuple2<Integer, String>(25,"ailins")
        );
        JavaPairRDD<Integer, String> scoreRDD  = sc.parallelizePairs(scoreList);
        //sortByKey(false)为降序,sortByKey(true)默认为升序排序
        //返回的还是JavaRDD
        JavaPairRDD<Integer, String> sortdScore = scoreRDD.sortByKey();
        sortdScore.foreach(new VoidFunction<Tuple2<Integer, String>>() {
            @Override
            public void call(Tuple2<Integer, String> tuple2) throws Exception {
                System.out.println(tuple2._2+" : "+tuple2._1);
            }
        });
        sc.close();


    }

    /**
     * 打印学生成绩
     * join算子
     */
    private static void join(){
        SparkConf conf = new SparkConf().setAppName("TransformationOperation").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //模拟集合
        List<Tuple2<Integer,String>> studentList = Arrays.asList(
                new Tuple2<Integer, String>(1,"leo"),
                new Tuple2<Integer, String>(2,"jcak"),
                new Tuple2<Integer, String>(3,"tom")
        );
        List<Tuple2<Integer,Integer>> scoreList = Arrays.asList(
                new Tuple2<Integer, Integer>(1,100),
                new Tuple2<Integer, Integer>(2,90),
                new Tuple2<Integer, Integer>(3,60)
        );
        JavaPairRDD<Integer, String> students = sc.parallelizePairs(studentList);
        JavaPairRDD<Integer, Integer> scores = sc.parallelizePairs(scoreList);
        //使用join算子关联俩个RDD
        //join以后,还是会根据key进行join,并返回JavaPariRDD
        //但是JavaPairRDD的第一个泛型类型,之前俩个JavaPairRDD的key的类型,因为是通过key进行join的
        //第二个泛型类型,是Tuple2<v1,v2>类型,Tuple2的俩个泛型分别为原始RDD的value的类型
        //join,就返回的RDD的每一个元素,就是通过key Join上一个pair
        //比如 (1,1) (1,2) (1,3)的一个RDD
        //还有 (1,4) (2,1) (2,2)的一个RDD
        //join以后会得到 (1,(1,4)) (1,(2,4)) (1,(3,4))
        JavaPairRDD<Integer, Tuple2<String, Integer>> studentScores = students.join(scores);
        studentScores.foreach(new VoidFunction<Tuple2<Integer, Tuple2<String, Integer>>>() {
            @Override
            public void call(Tuple2<Integer, Tuple2<String, Integer>> tuple) throws Exception {
                System.out.println("student id : " + tuple._1);
                System.out.println("student name : " + tuple._2._1);
                System.out.println("student score : " + tuple._2._2);
                System.out.println("=================================");
            }
        });

        sc.close();

    }
    /**
     * 打印学生成绩
     * cogroupe算子
     */
    private static void coGroupe(){
        SparkConf conf = new SparkConf().setAppName("TransformationOperation").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        //模拟集合
        List<Tuple2<Integer,String>> studentList = Arrays.asList(
                new Tuple2<Integer, String>(1,"leo"),
                new Tuple2<Integer, String>(2,"jcak"),
                new Tuple2<Integer, String>(3,"tom")
        );
        List<Tuple2<Integer,Integer>> scoreList = Arrays.asList(
                new Tuple2<Integer, Integer>(1,100),
                new Tuple2<Integer, Integer>(2,90),
                new Tuple2<Integer, Integer>(3,60),
                new Tuple2<Integer, Integer>(2,60),
                new Tuple2<Integer, Integer>(3,80)
        );
        JavaPairRDD<Integer, String> students = sc.parallelizePairs(studentList);
        JavaPairRDD<Integer, Integer> scores = sc.parallelizePairs(scoreList);
        //cogroup
        JavaPairRDD<Integer, Tuple2<Iterable<String>, Iterable<Integer>>> studentsScore = students.cogroup(scores);

        studentsScore.foreach(new VoidFunction<Tuple2<Integer, Tuple2<Iterable<String>, Iterable<Integer>>>>() {
            @Override
            public void call(Tuple2<Integer, Tuple2<Iterable<String>, Iterable<Integer>>> v) throws Exception {
                System.out.println("student id : " + v._1);
                System.out.println("student name : " + v._2._1);
                System.out.println("student score : " + v._2._2);
                System.out.println("==============================");

            }
        });
        sc.close();

    }
}
