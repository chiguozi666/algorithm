package Singal;

import java.util.Arrays;

import static java.lang.Math.*;


public class PsoAlgorithm {
    public static double uplimit = 50;
    public static double downlimit = 0;
    public static double vuplimit = 3;
    public static double vdownlimit = -3;
    static int i = 0;
    public static void main(String[] args) {
        int size = 10;
        double population[] = initPopulation(uplimit,downlimit,size);
        double curv[] =new double[size];//存放当前速度的数组
        for(int i=0;i<size;i++){
            curv[i] = 0;//初始化
        }
        double minLocation[] = new double[size];//个体最优位置
        double fPopulation[] = new double[size];//个体适应度
        minLocation = Arrays.copyOf(population,size);//初始化

        double minfPopulation[] = new double[size];//个体最优位置的值--最小值
        double w = 2;//惯性
        double c1 = 0.1;//个体最佳位置的权重
        double c2 = 0.1;//全局最佳位置的权重
        int gen = 50;//粒子群的代数
        int iter = 1;//第一代开始
        double bestLocation = population[1];
        double bestfLocation = f(bestLocation);
        for(;iter<=gen;iter++){
            //System.out.println("第"+iter+"代"+Arrays.toString(population));
            //System.out.println("    "+"第"+iter+"代"+Arrays.toString(curv));
            for(int i = 0;i<size;i++){
                fPopulation[i] = f(population[i]);//计算出适应度
            }
            for(int i = 0;i<size;i++){
                //更新最个体最优适应度minfPopilation以及最优的位置minLocation
                if(fPopulation[i]<minfPopulation[i]){
                    minfPopulation[i] = fPopulation[i];
                    minLocation[i] = population[i];
                }
            }
            //跟新全局最优解
            for(int i = 0;i<size;i++){
                if(bestfLocation>minfPopulation[i]){
                    bestfLocation = minfPopulation[i];
                    bestLocation = minLocation[i];
                }
            }
            //计算出速度
            for(int i = 0;i<size;i++){
                curv[i] = w*random()*curv[i] + c1*random()*(minLocation[i]-population[i]) + c2*random()*(bestLocation - population[i]);
                curv[i] = getlimitV(curv[i]);
            }
            //更新坐标
            for(int i = 0 ;i<size;i++){
                population[i] = getlimitLocation(population[i]+curv[i]);
            }
            System.out.println("    第"+iter+"轮最小值:"+bestfLocation+"  *** "+bestLocation);
        }
        //获得最后结果,偷懒了直接复制上面的代码了
        for(int i = 0;i<size;i++){
            fPopulation[i] = f(population[i]);//计算出适应度
            if(fPopulation[i]<minfPopulation[i]){
                minfPopulation[i] = fPopulation[i];
                minLocation[i] = fPopulation[i];
            }
            if(bestfLocation<minfPopulation[i]){
                bestfLocation = minfPopulation[i];
                bestLocation = minLocation[i];
            }
        }
        System.out.println(bestfLocation+" *** "+bestLocation);

    }
    public static double f(double x){
        return x*sin(x)*cos(2*x)-2*x*sin(3*x)+3*x*sin(4*x);
    }
    /**
     * 若大于上界返回上界，若小于下界则返回下界*/
    public static double getlimitLocation(double x){
        if(x>uplimit)return uplimit;
        if(x<downlimit)return downlimit;
        return x;
    }
    public static double getlimitV(double v){
        if(v>vuplimit)return vuplimit;
        if(v<vdownlimit)return vdownlimit;
        return v;
    }
    /**
     *
     * @param uplimit 上界
     * @param downlimit 下界
     * @param size 大小
     * @return 返回一个初始化种群
     */
    public static double[] initPopulation(double uplimit,double downlimit,int size){
        double population[] = new double[size];
        for(int i = 0;i<size;i++){
            population[i] = downlimit+(uplimit-downlimit)*random();
        }
        return population;
    }

}
