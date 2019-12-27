import java.util.Random;

/**
 * @ClassName CoinAndApple
 * @Deseription TODO
 * @Author lxy_m
 * @Date 2019/10/16 22:21
 * @Version 1.0
 */
public class CoinAndApple {
    public static void main(String[] args) {
        Random coin = new Random();
        Integer number = 20000;
//        System.out.println(2<<3);
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
        System.out.println(getRandIndexOnNumber(number,coin));
    }

    /**
     * 在给定索引上限范围内获取随机index
     * @param number
     * @param coin
     * @return
     */
    public static Integer getRandIndexOnNumber (int number,Random coin){
        int count = getCount(number);
        boolean flag = true;
        Integer index = 0;
        while(flag){
            //如果不满足,下一次重新获取,保证id随机且唯一
            index = getIndex(coin, count);
            if(index <= number){
                flag = false;
            }
        }
        return index;
    }

    /**
     * 在2幂次区间内获取随机数
     * @param coin
     * @param count
     * @return
     */
    private static Integer getIndex(Random coin, int count) {
        String indexStr ="";
        while (count>0){
            indexStr +=coin.nextInt(2);
            count --;
        }
        return Integer.valueOf(indexStr,2);
    }

    /**
     * 获取给定数字所在2的幂次区间的幂
     * @param number
     * @return
     */
    private static int getCount(int number) {
        int count = 1;
        while(number!=0){
            count++;
            if( number - 2<<count-1 <= 0) {
                break;
            }
        }
        return count;
    }
}