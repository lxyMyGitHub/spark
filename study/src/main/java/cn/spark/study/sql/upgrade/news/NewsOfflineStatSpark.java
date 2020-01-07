package cn.spark.study.sql.upgrade.news;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName NewsOfflineStatSpark
 * @Deseription 新闻网站关键指标离线统计spark作业
 * @Author lxy_m
 * @Date 2019/12/30 16:10
 * @Version 1.0
 * 每天每个页面的PV：PV是Page View，是指一个页面被所有用户访问次数的总和，页面被访问一次就被记录1次PV
 * 每天每个页面的UV：UV是User View，是指一个页面被多少个用户访问了，一个用户访问一次是1次UV，一个用户访问多次还是1次UV
 * 新用户注册比率：当天注册用户数 / 当天未注册用户数
 * 用户跳出率：IP只浏览了一个页面就离开网站的次数/网站总访问数（PV）
 * 版块热度排行榜：根据每个版块每天被访问的次数，做出一个排行榜
 * 网站日志格式
 * date timestamp userid pageid section action
 * 日志字段说明
 * date: 日期，yyyy-MM-dd格式
 * timestamp: 时间戳
 * userid: 用户id
 * pageid: 页面id
 * section: 新闻版块
 * action: 用户行为，两类，点击页面和注册
 */
public class NewsOfflineStatSpark {
    public static void main(String[] args) {
        //拿到昨天的日期,去hive表中查昨天的数据
        //针对昨天的数据执行SQL语句
        SparkConf conf = new SparkConf().setAppName("NewsOfflineStatSpark").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        HiveContext hiveContext = new HiveContext(sc.sc());
        //页面PV指标统计及排序
//        String yesterday = getYesterday();
        String yesterday = "2016-02-20";
        System.out.println("yesterday is : " + yesterday);
        //每次spark计算出来的结果,实际上都会写入MySQL存储
        //这样就可以基于MySQL,用javaweb技术开发一套管理平台,来使用图标方式展示每次spark计算出来的关键指标
        //第一个关键指标:页面pv统计及排序
        caculateDailyPagePv(hiveContext,yesterday);
        //第二个关键指标:页面uv统计及排序
        caculateDailyPageUv(hiveContext,yesterday);
        //第三个关键指标:新用户注册比率统计
        caculateDailyNewUserRegisterRate(hiveContext,yesterday);
        //第四个关键指标:用户跳出率统计
        caculateDailyUserJumpRate(hiveContext,yesterday);
        //第五个关键指标:板块热度排行榜
        caculateDailySectionPvSort(hiveContext,yesterday);

        sc.close();
    }

    /**
     * 板块热度排行榜
     * @param hiveContext
     * @param yesterday
     */
    private static void caculateDailySectionPvSort(HiveContext hiveContext, String yesterday) {
        String sql =
                "SELECT " +
                        "date,section,pv " +
                        "FROM ( " +
                        "SELECT " +
                        "date," +
                        "section," +
                        "count(*) pv " +
                        "FROM news_access " +
                        "WHERE action = 'view' " +
                        "AND date = '"+  yesterday +"' " +
                        "GROUP BY date,section" +
                        ") t " +
                        "ORDER BY pv DESC ";
        DataFrame df = hiveContext.sql(sql);
        df.show();
    }

    /**
     * 用户跳出率统计
     * @param hiveContext
     * @param yesterday
     */
    private static void caculateDailyUserJumpRate(HiveContext hiveContext, String yesterday) {
        //计算昨天总的访问pv
        String sql1 =
                "SELECT count(*) " +
                        "FROM news_access " +
                        "WHERE action = 'view' " +
                        "AND date = '"+ yesterday +"' " +
                        "AND userid IS NOT NULL ";
        //昨天的跳出总数
        String sql2 = "SELECT count(*) FROM (SELECT count(*) count FROM news_access WHERE action = 'view' AND date = '"+ yesterday +"' AND " +
                "userid IS NOT NULL GROUP BY userid HAVING count = 1 ) t " ;

        //执行俩条SQL获取结果
        Object result1 = hiveContext.sql(sql1).collect()[0].get(0);
        long number1 = 0L;
        if(result1 != null){
            number1 = Long.valueOf(result1.toString());
        }
        Object result2 = hiveContext.sql(sql2).collect()[0].get(0);
        long number2 = 0L;
        if(result2 != null){
            number2 = Long.valueOf(result2.toString());
        }
        //计算结果
        double rate = number2 / number1;
        System.out.println("rate is : " + formatDouble(rate,2));
    }

