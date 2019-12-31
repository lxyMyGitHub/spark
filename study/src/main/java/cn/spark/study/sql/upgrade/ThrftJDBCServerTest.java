package cn.spark.study.sql.upgrade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @ClassName ThrftJDBCServerTest
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/30 9:41
 * @Version 1.0
 */
public class ThrftJDBCServerTest {
    public static void main(String[] args) {
        String sql = "select name from users where id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
            conn = DriverManager.getConnection("jdbc:hive2://weekend109:10001/default?hive.server2.transport.mode=http;hive.server2.thrift.http.path=cliservice",
                    "root","");
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,1);
            rs = pstmt.executeQuery();
            while (rs.next()){
                String name = rs.getString(1);
                System.out.println(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
