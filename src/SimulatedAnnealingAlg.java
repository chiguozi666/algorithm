import static java.lang.Math.*;
//模拟退火算法
public class SimulatedAnnealingAlg {
    public static double f(double x){
        return x*sin(x)*cos(2*x)-2*x*sin(3*x)+3*x*sin(4*x);
    }

    public static void main(String[] args) {
        double tempreture = 1000000;
        double deltaT = 0.99;
        double eps = 1e-14;//结束的条件
        int upLimit = 50;
        int downLimit = 0;
        double lastx = random()*(upLimit-downLimit)+downLimit;
        double currentx;
        double bestX = 0;
        int i = 1;
        while(tempreture>eps){
            currentx = lastx+2*(random()-0.5)*tempreture;
            if(currentx>upLimit){
                currentx = upLimit;
            }
            if(currentx<downLimit){
                currentx = downLimit;
            }//越界判断
            if(f(lastx)>f(currentx)){
                lastx = currentx;
            }else {
                if(Math.exp(-abs(f(bestX)-f(currentx)/tempreture))>random()){
                    lastx = currentx;
                }
            }
            if(f(lastx)<f(bestX)){
                bestX = lastx;
            }
            System.out.println("第"+i+++"轮最优解(x,y)："+lastx+"   "+f(bestX));
            tempreture = tempreture*deltaT;
        }
    }
}
