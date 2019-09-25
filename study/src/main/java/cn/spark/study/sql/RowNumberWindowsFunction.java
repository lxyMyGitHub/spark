package cn.spark.study.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;

/**
 * row_number()开窗函数实战
 * @ClassName RowNumberWindowsFunction
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/18 17:09
 * @Version 1.0
 */
public class RowNumberWindowsFunction {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf().setAppName("RowNumberWindowsFunction");
        JavaSparkContext sc = new JavaSparkContext(conf);
        HiveContext hiveContext = new HiveContext(sc.sc());
        //创建销售额表
        hiveContext.sql("DROP TABLE IF EXISTS sales");
        hiveContext.sql("CREATE TABLE IF NOT EXISTS sales(" +
                "product STRING," +
                "category STRING," +
                "revenue BIGINT)");
        hiveContext.sql("LOAD DATA " +
                "LOCAL INPATH '/home/hadoop/app/study/resources/sales.txt' " +
                "INTO TABLE sales");
        //开始编写我们的统计逻辑row_number()开窗函数
        //语法说明:
        //首先可以在SELECT查询时,使用row_number()函数,
        //其次,row_number()函数后面 OVER关键字()
        //然后括号中,是PARTITION BY,也就是根据那个字段进行分组
        //其次是可以用ORDER BY进行组内排序
        //然后row_number就可以给每个组内的行,一个组内行号
        DataFrame top3SalesDF = hiveContext.sql("" +
                "SELECT tmp_sales.product,tmp_sales.category,tmp_sales.revenue " +
                "FROM (" +
                "SELECT " +
                "product,category,revenue," +
                "row_number() OVER (PARTITION BY category ORDER BY revenue DESC) rank " +
                "FROM sales" +
                ") tmp_sales " +
                "WHERE tmp_sales.rank <= 3");
        //将每组排名前3的数据,保存到一个表中
        hiveContext.sql("DROP TABLE IF EXISTS top3_sales");
        top3SalesDF.saveAsTable("top3_sales");
        sc.close();
    }
}
