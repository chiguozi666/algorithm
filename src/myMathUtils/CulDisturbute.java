package myMathUtils;

public class CulDisturbute {
    public static double gamma(double x, double setAbsRelaErr) {
        //setAbsRelaErr 相对误差绝对值
        //递归结束条件
        if(x < 0) {
            return gamma(x + 1, setAbsRelaErr) / x;
        }
        if(Math.abs(1.0 - x) < 0.00001) {
            return 1;
        }
        if(Math.abs(0.5 - x) < 0.00001) {
            return Math.sqrt(3.1415926);
        }

        if(x > 1.0) {
            return (x - 1) * gamma(x - 1, setAbsRelaErr);
        }
        double res = 0.0;
        double temp = 1.0;
        double check = 0.0;
        int i = 1;
        while(Math.abs((check - temp) / temp) > setAbsRelaErr){
            check = temp;
            temp *= i / (x - 1 + i);
            i++;
        }
        res = temp * Math.pow(i, x - 1);
        return res;
    }
}
