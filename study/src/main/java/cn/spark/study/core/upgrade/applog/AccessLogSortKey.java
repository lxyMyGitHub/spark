package cn.spark.study.core.upgrade.applog;

import scala.Serializable;
import scala.math.Ordered;

import java.util.Objects;

/**
 * @ClassName AccessLogSortKey
 * @Deseription 日志的二次排序key
 * @Author lxy_m
 * @Date 2019/12/25 20:30
 * @Version 1.0
 */
public class AccessLogSortKey implements Ordered<AccessLogSortKey>, Serializable {
    private static final long serialVersionUID = -2007035747484915113L;

    //时间戳
    private long timestamp;
    //上行流量
    private long upTraffic;
    //下行流量
    private long downTraffic;


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

    public AccessLogSortKey() {
    }

    public AccessLogSortKey(long timestamp, long upTraffic, long downTraffic) {
        this.timestamp = timestamp;
        this.upTraffic = upTraffic;
        this.downTraffic = downTraffic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccessLogSortKey that = (AccessLogSortKey) o;
        return timestamp == that.timestamp &&
                upTraffic == that.upTraffic &&
                downTraffic == that.downTraffic;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, upTraffic, downTraffic);
    }

    @Override
    public int compare(AccessLogSortKey other) {
        if(upTraffic - other.upTraffic != 0){
            return (int)(upTraffic - other.upTraffic);
        }else if(downTraffic - other.downTraffic != 0){
            return (int)(downTraffic - other.downTraffic);
        }else if(timestamp - other.timestamp != 0){
            return (int)(timestamp - other.timestamp);
        }
        return 0;
    }

    @Override
    public boolean $less(AccessLogSortKey other) {
        if(upTraffic < other.upTraffic){
            return true;
        }else if(upTraffic == other.upTraffic && downTraffic < other.downTraffic){
            return true;
        }else if(upTraffic == other.upTraffic && downTraffic == other.downTraffic && timestamp < other.timestamp){
            return true;
        }
        return false;
    }

    @Override
    public boolean $greater(AccessLogSortKey other) {
        if(upTraffic > other.upTraffic){
            return true;
        }else if(upTraffic == other.upTraffic && downTraffic > other.downTraffic){
            return true;
        }else if(upTraffic == other.upTraffic && downTraffic == other.downTraffic && timestamp > other.timestamp){
            return true;
        }
        return false;
    }

    @Override
    public boolean $less$eq(AccessLogSortKey other) {
        if($less(other)){
            return true;
        }else if(upTraffic == other.upTraffic && downTraffic == other.downTraffic && timestamp == other.timestamp){
            return true;
        }
        return false;
    }

    @Override
    public boolean $greater$eq(AccessLogSortKey other) {
        if($greater(other)){
            return true;
        }else if(upTraffic == other.upTraffic && downTraffic == other.downTraffic && timestamp == other.timestamp){
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(AccessLogSortKey other) {
        if(upTraffic - other.upTraffic != 0){
            return (int)(upTraffic - other.upTraffic);
        }else if(downTraffic - other.downTraffic != 0){
            return (int)(downTraffic - other.downTraffic);
        }else if(timestamp - other.timestamp != 0){
            return (int)(timestamp - other.timestamp);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "AccessLogSortKey{" +
                "timestamp=" + timestamp +
                ", upTraffic=" + upTraffic +
                ", downTraffic=" + downTraffic +
                '}';
    }
}
