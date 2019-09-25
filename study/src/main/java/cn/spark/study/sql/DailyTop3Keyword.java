package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.*;

/**
 * @ClassName DailyTop3Keyword
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/19 15:34
 * @Version 1.0
 */
public class DailyTop3Keyword {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("DailyTop3Keyword");
        JavaSparkContext sc = new JavaSparkContext(conf);
        HiveContext hiveContext = new HiveContext(sc.sc());
        //伪造出一份数据,查询条件
        //备注:实际上,在实际的企业项目开发中,很可能,这个查询条件,是通过J2EE平台插入到某个MySQL表中的.
        //然后,这里呢,实际上,通常是会用Spring框架和ORM框架(Mybatis)的,去提取MySQL表中
        Map<String, List<String>> queryParamMap = new HashMap<String,List<String>>();
        queryParamMap.put("city", Arrays.asList("beijing"));
        queryParamMap.put("platform", Arrays.asList("android"));
        queryParamMap.put("version", Arrays.asList("1.0","1.2","1.5","2.0","1.1"));

        //根据优化思路,这个queryParamMap最合适的方式是封装为一个广播变量
        //这样可以进行优化,每个worker节点,就拷贝一份数据即可
        final Broadcast<Map<String, List<String>>> queryParamMapBroadcast = sc.broadcast(queryParamMap);

        //针对HDFS日志,获取输入RDD
        JavaRDD<String> rawRDD = sc.textFile("hdfs://weekend109:9000/spark-study/keyword.txt");

