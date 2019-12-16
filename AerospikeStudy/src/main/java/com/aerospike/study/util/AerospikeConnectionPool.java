package com.aerospike.study.util;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;
import org.junit.Test;

import java.util.LinkedList;

/**
 * @ClassName AerospikeConnectionPool
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/10 15:29
 * @Version 1.0
 */
public class AerospikeConnectionPool {
    private static LinkedList<AerospikeClient> clientPool = new LinkedList<AerospikeClient>();
    private static int asPoolSize = 10;
    static {
        while(clientPool.size() < asPoolSize){
            Host[] hosts = new Host[] {
//                    new Host("192.168.44.141", 3000),
//                    new Host("192.168.44.142", 3000),
//                    new Host("192.168.44.143", 3000)
                    new Host("192.168.2.130", 3000)
            };
            ClientPolicy policy = new ClientPolicy();
//            用户名
//            policy.user = "myuser";
//            密码
//            policy.password = "mypass";
            AerospikeClient client =  new AerospikeClient(policy, hosts);
            clientPool.add(client);
        }
    }

    public AerospikeClient getClient(){
        if(clientPool.size() <= 0){
            init();
            return clientPool.pop();
        }else{
            return clientPool.pop();
        }
    }

    public AerospikeConnectionPool(){
        init();
    }

    public void init(){
        while(clientPool.size() < asPoolSize){
            Host[] hosts = new Host[] {
                    new Host("192.168.2.130", 3000)
//                    new Host("192.168.44.141", 3000),
//                    new Host("192.168.44.142", 3000),
//                    new Host("192.168.44.143", 3000)
            };
            ClientPolicy policy = new ClientPolicy();
//            用户名
//            policy.user = "myuser";
//            密码
//            policy.password = "mypass";
            AerospikeClient client =  new AerospikeClient(policy, hosts);
            clientPool.add(client);
        }
    }

    public void close(AerospikeClient client){
        if(clientPool.size() >= asPoolSize){
            client.close();
        }else{
            clientPool.add(client);
        }
    }

    @Test
    public void test1() {
        AerospikeClient client = getClient();
        System.out.println(client);
        System.out.println(client.getClusterStats().threadsInUse);
        for (int index = 0;index < client.getNodes().length; index++) {
            System.out.println(client.getNodes()[index]);
        }
    }
}
