package cn.spark.study.core;

import scala.Serializable;
import scala.math.Ordered;

import java.util.Objects;

/**
 * @ClassName SecondarySortKey
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/8/23 9:32
 * @Version 1.0
 */
public class SecondarySortKey implements Ordered<SecondarySortKey>, Serializable {

    private static final long serialVersionUID = -5384701224849114190L;
    private int first;
    private int second;

    public SecondarySortKey(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public SecondarySortKey() {
    }

    @Override
    public int compare(SecondarySortKey other) {
        if(this.first-other.getFirst() != 0){
            return this.first - other.getFirst();
        }else {
            return this.second - other.getSecond();
        }
    }

    @Override
    public boolean $less(SecondarySortKey other) {
        if(this.first < other.getFirst()){
            return true;
        }else if(this.first==other.getFirst() && this.second < other.getSecond()){
            return true;
        }
        return false;
    }

    @Override
    public boolean $greater(SecondarySortKey other) {
        if (this.first > other.first) {
            return true;
        }else if(this.first == other.getFirst() && this.second > other.getSecond()){
            return true;
        }
        return false;
    }

    @Override
    public boolean $less$eq(SecondarySortKey other) {
        if($less(other)){
            return true;
        }else if(this.first == other.getFirst() && this.second == other.getSecond()){
            return true;
        }
        return false;
    }

    @Override
    public boolean $greater$eq(SecondarySortKey other) {
        if (this.$greater(other)) {
            return true;
        }else if(this.first == other.getFirst() && this.second == other.getSecond()){
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(SecondarySortKey other) {
        if(this.first-other.getFirst() != 0){
            return this.first - other.getFirst();
        }else {
            return this.second - other.getSecond();
        }
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecondarySortKey that = (SecondarySortKey) o;
        return first == that.first &&
                second == that.second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
