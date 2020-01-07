package cn.spark.study.streaming;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @ClassName JavaCustomReceiver
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2020/1/3 10:15
 * @Version 1.0
 */
public class JavaCustomReceiver extends Receiver<String> {
    String host = null;
    int port = -1;
    public JavaCustomReceiver(String host,int port) {
        super(StorageLevel.MEMORY_AND_DISK_2());
        this.host = host;
        this.port = port;
    }

    @Override
    public void onStart() {
        new Thread(){
            @Override
            public void run(){
                receive();
            }
        }.start();
    }

    private void receive() {
        Socket socket = null;
        String userInput = null;
        try{
            socket = new Socket(host,port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(!isStopped() && (userInput = reader.readLine()) != null){
                System.out.println("Received data '" + userInput + "'");
                store(userInput);
            }
            reader.close();
            socket.close();
            restart("Trying to connect again");
        }catch (Exception e){
            restart("Trying to connect again",e);
        }
    }

    @Override
    public void onStop() {

    }
}
