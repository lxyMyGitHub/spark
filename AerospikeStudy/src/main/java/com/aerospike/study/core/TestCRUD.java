package com.aerospike.study.core;

import com.aerospike.client.*;
import com.aerospike.client.policy.BatchPolicy;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.study.util.AerospikeConnectionPool;

/**
 * @ClassName TestCRUD
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/10 16:41
 * @Version 1.0
 */
public class TestCRUD {
    private static AerospikeConnectionPool clientPool = new AerospikeConnectionPool();
    public static void main(String[] args) {
        AerospikeClient client = clientPool.getClient();
        WritePolicy policy = new WritePolicy();
        policy.setTimeout(50);
        //设置过期时间,单位秒
        /**
         * 设置writePolicy.expiration为0可以在每次记录更新时在服务器端应用默认的TTL。
         * 设置writePolicy.expiration为-1以指定记录永不过期。
         */
//        policy.expiration=2;
        //param1 : namespace
        //param2: set
        //param3: key
//        Key key = new Key("test","lxy_test001","key006");
//        write(client, policy, key);
//        deleteBin(client, policy, key);
//        update(client, policy, key);
//        query(client, policy, key);
//        deleteKey(client, policy, key);
//        batchQuery(client);

        multipleOps(client);
    }

    private static void batchQuery(AerospikeClient client) {
        int size = 5;
        Key[] keys = new Key[5];
        for (int i = 0; i <size; i++) {
            keys[i] = new Key("test","lxy_test001","key00"+i);
        }
        BatchPolicy batchPolicy = new BatchPolicy();
        batchPolicy.setTimeout(50);
        Record[] records = client.get(batchPolicy, keys);
        for (Record record : records) {
            System.out.println(record.toString());
        }
    }

    private static void deleteKey(AerospikeClient client, WritePolicy policy, Key key) {
        client.delete(policy,key);
    }

    private static void query(AerospikeClient client, WritePolicy policy, Key key) {
        Record record = client.get(policy, key);
        System.out.println(record.getString("name"));
        System.out.println(record.getString("age"));
        Record record1 = client.get(policy, key, "name", "age");
        System.out.println(record1.toString());
    }

    private static void update(AerospikeClient client, WritePolicy policy, Key key) {
        //        Bin age = new Bin("age","24");
//        client.put(policy,key,age);
        Bin newAge = new Bin("age","26");
        client.put(policy,key,newAge);
    }


    private static void deleteBin(AerospikeClient client, WritePolicy policy, Key key) {
        //删除时,置空就可以
        Bin delBin = Bin.asNull("age");
        client.put(policy,key,delBin);
    }

    private static void write(AerospikeClient client, WritePolicy policy, Key key) {
        //param1 : name
        //param2 : value
        Bin bin = new Bin("name","XYLiang");
        Bin bin2 = new Bin("age","25");
        client.put(policy,key,bin,bin2);
    }

    private static void multipleOps (AerospikeClient client){
        WritePolicy policy = new WritePolicy();
        policy.setTimeout(50);
        Key key = new Key("test", "demoset", "opkey2");
        Bin bin1 = new Bin("optintbin", 7);
        Bin bin2 = new Bin("optstringbin", "string value");
        client.put(policy, key, bin1, bin2);

        Bin bin3 = new Bin(bin1.name, 4);
        Bin bin4 = new Bin(bin2.name, "new string2");
        /**
         * operate()
         * 参数1: policy
         * 参数2: Key
         * 参数3-n Operation操作
         *  可以是:
         *  get() 获取所以record
         *  get(String binName)  根据binName获取记录
         *  add(Bin bin)  累加,现有值必须为integer
         *  put(Bin bin) 更新/覆盖/新增
         *  append(Bin bin) 在原字符串后追加
         *  getHeader() 读取记录元数据,生存时间和版本
         *  prepend(Bin bin) 在原字符串前追加
         *  touch() 重写同一条记录
         */
        Record record = client.operate(policy, key, Operation.add(bin3),
                Operation.put(bin4), Operation.get(),Operation.touch());
        System.out.println(record.toString());
    }
}
