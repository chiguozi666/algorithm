package Singal;


import java.util.*;
import java.util.function.BiConsumer;

import static java.lang.Math.*;

public class FWAlgHight{
    static List<Position> errorList = new LinkedList<>();//禁忌表
    //private static int bestIndex = 0;
    int countTimes = 0;
    public static final double upLimit = 1;
    public static final double downLimit = 0;
    //private double maxCost = -Double.MAX_VALUE;
    //private double minCost = Double.MAX_VALUE;
    final public static double kesi = Double.MIN_VALUE;
    static int initFireworkNums = 5;//30时优秀
    final public static double a = 0.04;
    final public static double b = 0.8;
    final public static double m = 50;
    final public static double mhat = 5;
    final public static double AHat = 40;
    private static final double P0 = 0.8;//筛选的概率
    //private Position minPosition = null;
    private int range;
    private Random rnd = new Random();
    private List<Individual> fireworks = null;
    private double sHat[] = null;
    private double A[] = null;
    private int vecDimension;
    private static double temperature;// FWA论文5.5
    final private static double cr = 0.98;
    private static int iter = 300;
    private static Individual bestFirework = null;
    List<Individual> sparks[] = new List[initFireworkNums];//
    Random random = new Random();
    double deadline;
    PositionFactory positionFactory;
    public FWAlgHight(){
        countTimes = 0;
        bestFirework = null;
        iter = 100;
        temperature = 10000;
        vecDimension = 30;
        this.positionFactory = new PositionFactory(vecDimension, upLimit, downLimit, new Individual.FitnessCulTor() {
            @Override
            public double culFitness(Individual i) {
                return FUtil.F8(i.getPosition().getData());
            }
        });
        while (iter-->0){
            initS_A();
            getSparkLoc();
            getGaussianSpark();
            for (int i = 0; i < initFireworkNums; i++) {
                seleteNewSpark(i);//按照论文，每个烟花自己筛选
                temperature = temperature*cr;//(16)
            }
            recordBest();
        }
        System.out.println("生成了"+countTimes+"个解");
        //DrawSolutionUtil.draw(bestFirework.getSolution());
    }
    public Individual getMinCostFirework(List<Individual> fireworks){
        Individual bestFirework = fireworks.get(0);
        for (Individual Individual:
                fireworks) {
            if(bestFirework.fitness>Individual.fitness){
                bestFirework = Individual;
            }
        }
        return bestFirework;
    }
    public Individual getMaxCostFirework(List<Individual> fireworks){
        Individual bestFirework = fireworks.get(0);
        for (Individual Individual:
                fireworks) {
            if(bestFirework.fitness<Individual.fitness){
                bestFirework = Individual;
            }
        }
        return bestFirework;
    }
    //构建S矩阵和A矩阵，S，A矩阵是负责控制烟花大小还有振幅的
    public void initS_A() {
        double siSum = 0;//si分母累加的部分
        double s[] = new double[fireworks.size()];
        sHat = new double[fireworks.size()];
        A = new double[fireworks.size()];
        double ASum = 0;//a分母累加的部分
        double minCost = getMinCostFirework(fireworks).fitness;//得到当前最小的花费
        double maxCost = getMaxCostFirework(fireworks).fitness;
        for (int i = 0; i < fireworks.size(); i++) {//对应eq(2)
            Individual Individual = fireworks.get(i);
            s[i] = maxCost - Individual.fitness;
            siSum += s[i];
            s[i] += kesi;
            //计算增幅向量
            A[i] = Individual.fitness - minCost;
            ASum += A[i];
            A[i] += kesi;
        }
        for (int i = 0; i < fireworks.size(); i++) {//对应eq(3) s[i]是没有m的
            s[i] = m * s[i] / (siSum + kesi);
            if (s[i] < a * m) sHat[i] = round(a * m);
            else if (s[i] > b * m) sHat[i] = round(b * m);
            else sHat[i] = round(s[i]);
            A[i] = AHat * A[i] / (ASum + kesi);
        }
    }
    public void getSparkLoc(){
        for (int i = 0; i < initFireworkNums; i++) {
            sparks[i] = new LinkedList<>();
        }
        for(int index = 0;index < fireworks.size();index++){
            Individual individual = fireworks.get(index);
            for (int i = 0; i < sHat[index]; i++) {
                //
                double z = round((vecDimension)*random());//一共有z维度的被影响
                Position loc =  individual.getPosition().clone();;//深拷贝
                for (int j = 0; j < z;) {//j是投标的数目
                    boolean isVisit[] = new boolean[vecDimension];//这里的visit用于抽出n维，这n维是不重复的。
                    double h = A[index]*(random()*2+-1);
                    double prob = random()*vecDimension;//轮盘赌的飞镖
                    double prob_temp = 0;
                    for (int k = 0; k < vecDimension; k++) {
                        prob_temp+=1;
                        if(prob<prob_temp){
                            if(!isVisit[k]) {
                                loc.set(k, loc.get(k) + h);
                                if (loc.get(k) > upLimit || loc.get(k) < downLimit) {//越界判断
                                    loc.set(k, downLimit + abs(loc.get(k)) % (upLimit - downLimit));
                                    isVisit[k] = true;
                                }
                                j++;//成功更改一个
                            }
                            break;//这一轮投标结束
                        }
                    }
                }
                Individual spark = new Individual(loc);
                sparks[index].add(spark);
            }
        }
    }
    public void getGaussianSpark(){
        for (int i = 0; i < mhat; i++) {
            int index = (int)round(random()*fireworks.size())%fireworks.size();//随机挑出一个烟花
            Individual individual = fireworks.get(index);
            Position loc =  individual.getPosition().clone();
            double z = round((vecDimension)*random());//一共有z维度的被影响
            for (int j = 0; j < z;) {//j是投标的数目
                boolean isVisit[] = new boolean[vecDimension];//这里的visit用于抽出n维，这n维是不重复的。
                double prob = random()*vecDimension;//轮盘赌的飞镖
                double prob_temp = 0;
                for (int k = 0; k < vecDimension; k++) {
                    prob_temp+=1;
                    if(prob<prob_temp){
                        if(!isVisit[k]) {
                            isVisit[k] = true;//todo:后面改的
                            loc.set(k, loc.get(k)*random.nextGaussian());
                            if (loc.get(k) > upLimit || loc.get(k) < downLimit) {//越界判断
                                loc.set(k, downLimit + abs(loc.get(k)) % (upLimit - downLimit));
                            }
                            j++;//成功更改一个
                        }
                        break;//这一轮投标结束
                    }
                }
            }
            Individual firework1 = new Individual(loc);
            sparks[i].add(firework1);
        }
    }
    public void seleteNewSpark(int index){//这个地方是参照FWA里面的烟花算法写的，和原本的不一样，对应Alg5
        //index代表目前遍历烟花的顺序
        Individual bestFirework = sparks[index].get(0);//最好火花的位置
        for (Individual spark: sparks[index]) {//找出fs 和 gs 里面最好的firework
            if(bestFirework.fitness>=spark.fitness){//todo:为了保证更快的变异，相同cost也替换
                bestFirework = spark;
            }
        }
        Double alpha = null;//公式(16)  10~14
        if(bestFirework.fitness - fireworks.get(index).fitness <= 0){
            alpha = 1.0;
        }else {
            alpha = Math.exp(fireworks.get(index).fitness-bestFirework.fitness)/temperature;
        }
        if(alpha>=random()){
            fireworks.set(index,bestFirework);
        }else {
            return;
        }
    }
    private void recordBest(){//要满足特性
        if(bestFirework == null){
            bestFirework = fireworks.get(0);
        }
        for (Individual Individual:
                fireworks) {
            if(bestFirework.fitness>Individual.fitness){
                bestFirework = Individual;
            }
        }

        //Logger.getLogger(this.getClass()).log(Priority.ERROR,bestFirework.fitness);
    }
    private static double culDistance(Position a,Position b){
        int distance = 0;
        for (int i = 0; i < a.size(); i++) {
            distance+=sqrt(a.get(i)-b.get(i))*(a.get(i)-b.get(i));
        }
        return (distance);
    }
    
}
