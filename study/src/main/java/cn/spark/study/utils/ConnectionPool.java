package cn.spark.study.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;

/**
 * @ClassName ConnectionPool
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/26 11:35
 * @Version 1.0
 */
public class ConnectionPool {

    //静态的Connection队列
    private static LinkedList<Connection> connectionQueue;

    static{
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取连接,多线程访问并发控制
     * @return
     */
    public synchronized static Connection getConnection(){
        try{
            //懒创建
            if(connectionQueue == null){
                connectionQueue = new LinkedList<Connection>();
                for(int i = 0;i<10;i++){
                    Connection conn = DriverManager.getConnection("jdbc:mysql://weekend109:3306/test","root","root");
                    connectionQueue.push(conn);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return connectionQueue.poll();
    }

    public static void returnConnection(Connection conn){
        connectionQueue.push(conn);
    }

    public static void main(String[] args) {
        System.out.println("begin");
        Connection conn = ConnectionPool.getConnection();
        System.out.println("start");
        System.out.println(conn);
        System.out.println("end");
    }
}