        //数据筛选
        JavaRDD<String> filterRDD = rawRDD.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String log) throws Exception {
                String[] logSplited = log.split("\t");
                String city = logSplited[3];
                String platform = logSplited[4];
                String version = logSplited[5];
                //与查询条件进行比对,任意一个条件设置了,且日志中的数据没有满足条件
                //则返回false,过滤日志
                //否则返回数据
                Map<String,List<String>> queryParamsMap = queryParamMapBroadcast.value();
                List<String> citys = queryParamsMap.get("city");
                if(citys.size() > 0 && !citys.contains(city)){
                    return false;
                }
                List<String> platforms = queryParamsMap.get("platform");
                if(platforms.size() > 0 && !platforms.contains(platform)){
                    return false;
                }
                List<String> versions = queryParamsMap.get("version");
                if(versions.size() > 0 && !versions.contains(version)){
                    return false;
                }
                return true;
            }
        });
        //将过滤出来的原始日志,映射为(日期_搜索词,用户)的格式,方便后期分组排序
        JavaPairRDD<String,String> dateKeywordUserRDD = filterRDD.mapToPair(new PairFunction<String, String, String>() {
            @Override
            public Tuple2<String, String> call(String log) throws Exception {
                String[] logSplited = log.split("\t");
                String date = logSplited[0];
                String user = logSplited[1];
                String keyword = logSplited[2];
                return new Tuple2<String,String>(date+"_"+keyword,user);
            }
        });
        //进行分组,获取每天每个搜索词,有哪些用户搜索了(没有去重)
        JavaPairRDD<String, Iterable<String>> dateKeywordUsersRDD = dateKeywordUserRDD.groupByKey();
        //对每天每个搜索词饿用户进行去重操作,得到uv
        JavaPairRDD<String, Long> dateKeywordUvRDD = dateKeywordUsersRDD.mapToPair(new PairFunction<Tuple2<String, Iterable<String>>, String, Long>() {
            @Override
            public Tuple2<String, Long> call(Tuple2<String, Iterable<String>> dateKeywordUsers) throws Exception {
                String dateKyeword = dateKeywordUsers._1;
                Iterator<String> users = dateKeywordUsers._2.iterator();
                //对用户进行去重,并统计去重后的数量
                List<String> distinctUsers = new ArrayList<String>();
                while(users.hasNext()){
                    String user = users.next();
                    if(!distinctUsers.contains(user)){
                        distinctUsers.add(user);
                    }
                }
                long uv = distinctUsers.size();
                return new Tuple2<>(dateKyeword,uv);
            }
        });
        JavaRDD<Row> dateKeywordUvRowRDD = dateKeywordUvRDD.map(new Function<Tuple2<String, Long>, Row>() {
            @Override
            public Row call(Tuple2<String, Long> dateKeywordUv) throws Exception {
                String date = dateKeywordUv._1.split("_")[0];
                String keyword = dateKeywordUv._1.split("_")[1];
                long uv = dateKeywordUv._2;
                return RowFactory.create(date,keyword,uv);
            }
        });
        List<StructField> structFields = Arrays.asList(
                DataTypes.createStructField("date",DataTypes.StringType,true),
                DataTypes.createStructField("keyword",DataTypes.StringType,true),
                DataTypes.createStructField("uv",DataTypes.LongType,true)
        );
        StructType structType = DataTypes.createStructType(structFields);
        DataFrame dateKeywordUvDF = hiveContext.createDataFrame(dateKeywordUvRowRDD, structType);
        dateKeywordUvDF.registerTempTable("daily_keyword_uv");
        //使用Spark SQL的开窗函数,统计每天搜索uv排名前3 的热点搜索词
        DataFrame dailyTop3KeywordDF = hiveContext.sql("select tmp.date,tmp.keyword,tmp.uv " +
                "from (" +
                "select " +
                "date," +
                "keyword," +
                "uv," +
                "row_number() over (partition by date order by uv desc) rank " +
                "from daily_keyword_uv) tmp " +
                "where tmp.rank<=3");
        //将DataFrame转换为RDD,然后映射,计算出每天的top3搜索词uv总数
        JavaRDD<Row> dailyTop3KeywordRDD = dailyTop3KeywordDF.javaRDD();
        JavaPairRDD<String,String> top3DateKeywordUvRDD = dailyTop3KeywordRDD.mapToPair(new PairFunction<Row, String, String>() {
            @Override
            public Tuple2<String, String> call(Row row) throws Exception {
                String date = new String(row.get(0)+"");
                String keyword = new String(row.get(1)+"");
                Long uv = Long.parseLong(new String(row.get(2)+""));
                return new Tuple2<String, String>(date,keyword+"_"+uv);
            }
        });
        JavaPairRDD<String, Iterable<String>> top3DateKeywordsRDD = top3DateKeywordUvRDD.groupByKey();
        JavaPairRDD<Long, String> uvDateKeywordsRDD = top3DateKeywordsRDD.mapToPair(new PairFunction<Tuple2<String, Iterable<String>>, Long, String>() {
            @Override
            public Tuple2<Long, String> call(Tuple2<String, Iterable<String>> tuple) throws Exception {
                String date = tuple._1;
                Long totalUv = 0L;
                String dateKeywords = date;
                Iterator<String> keywordUvIterator = tuple._2.iterator();
                while(keywordUvIterator.hasNext()){
                    String keywordUv = keywordUvIterator.next();
                    Long uv =Long.parseLong( keywordUv.split("_")[1]);
                    totalUv += uv;
                    dateKeywords += "," + keywordUv;
                }
                return new Tuple2<Long, String>(totalUv,dateKeywords);
            }
        });
        //按照每天的总搜索uv进行倒序排序
        JavaPairRDD<Long, String> sortedUvDatekeywordsRDD = uvDateKeywordsRDD.sortByKey(false);
        //再次进行映射,将排序后的数据映射会原始的格式(Iterable<Row>)
        JavaRDD<Row> sortedRowRDD = sortedUvDatekeywordsRDD.flatMap(new FlatMapFunction<Tuple2<Long, String>, Row>() {
            @Override
            public Iterable<Row> call(Tuple2<Long, String> tuple) throws Exception {
                String dateKeywords = tuple._2;
                String[] dateKeywordsSplited = dateKeywords.split(",");
                String date = dateKeywordsSplited[0];
                List<Row> rows = new ArrayList<Row>();
                for (int i =1;i<dateKeywordsSplited.length;i++) {
                    String keywordUv = dateKeywordsSplited[i];
                    String keyword = keywordUv.split("_")[0];
                    Long uv = Long.parseLong( keywordUv.split("_")[1]);
                    rows.add(RowFactory.create(date,keyword,uv));
                }
                return rows;
            }
        });
        //将最终的数据转换为DateFream,保存到Hive表中
        // structFields = Arrays.asList()和之前的一样,用之前的就可以
        DataFrame finalDF = hiveContext.createDataFrame(sortedRowRDD, structType);
        finalDF.saveAsTable("daily_top3_keyword_uv");


        sc.close();
    }

}
