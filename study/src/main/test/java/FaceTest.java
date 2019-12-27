/**
 * @ClassName FaceTest
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/12/9 14:51
 * @Version 1.0
 */
public class FaceTest {
    public static void main(String[] args) {
        /**
         * 企业进行技术改造,预计需要300万资金,计划通过5年筹备完成,每年年末存入一定的资金到银行中,银行存款年利率为6%
         * 利息每月末支付,已结算的利息计入下月计息金额的本金,假设每年筹集的金额均相等,请计算你每年年末至少需要筹集多少资金
         */
        double total = 3000000;
        double rate = 0.06;
        int monthTotal = 5 * 12;
        double capital = total/5;
        for(double amount = capital;amount > 0;amount --){
            double temp= 0;
            for(int i =0;i<5;i++){
                temp += amount;
                temp = temp * Math.pow((1+rate),12);
            }
            if(temp < total){
                capital = amount +1;
                break;
            }
        }
        System.out.println("capital is :"+capital);
    }
}
