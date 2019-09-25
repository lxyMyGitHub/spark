package cn.spark.study.sql;

import java.io.Serializable;

/**
 * @ClassName Student
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/9/4 10:22
 * @Version 1.0
 */
public class Student implements Serializable {
        private int id;
        private String name;
        private int age;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
