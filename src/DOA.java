import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DOA {
    double beta1 = -2+4*Math.random();
    double beta2 = -1+2*Math.random();
    int populationSize = 20;
    public static final int iteration = 100000;
    public static final double lb = 0;
    public static final double ub = 50;

    public static final int dim = 50;
    public static final double P = 0.5;//Hunting or Scavenger rate.
    public static final double Q = 0.7;//Group attack or persecution
    CulFitness culFitness = null;
    int minAtk;//最小攻击数目
    int maxAtk;//最大攻击数目 populationSize/minAtk
    int willAtk;//Math.round(minAtk+(maxAtk-minAtk)*Math.random)
    List<List<Double>> dogs = new LinkedList<>();
    double fitness[] = new double[populationSize];
    public DOA(CulFitness culFitness){
        this.culFitness = culFitness;
        int iter = 0;
        generate();
        while(iter<iteration){
            beta1 = -2+4*Math.random();
            beta2 = -1+2*Math.random();
            if(Math.random() < P){
                if(Math.random() < Q){
                    //策略1
                    strage1();
                }else {
                    //策略2
                    strage2();
                }
            }else {
                //策略3
                strage3();
            }
            strage4();
            double fitnessMin = Double.MAX_VALUE;
            for(int i = 0; i < dogs.size();i++){
                if(fitnessMin>fitness[i])fitnessMin = fitness[i];
            }
            System.out.println(iter+"  "+fitnessMin);
            iter++;
        }
    }
    //小团体进攻
    private void strage1(){
        List<Double> bestdog = findBest();
        int na = 2 + (int)(Math.random()*(dogs.size()/2-2));
        int atkCount = 0;
        boolean visit[] = new boolean[dogs.size()];
        List<List<Double>> willAtkDogs = new LinkedList<>();
        while(atkCount<na){//设置即将攻击的狗群
            int index = (int)(dogs.size()*Math.random());
            if(!visit[index]){
                atkCount++;
                visit[index] = true;
                List<Double> newDog = dogs.get(index).stream().collect(Collectors.toList());//深复制
                willAtkDogs.add(newDog);
            }
        }
        for(int i = 0;i<dogs.size();i++){
            if(visit[i]){
                List<Double> curDog = dogs.get(i);
                List<Double> temp = new ArrayList<>(dim);
                for (int j = 0; j < dim; j++) {
                    temp.add(0.0);
                }
                for(List<Double> dog:willAtkDogs){//对每只狼都搜一遍;
                    for (int j = 0; j < dim; j++) {
                        double t = curDog.get(j)-dog.get(j);
                        temp.set(j,temp.get(j)+Math.abs(t));//对应eq2
                    }
                }
                for (int j = 0; j < dim; j++) {
                    temp.set(j,temp.get(j)*beta1/na-bestdog.get(j));//eq2
                }
                dogs.set(i,temp);
                fitness[i] = culFitness.cul(temp);
            }
        }
    }
    //全部个体自己去觅食
    private void strage2(){
        List<Double> bestDog = findBest();
        for (int i = 0; i < dogs.size(); i++) {
            int r1 = (int)(Math.random()*dogs.size()); //随机跟条狗走
            while(r1==i){
                r1 = (int)(Math.random()*dogs.size());
            }
            List<Double> dogR = dogs.get(r1);
            List<Double> curDog = dogs.get(i);
            double preCul = beta1*Math.exp(beta2);
            for (int j = 0; j < dim; j++) {//java的向量计算真烦;
                curDog.set(j,preCul*(dogR.get(j)-curDog.get(j))+bestDog.get(j));//eq3
            }
            fitness[i] = culFitness.cul(curDog);
        }
    }
    //随便找个哥们去吃他剩下的腐肉
    private void strage3(){
        for (int i = 0; i < dogs.size(); i++) {
            int r1 = (int)(Math.random()*dogs.size()); //随机跟条狗走
            while(r1==i){
                r1 = (int)(Math.random()*dogs.size());
            }
            List<Double> dogR = dogs.get(r1);
            List<Double> curDog = dogs.get(i);
            double preCul = Math.exp(beta2);
            for (int j = 0; j < dim; j++) {//java的向量计算真烦;
                int sigma = Math.random()<=0.5?1:-1;//eq4里面的(-1)^sigma
                curDog.set(j,1.0/2.0*(preCul*dogR.get(j)-sigma*curDog.get(j)));//eq3
            }
            fitness[i] = culFitness.cul(curDog);
        }
    }
    //淘汰劣质
    private void strage4(){
        List<Double> bestDog = findBest();
        double fitnessMax = -Double.MAX_VALUE;
        double fitnessMin = Double.MAX_VALUE;
        for(int i = 0; i < dogs.size();i++){
            if(fitnessMax<fitness[i])fitnessMax = fitness[i];
            if(fitnessMin>fitness[i])fitnessMin = fitness[i];
        }
        for (int i = 0; i < dogs.size(); i++) {
            double survival = (fitnessMax -fitness[i])/(fitnessMax-fitnessMin);
            if(survival<=0.3){
                int r1 = (int)(Math.random()*dogs.size()); //随机跟条狗走
                int r2 = (int)(Math.random()*dogs.size()); //随机跟条狗走
                while(r1==r2){
                    r2 = (int)(Math.random()*dogs.size());
                }
                List<Double> curDog = dogs.get(i);
                List<Double> dogR1 = dogs.get(r1);
                List<Double> dogR2 = dogs.get(r2);
                for (int j = 0; j < dogs.size(); j++) {
                    int sigma = Math.random()<=0.5?1:-1;//eq4里面的(-1)^sigma
                    curDog.set(j,bestDog.get(j)+0.5*(dogR1.get(j)-sigma*dogR2.get(j)));
                }
                fitness[i] = culFitness.cul(curDog);
            }
        }
    }
    private List<Double> findBest(){
        List<Double> best = dogs.get(0);
        double bestFitness = culFitness.cul(best);
        for (int i = 0; i < dogs.size(); i++) {
            List<Double> dog = dogs.get(i);
            double curFitness = culFitness.cul(dog);//这里是最小化的
            if(curFitness<bestFitness){//原文是最大化，说是fitness但是这里代码体现的是最小化
                bestFitness = curFitness;
                best = dog;
            }
        }
        return best;
    }
    private void generate(){
        for (int i = 0; i < populationSize; i++) {
            Double x[] = new Double[dim];
            List<Double> dog = new ArrayList<>(dim);
            for(int j = 0; j < dim; j++){
                dog.add(j,lb + (ub - lb)*Math.random());
            }
            fitness[i] = culFitness.cul(dog);
            dogs.add(dog);
        }
    }
    public static void main(String[] args) {
        new DOA(new CulFitness() {
            @Override
            public Double cul(List<Double> List) {
                return FUtil.Sphere(List);
            }
        });
    }
    interface CulFitness{
        public Double cul(List<Double> List);//通过坐标算出适应值
    }
}