    /**
     * 计算每天的新用户注册
     * @param hiveContext
     * @param yesterday
     */
    private static void caculateDailyNewUserRegisterRate(HiveContext hiveContext, String yesterday) {
        //昨天所有访问行为中,userid为NULL,新用户的访问总数
        String sql1 =
                "SELECT count(*) " +
                        "FROM news_access " +
                        "WHERE action = 'view' " +
                        "AND date = '"+ yesterday +"' " +
                        "AND userid IS NULL ";
        //昨天的注册用户数
        String sql2 = "SELECT count(*) FROM news_access WHERE action = 'register' AND date = '"+ yesterday +"' " ;

        //执行俩条SQL获取结果
        Object result1 = hiveContext.sql(sql1).collect()[0].get(0);
        long number1 = 0L;
        if(result1 != null){
            number1 = Long.valueOf(result1.toString());
        }
        Object result2 = hiveContext.sql(sql2).collect()[0].get(0);
        long number2 = 0L;
        if(result2 != null){
            number2 = Long.valueOf(result2.toString());
        }
        //计算结果
        double rate = number2 / number1;
        System.out.println("rate is : " + formatDouble(rate,2));
    }

    /**
     * 格式化小数
     * @param num
     * @param scale
     * @return
     */
    private static double formatDouble(double num,int scale){
        BigDecimal bd = new BigDecimal(num);
        return bd.setScale(scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算每天每个页面的Uv,并排序
     * @param hiveContext
     * @param yesterday
     * Spark SQL 的count(distinct)语句有bug,默认会产生严重的数据倾斜
     * 只会用一个task,做去重和汇总统计,性能很差
     */
    private static void caculateDailyPageUv(HiveContext hiveContext, String yesterday) {
        String sql =
                "SELECT " +
                        "date," +
                        "pageid," +
                        "uv " +
                    "FROM ( " +
                        "SELECT " +
                            "date," +
                            "pageid," +
                            "count(*) uv " +
                        "FROM (" +
                            "SELECT " +
                                "date," +
                                "pageid," +
                                "userid " +
                            "FROM news_access " +
                            "WHERE action = 'view' " +
                            "AND date = '" + yesterday + "' " +
                            "GROUP BY date,pageid,userid " +
                        ") t2 " +
                        "GROUP BY date,pageid" +
                        ") t " +
                        "ORDER BY uv DESC ";
        DataFrame df = hiveContext.sql(sql);
        df.show();
    }

    /**
     * 获取昨天的字符串类型日期
     * @return
     */
    private static String getYesterday(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR,-1);
        Date yesterday = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(yesterday);
    }

    /**
     * 计算每天每个页面的pv,并排序
     * 排序的好处:
     *  排序后插入MySQL,java web系统查询非常方便
     *      如果这里不排序,java web端就要排序,会影响java web端性能,影响用户响应时间
     */
    private static void caculateDailyPagePv(HiveContext hiveContext,String date){
        String sql =
                "SELECT " +
                    "date,pageid,pv " +
                "FROM (" +
                    "SELECT " +
                        "date,pageid,count(*) pv " +
                    "FROM news_access " +
                    "WHERE action = 'view' " +
                        "AND date='"+ date+"' " +
                    "GROUP BY " +
                        "date,pageid " +
                    ") t " +
                    "ORDER BY pv DESC ";
        DataFrame df = hiveContext.sql(sql);
        //转换成一个RDD.对RDD执行一个foreach算子,在算子中将数据写入mysql
        //测试show()
        df.show();
    }
}
