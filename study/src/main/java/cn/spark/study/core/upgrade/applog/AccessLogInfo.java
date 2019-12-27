package cn.spark.study.core.upgrade.applog;

import java.io.Serializable;

/**
 * @ClassName AccessLogInfo
 * @Deseription 访问日志信息类,可序列化
 * @Author lxy_m
 * @Date 2019/12/25 20:03
 * @Version 1.0
 */
public class AccessLogInfo implements Serializable {

    private static final long serialVersionUID = 5749943279909593929L;
    //时间戳
    private long timestamp;
    //上行流量
    private long upTraffic;
    //下行流量
    private long downTraffic;

    public AccessLogInfo(long timestamp, long upTraffic, long downTraffic) {
        this.timestamp = timestamp;
        this.upTraffic = upTraffic;
        this.downTraffic = downTraffic;
    }

    public AccessLogInfo() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getUpTraffic() {
        return upTraffic;
    }

    public void setUpTraffic(long upTraffic) {
        this.upTraffic = upTraffic;
    }

    public long getDownTraffic() {
        return downTraffic;
    }

    public void setDownTraffic(long downTraffic) {
        this.downTraffic = downTraffic;
    }
}
